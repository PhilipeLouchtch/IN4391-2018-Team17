package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.infrastructure.InterServerCommunication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class LedgerController implements Runnable
{
	private static final long LEDGER_OPEN_PERIOD_MS = 500;
	private static final long LEDGER_STATUS_CHECK_PERIOD_MS = 10;

	private CurrentWorldState currentWorldState;
	private InterServerCommunication interServerCommunication;
	private LedgerConsensus ledgerConsensus;
	private LedgerExchangeRoundManager ledgerExchangeRoundManager;

	private Instant ledgerOpenedAtInstant;

	public LedgerController(InterServerCommunication interServerCommunication, CurrentWorldState currentWorldState, LedgerConsensus ledgerConsensus, LedgerExchangeRoundManager ledgerExchangeRoundManager)
	{
		this.interServerCommunication = interServerCommunication;
		this.currentWorldState = currentWorldState;
		this.ledgerConsensus = ledgerConsensus;
		this.ledgerExchangeRoundManager = ledgerExchangeRoundManager;
	}

	@Override
	public void run()
	{
		ledgerOpenedAtInstant = Instant.now();
		try
		{
			Thread.sleep(LEDGER_STATUS_CHECK_PERIOD_MS);
			handleLedgerOpenTimeoutIfOccurred();
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ex);
		}
	}

	private void handleLedgerOpenTimeoutIfOccurred()
	{
		long msBetweenOpenAndNow = getMsBetweenOpenAndNow();
		if (msBetweenOpenAndNow >= LEDGER_OPEN_PERIOD_MS)
		{
			// handling ledger open timeout
			doLedgerExchange();
		}
	}

	private long getMsBetweenOpenAndNow()
	{
		return ChronoUnit.MILLIS.between(ledgerOpenedAtInstant, Instant.now());
	}

	private void doLedgerExchange()
	{
		currentWorldState.runInCriticalSection((ourLedger) ->
		{
			ourLedger.setClosed();

			List<Ledger> ledgers = interServerCommunication.exchangeLedger(ourLedger);

			// adding our own ledger to the list of received
			ledgers.add(ourLedger);

			Ledger agreedLedger = ledgerConsensus.runConsensus(ledgers, ourLedger.getGeneration());

			return agreedLedger;
		});

		// A new ledger has been opened, so set the new open time
		ledgerOpenedAtInstant = Instant.now();
	}
}
