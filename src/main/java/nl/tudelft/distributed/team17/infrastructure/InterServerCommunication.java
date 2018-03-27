package nl.tudelft.distributed.team17.infrastructure;

import nl.tudelft.distributed.team17.application.KnownServerList;
import nl.tudelft.distributed.team17.application.Ledger;
import nl.tudelft.distributed.team17.application.LedgerConsensus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Component
public class InterServerCommunication
{
	private RestTemplate restTemplate;
	private KnownServerList knownServerList;
	private LedgerConsensus ledgerConsensus;

	@Autowired
	public InterServerCommunication(RestTemplate restTemplate, KnownServerList knownServerList, LedgerConsensus ledgerConsensus)
	{
		this.restTemplate = restTemplate;
		this.knownServerList = knownServerList;
		this.ledgerConsensus = ledgerConsensus;
	}

	private final Object ledgerExchangeLock = new Object();
	public void exchangeLedger(Ledger ledger, Consumer<List<Ledger>> receivedLedgers)
	{
		synchronized (ledgerExchangeLock)
		{
			LedgerDto ledgerDto = LedgerDto.from(ledger);

			Set<String> knownServers = knownServerList.getKnownServers();
			knownServers.forEach((server) ->
			{
				// precache URI's in knownServerList maybe?
				URI uriWithLocation = URI.create(knownServers + "/ledger/");


				// todo: need to record success/failure so that consensus knows how long to wait for other ledgers, or maybe we just exchange Ledgers? How do we fight it out if parallel exchange happens?
//				try
//				{
					restTemplate.postForLocation(uriWithLocation, ledgerDto);
//				}
			});
		}
	}
}
