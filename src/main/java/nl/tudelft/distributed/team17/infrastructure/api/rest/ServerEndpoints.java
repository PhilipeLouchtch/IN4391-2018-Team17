package nl.tudelft.distributed.team17.infrastructure.api.rest;

import nl.tudelft.distributed.team17.application.KnownServerList;
import nl.tudelft.distributed.team17.application.LedgerExchangeRoundManager;
import nl.tudelft.distributed.team17.application.CurrentWorldState;
import nl.tudelft.distributed.team17.infrastructure.LedgerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping
public class ServerEndpoints
{
	private static final Logger LOG = LoggerFactory.getLogger(ServerEndpoints.class);

	public static final String ledgerExchangeEndpoint = "/ledger/exchange";
	public static final String serverExchangeEndpoint = "/bootstrap/server";

	private LedgerExchangeRoundManager ledgerExchangeRoundManager;
	private KnownServerList knownServerList;

	public ServerEndpoints(LedgerExchangeRoundManager ledgerExchangeRoundManager, KnownServerList knownServerList)
	{
		this.ledgerExchangeRoundManager = ledgerExchangeRoundManager;
		this.knownServerList = knownServerList;
	}

	@PostMapping(path = ServerEndpoints.ledgerExchangeEndpoint)
	public LedgerDto exchangeLedgers(@RequestBody LedgerDto ledgerAsDto, HttpServletRequest request)
	{
//		Ledger ledger = ledgerAsDto.toLedger();
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
}
