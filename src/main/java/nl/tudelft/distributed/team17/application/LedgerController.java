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

	private Instant ledgerOpenedAtInstant;

	public LedgerController(InterServerCommunication interServerCommunication, CurrentWorldState currentWorldState, LedgerConsensus ledgerConsensus)
	{
		this.interServerCommunication = interServerCommunication;
		this.currentWorldState = currentWorldState;
		this.ledgerConsensus = ledgerConsensus;
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
		long msBetweenOpenAndNow = ChronoUnit.MILLIS.between(ledgerOpenedAtInstant, Instant.now());
		if (msBetweenOpenAndNow <= LEDGER_OPEN_PERIOD_MS)
		{
			// handling ledger open timeout
			currentWorldState.runInCriticalSection((ourLedger) ->
			{
				ourLedger.setClosed();

				List<Ledger> ledgers = interServerCommunication.exchangeLedger(ourLedger);
				Ledger agreedLedger = ledgerConsensus.runConsensus(ledgers, ourLedger.getGeneration());

				ledgerOpenedAtInstant = Instant.now();

				return agreedLedger;
			});
		}
	}
}
