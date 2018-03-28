package nl.tudelft.distributed.team17.infrastructure.api.rest;

import nl.tudelft.distributed.team17.application.CurrentWorldState;
import nl.tudelft.distributed.team17.application.Ledger;
import nl.tudelft.distributed.team17.infrastructure.LedgerDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ServerEndpoints
{
	public static final String ledgerExchangeEndpoint = "/ledger/exchange";

	private CurrentWorldState currentWorldState;

	@PostMapping(path = ServerEndpoints.ledgerExchangeEndpoint)
	public LedgerDto exchangeLedgers(@RequestBody LedgerDto ledgerAsDto)
	{
		Ledger ledger = ledgerAsDto.toLedger();

		// TODO: atomic request current Ledger and Start the Exchange protocol...

		currentWorldState.
	}
}
