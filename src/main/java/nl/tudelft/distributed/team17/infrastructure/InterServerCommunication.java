package nl.tudelft.distributed.team17.infrastructure;

import nl.tudelft.distributed.team17.application.KnownServerList;
import nl.tudelft.distributed.team17.application.Ledger;
import nl.tudelft.distributed.team17.application.LedgerExchangeRoundManager;
import nl.tudelft.distributed.team17.infrastructure.api.rest.ServerEndpoints;
import nl.tudelft.distributed.team17.model.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class InterServerCommunication
{
	private final static Logger LOG = LoggerFactory.getLogger(InterServerCommunication.class);
	public final static long EXCHANGE_LEDGERS_TX_TIMEOUT_MS = 200;

	private ExecutorService executorService;
	private RestTemplate restTemplate;
	private KnownServerList knownServerList;

	private LedgerExchangeRoundManager ledgerExchangeRoundManager;

	@Autowired
	public InterServerCommunication(ExecutorService executorService, RestTemplate restTemplate, KnownServerList knownServerList, LedgerExchangeRoundManager ledgerExchangeRoundManager)
	{
		this.executorService = executorService;
		this.restTemplate = restTemplate;
		this.knownServerList = knownServerList;
		this.ledgerExchangeRoundManager = ledgerExchangeRoundManager;
	}

	private final Object ledgerExchangeLock = new Object();
	public Ledger exchangeLedger(Ledger ledger)
	{
		synchronized (ledgerExchangeLock)
		{
			LedgerDto ourLedgerAsDto = LedgerDto.from(ledger);
			Set<String> knownServers = knownServerList.getKnownOtherServers();

			// hacky: List of Callable<LedgerDto> instead of Runnable to more easily set timeouts on the tasks through executorService.invokeAll(...)
			List<Callable<LedgerDto>> fns = new ArrayList<>(knownServers.size());

			for(String server : knownServers)
			{
				// Optimization: do no exchange ledger if already have received from that server for the round,
				// that machine is probably waiting at LedgerExchange for the winner already
				if (ledgerExchangeRoundManager.isPresent(server, ledger.getGeneration()) == false)
				{
					fns.add(() -> {
						LOG.info("exchanging ledger [{}] with [{}]", ledger.getHashHex(), server);
						exchangeLedgerWithServer(ourLedgerAsDto, server);
						LOG.info("exchanged ledger [{}] with [{}]", ledger.getHashHex(), server);
						return null;
					});
				}
			}

			// Ignoring return value as the ledgers are put into the CurrentLedgerExchangeRound indirection layer
			LOG.debug("Sending ledger to {} servers", fns.size());
			List<Future<LedgerDto>> futures = executeAsync(fns, EXCHANGE_LEDGERS_TX_TIMEOUT_MS);
			LOG.debug("Exchange has finished, {} servers responded", futures.stream().filter(future -> !future.isCancelled()).count());

			try
			{
				Ledger winner =  ledgerExchangeRoundManager.concludeRound(ledger.getGeneration());
				return winner;
			}
			catch (Exception ex)
			{
				LOG.error("Bugcheck: could not conclude round during exchangeLedger", ex);
				throw new RuntimeException(ex);
				// mem optimization: we know that only one object will be put into this list
//				return new ArrayList<>(1);
			}
		}
	}

	public void broadcast(Command command)
	{
		Set<String> knownServers = knownServerList.getKnownOtherServers();
		List<Runnable> fns = new ArrayList<>(knownServers.size());
		for(String server : knownServers)
		{
			// optimization: serialize only once
			executorService.submit(() -> sendCommandToServer(server, command));
		}
	}

	private void sendCommandToServer(String server, Command command)
	{
		URI uriWithLocation = URI.create("http://" + server + ServerEndpoints.serverForwardedCommandEndpoint);

		try
		{
			restTemplate.postForEntity(uriWithLocation, command, String.class);
		}
		catch (Exception ex)
		{
			LOG.error("Error occurred during broadcast of command to Server [" + server + "]", ex);
		}
	}

	private void exchangeLedgerWithServer(LedgerDto ledgerDto, String server)
	{
		// precache URI's in knownServerList maybe?
		URI uriWithLocation = URI.create("http://" + server + ServerEndpoints.ledgerExchangeEndpoint);

		try
		{
			LedgerDto receivedLedgerDto = restTemplate.postForObject(uriWithLocation, ledgerDto, LedgerDto.class);
			ledgerExchangeRoundManager.accept(server, receivedLedgerDto.toLedger());
		}
		catch (Exception ex)
		{
			LOG.error("Error occurred during Exchange of Ledger with Server [" + server + "], ignoring and continuing", ex);
		}
	}

	private List<Future<LedgerDto>> executeAsync(List<Callable<LedgerDto>> fns, long timeoutInMs)
	{
		if (fns.size() == 0)
		{
			return Collections.emptyList();
		}

		try
		{
			return executorService.invokeAll(fns, timeoutInMs, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ex);
		}
		catch (Exception ex)
		{
			LOG.error("Exception during executeAsync", ex);
			throw ex;
		}
	}
}
