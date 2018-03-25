package nl.tudelft.distributed.team17.application;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class LedgerExchanger implements Runnable
{
	private static final long LEDGER_OPEN_PERIOD_MS = 500;
	private static final long LEDGER_STATUS_CHECK_PERIOD_MS = 10;

	private KnownServerList knownServerList;
	private RestTemplate restTemplate;

	private CurrentWorldState currentWorldState;

	private Instant ledgerOpenedAtInstant;


	public LedgerExchanger(CurrentWorldState currentWorldState)
	{
		this.currentWorldState = currentWorldState;
	}

	@Override
	public void run()
	{
		ledgerOpenedAtInstant = Instant.now();
		try
		{
			Thread.sleep(LEDGER_STATUS_CHECK_PERIOD_MS);
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
			currentWorldState.runInCriticalSection((ledger) ->
			{
				ledger.setClosed();
				// now exchange ledgers between machines
			});
		}
	}
}
