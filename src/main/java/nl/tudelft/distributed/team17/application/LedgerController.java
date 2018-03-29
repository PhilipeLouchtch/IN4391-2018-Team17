package nl.tudelft.distributed.team17.application;

import net.coolicer.functional.actions.Rethrow;
import net.coolicer.util.Try;
import nl.tudelft.distributed.team17.infrastructure.InterServerCommunication;
import nl.tudelft.distributed.team17.infrastructure.LedgerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class LedgerController implements Runnable
{
	private final static Logger LOG = LoggerFactory.getLogger(LedgerController.class);

	private static final long LEDGER_OPEN_PERIOD_MS = 500;
	private static final long LEDGER_STATUS_CHECK_PERIOD_MS = 10;

	private static final Instant IS_NOT_RUNNING = null;

	private CurrentWorldState currentWorldState;
	private InterServerCommunication interServerCommunication;
	private LedgerConsensus ledgerConsensus;
	private ExecutorService executorService;
	private LedgerExchangeRoundManager ledgerExchangeRoundManager;

	private Instant ledgerOpenedAtInstant;

	public LedgerController(InterServerCommunication interServerCommunication, CurrentWorldState currentWorldState, LedgerConsensus ledgerConsensus, LedgerExchangeRoundManager ledgerExchangeRoundManager, ExecutorService executorService)
	{
		this.interServerCommunication = interServerCommunication;
		this.currentWorldState = currentWorldState;
		this.ledgerConsensus = ledgerConsensus;
		this.executorService = executorService;
		this.ledgerExchangeRoundManager = ledgerExchangeRoundManager;

		this.ledgerOpenedAtInstant = IS_NOT_RUNNING;
	}

	public boolean isRunning()
	{
		return ledgerOpenedAtInstant != IS_NOT_RUNNING;
	}

	public void startRunning()
	{
		if (!isRunning())
		{
			LOG.info("Starting running ledger");
			executorService.submit(this);
		}
	}

	@Override
	public void run()
	{
		ledgerOpenedAtInstant = Instant.now();
		while(true)
		{
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
	}

	private void handleLedgerOpenTimeoutIfOccurred()
	{
		long msBetweenOpenAndNow = getMsBetweenOpenAndNow();
		if (msBetweenOpenAndNow >= LEDGER_OPEN_PERIOD_MS)
		{
			// handling ledger open timeout
			LOG.info("Ledger timeout occurred, handling");
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

			// Workaround for when there's only one server: need to let ledgerExchangeRoundManager know of our ledger or it will spazz out
			Try.doing(() -> ledgerExchangeRoundManager.accept("THIS_SERVER", LedgerDto.from(ourLedger))).or(Rethrow.asRuntime());

			List<Ledger> ledgers = interServerCommunication.exchangeLedger(ourLedger);
			Ledger agreedLedger = ledgerConsensus.runConsensus(ledgers, ourLedger.getGeneration());

			return agreedLedger;
		});

		// A new ledger has been opened, so set the new open time
		ledgerOpenedAtInstant = Instant.now();
	}
}
