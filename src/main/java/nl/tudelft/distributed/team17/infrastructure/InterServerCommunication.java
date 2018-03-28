package nl.tudelft.distributed.team17.infrastructure;

import nl.tudelft.distributed.team17.application.KnownServerList;
import nl.tudelft.distributed.team17.application.Ledger;
import nl.tudelft.distributed.team17.model.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class InterServerCommunication
{
	private final static Logger LOG = LoggerFactory.getLogger(InterServerCommunication.class);

	private ExecutorService executorService;
	private RestTemplate restTemplate;
	private KnownServerList knownServerList;

	@Autowired
	public InterServerCommunication(ExecutorService executorService, RestTemplate restTemplate, KnownServerList knownServerList)
	{
		this.executorService = executorService;
		this.restTemplate = restTemplate;
		this.knownServerList = knownServerList;
	}

	private final Object ledgerExchangeLock = new Object();
	public List<Ledger> exchangeLedger(Ledger ledger)
	{
		synchronized (ledgerExchangeLock)
		{
			LedgerDto ourLedgerAsDto = LedgerDto.from(ledger);

			Set<String> knownServers = knownServerList.getKnownServers();
			List<Callable<LedgerDto>> fns = new ArrayList<>(knownServers.size());
			for(String server : knownServers)
			{
				fns.add(() -> exchangeLedgerWithServer(ourLedgerAsDto, server));
			}

			long timeoutInMs = 100;
			List<Future<LedgerDto>> futures = executeAsync(fns, timeoutInMs);

			List<Ledger> ledgers = futures.stream()
					.map(ledgerDtoFuture -> {
						try
						{
							return ledgerDtoFuture.get();
						}
						catch (Exception ex)
						{
							throw new Error("Unrecoverable error: exchange failed for some reason", ex);
						}
					})
					.filter(Objects::nonNull)
					.map(LedgerDto::toLedger)
					.collect(Collectors.toList());

			return ledgers;
		}
	}

	public void broadcast(Command command)
	{

		Set<String> knownServers = knownServerList.getKnownServers();
		List<Runnable> fns = new ArrayList<>(knownServers.size());
		for(String server : knownServers)
		{
			// optimization: serialize only once
			// optimization: abort after timeout?
			executorService.submit(() -> sendCommandToServer(server, command));
		}
	}

	private void sendCommandToServer(String server, Command command)
	{
		URI uriWithLocation = URI.create(server + "/command/");

		try
		{
			restTemplate.postForEntity(uriWithLocation, command, String.class);
		}
		catch (Exception ex)
		{
			LOG.error("Error occurred during broadcast of command to Server [" + server + "]", ex);
		}
	}

	private LedgerDto exchangeLedgerWithServer(LedgerDto ledgerDto, String server)
	{
		// precache URI's in knownServerList maybe?
		URI uriWithLocation = URI.create(server + "/ledger/");

		// todo: need to record success/failure so that consensus knows how long to wait for other ledgers, or maybe we just exchange Ledgers? How do we fight it out if parallel exchange happens?
		try
		{
			return restTemplate.postForObject(uriWithLocation, ledgerDto, LedgerDto.class);
		}
		catch (Exception ex)
		{
			LOG.error("Error occurred during Exchange of Ledger with Server [" + server + "], returning NULL instead and continuing", ex);
			return null;
		}
	}

	private List<Future<LedgerDto>> executeAsync(List<Callable<LedgerDto>> fns, long timeoutInMs)
	{
		try
		{
			return executorService.invokeAll(fns, timeoutInMs, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ex);
		}
	}
}
