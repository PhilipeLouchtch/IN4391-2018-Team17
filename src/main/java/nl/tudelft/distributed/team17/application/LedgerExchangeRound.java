package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.infrastructure.LedgerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LedgerExchangeRound
{
	private final static String THIS_SERVER = "Henk";
	private final static Map<String, LedgerDto> ROUND_IS_CLOSED = null;
	private final static Ledger NO_WINNING_LEDGERS = null;

	private int roundIdentifier;
	private Map<String, LedgerDto> receivedLedgers;
	private Ledger winningLedger;

	private LedgerExchangeRound(int roundIdentifier, Map<String, LedgerDto> receivedLedgers, Ledger winningLedger)
	{
		this.roundIdentifier = roundIdentifier;
		this.receivedLedgers = receivedLedgers;
		this.winningLedger = winningLedger;
	}

	public static LedgerExchangeRound createRound(int roundId)
	{
		LOG.info("Creating a LedgerExchangeRound for round [{}]", roundId);

		Map<String, LedgerDto> ledgerMap = new HashMap<>();

		return new LedgerExchangeRound(roundId, ledgerMap, NO_WINNING_LEDGERS);
	}

	public synchronized void accept(String sourceId, LedgerDto ledgerDto) throws LedgerExchangeRoundIsClosedException
	{
		assertRoundIsActive();
		assertLedgerCanBeAccepted(ledgerDto);

		receivedLedgers.put(sourceId, ledgerDto);
	}

	public synchronized void closeRound(Ledger winner) throws LedgerExchangeRoundIsClosedException
	{
		assertRoundIsActive();

		this.winningLedger = winner;

		receivedLedgers = ROUND_IS_CLOSED;
	}

	public synchronized Collection<LedgerDto> received()
	{
		return receivedLedgers.values();
	}

	public boolean isClosed()
	{
		return receivedLedgers == ROUND_IS_CLOSED;
	}

	public Ledger tryGetWinner()
	{
		if (!isClosed())
		{
			throw new IllegalStateException("Cannot get winner, round not closed");
		}

		if (winningLedger == NO_WINNING_LEDGERS)
		{
			throw new IllegalStateException("Bugcheck: cannot return winner, round is closed but no winners");
		}

		return winningLedger;
	}

	private void assertRoundIsActive() throws LedgerExchangeRoundIsClosedException
	{
		if (isClosed())
		{
			throw new LedgerExchangeRoundIsClosedException(roundIdentifier);
		}
	}

	private void assertLedgerCanBeAccepted(LedgerDto ledgerDto)
	{
		if (ledgerDto.getGeneration() != roundIdentifier)
		{
			String msg = String.format(
					"Cannot accept Ledger into LedgerExchangeRound, roundId <-> generation mismatch, given [%s], expected [%s]",
					roundIdentifier,
					ledgerDto.getGeneration()
			);
			throw new IllegalArgumentException(msg);
		}
	}

	public class LedgerExchangeRoundIsClosedException extends Exception
	{
		public LedgerExchangeRoundIsClosedException(int roundIdentifier)
		{
			super(String.format("Bugcheck: Ledger exchange round [%s] is closed, cannot execute the requested action", roundIdentifier));
		}
	}
}
