package nl.tudelft.distributed.team17.infrastructure.api.rest;

import nl.tudelft.distributed.team17.application.*;
import nl.tudelft.distributed.team17.infrastructure.LedgerDto;
import nl.tudelft.distributed.team17.model.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping
public class ServerEndpoints
{
	private static final Logger LOG = LoggerFactory.getLogger(ServerEndpoints.class);

	public static final String ledgerExchangeEndpoint = "/ledger/exchange";
	public static final String serverExchangeEndpoint = "/bootstrap/server";
	public static final String serverForwardedCommandEndpoint = "/forwarded/command";

	private CurrentWorldState currentWorldState;
	private LedgerExchangeRoundManager ledgerExchangeRoundManager;
	private KnownServerList knownServerList;
	private LedgerController ledgerController;

	public ServerEndpoints(CurrentWorldState currentWorldState, LedgerExchangeRoundManager ledgerExchangeRoundManager, KnownServerList knownServerList, LedgerController ledgerController)
	{
		this.currentWorldState = currentWorldState;
		this.ledgerExchangeRoundManager = ledgerExchangeRoundManager;
		this.knownServerList = knownServerList;
		this.ledgerController = ledgerController;
	}

	@PostMapping(path = ServerEndpoints.ledgerExchangeEndpoint)
	public LedgerDto exchangeLedgers(@RequestBody LedgerDto ledgerAsDto, HttpServletRequest request)
	{
		ledgerController.startRunning();

		int generationOfLedger = ledgerAsDto.getGeneration();

		// TODO: atomic request current Ledger and Start the Exchange protocol...

		// Good enough?? Or need to check X-Forwarded-For anyway?
		String serverId = request.getRemoteAddr();

		try
		{
			ledgerExchangeRoundManager.accept(serverId, ledgerAsDto);
		}
		catch (Exception ex)
		{
			LOG.warn(String.format("Could not accept Ledger from [%s] due to error", serverId), ex);
		}

		final int TIMEOUT_IN_MS = 5000;
		final int PERIOD_IN_MS = 100;
		for (int i = 0; i < TIMEOUT_IN_MS / PERIOD_IN_MS; i++)
		{
			Optional<LedgerDto> winnerOfRound = ledgerExchangeRoundManager.getWinnerOfRound(generationOfLedger);

			try
			{
				Thread.sleep(PERIOD_IN_MS);
			}
			catch (InterruptedException ex)
			{
				Thread.currentThread().interrupt();
				throw new RuntimeException(ex);
			}

			if (winnerOfRound.isPresent())
			{
				return winnerOfRound.get();
			}
		}

		return null;
	}

	@PostMapping(path = serverExchangeEndpoint)
	public List<String> learnNewServer(@RequestBody List<String> servers, HttpServletRequest httpServletRequest)
	{
		String remoteAddr = httpServletRequest.getRemoteAddr();

		knownServerList.acceptServer(remoteAddr);
		for (String server : servers)
		{
			knownServerList.acceptServer(server);
		}

		return new ArrayList<>(knownServerList.getKnownServers());
	}

	@GetMapping(path = "knownservers")
	public Set<String> getKnownServers()
	{
		return knownServerList.getKnownServers();
	}

	@PostMapping(path = ServerEndpoints.serverForwardedCommandEndpoint)
	public void acceptForwardedCommand(@RequestBody Command command)
	{
		ledgerController.startRunning();

		currentWorldState.addCommand(command);
	}
}
