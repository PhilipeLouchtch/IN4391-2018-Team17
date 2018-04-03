package nl.tudelft.distributed.team17.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LedgerExchangeRound
{
	private final static Logger LOG = LoggerFactory.getLogger(LedgerExchangeRound.class);

	private final static String THIS_SERVER = "Henk";
	private final static Map<String, Ledger> ROUND_IS_CLOSED = null;
	private final static Ledger NO_WINNING_LEDGERS = null;

	private int roundIdentifier;
	private Map<String, Ledger> receivedLedgers;
	private Ledger winningLedger;

	private LedgerExchangeRound(int roundIdentifier, Map<String, Ledger> receivedLedgers, Ledger winningLedger)
	{
		this.roundIdentifier = roundIdentifier;
		this.receivedLedgers = receivedLedgers;
		this.winningLedger = winningLedger;
	}

	public static LedgerExchangeRound createRound(int roundId)
	{
		LOG.info("Creating a LedgerExchangeRound for round [{}]", roundId);

		Map<String, Ledger> ledgerMap = new HashMap<>();

		return new LedgerExchangeRound(roundId, ledgerMap, NO_WINNING_LEDGERS);
	}

	public synchronized void accept(String sourceId, Ledger ledger) throws LedgerExchangeRoundIsClosedException
	{
		assertRoundIsActive();
		assertLedgerCanBeAccepted(ledger);

		LOG.info("Accepting Ledger (from [{}]), round [{}]) into round [{}]", sourceId, ledger.getGeneration(), roundIdentifier);

		receivedLedgers.put(sourceId, ledger);
	}

	public synchronized boolean hasLedgerFor(String sourceId)
	{
		return receivedLedgers.containsKey(sourceId);
	}

	public synchronized void closeRound(Ledger winner) throws LedgerExchangeRoundIsClosedException
	{
		assertRoundIsActive();

		this.winningLedger = winner;

		// find the sourceId of the winner for logging purposes
		Map.Entry<String, Ledger> winnerEntry = receivedLedgers.entrySet().stream()
				.filter(stringLedgerDtoEntry -> stringLedgerDtoEntry.getValue().getTieBreaker() == winner.getTieBreaker())
				.findAny().get();

		LOG.info("Closing round [{}], winning Ledger is (from [{}], #commands [{}])", roundIdentifier, winnerEntry.getKey(), winner.getCommands().size());

		receivedLedgers = ROUND_IS_CLOSED;
	}

	public synchronized Collection<Ledger> received()
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

	private void assertLedgerCanBeAccepted(Ledger ledgerDto)
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
