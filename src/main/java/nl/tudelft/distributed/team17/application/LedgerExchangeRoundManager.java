package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.infrastructure.LedgerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LedgerExchangeRoundManager
{
	private final static Logger LOG = LoggerFactory.getLogger(LedgerExchangeRoundManager.class);

	/* RoundId --> Round{ ServerId -> Ledger } */
	private final Map<Integer, LedgerExchangeRound> exchangeRounds;

	private LedgerConsensus ledgerConsensus;

	private LedgerExchangeRoundManager(LedgerConsensus ledgerConsensus)
	{
		this.ledgerConsensus = ledgerConsensus;
		this.exchangeRounds = new HashMap<>();
	}

	public synchronized void accept(String serverId, Ledger ledger) throws LedgerExchangeRound.LedgerExchangeRoundIsClosedException
	{
		Objects.requireNonNull(ledger, "LedgerDto cannot be null");

		int roundId = ledger.getGeneration();

		final LedgerExchangeRound ledgerExchangeRound = getExistingOrCreateNewLedgerExchangeRound(roundId);
		synchronized(ledgerExchangeRound)
		{
			if (!ledgerExchangeRound.isClosed())
			{
				ledgerExchangeRound.accept(serverId, ledger);
			}
			else
			{
				LOG.warn("Could not accept Ledger (from [{}], round [{}]), round is closed", serverId, roundId);
			}
		}
	}

	public Ledger concludeRound(int roundId) throws LedgerExchangeRound.LedgerExchangeRoundIsClosedException
	{
		synchronized (exchangeRounds)
		{
			if (!exchangeRounds.containsKey(roundId))
			{
				throw new IllegalArgumentException("Bugcheck: cannot close a round that does not exist");
			}
		}

		// todo: chance of race here if the architecutre changes where more than 1 instance of an object exists that can call this fn

		LedgerExchangeRound ledgerExchangeRound = getExistingOrCreateNewLedgerExchangeRound(roundId);
		synchronized (ledgerExchangeRound)
		{
			Collection<Ledger> ledgersInRound = ledgerExchangeRound.received();

			Ledger winner = ledgerConsensus.runConsensus(ledgersInRound, roundId);
			ledgerExchangeRound.closeRound(winner);

			return winner;
		}
	}

	public Optional<LedgerDto> getWinnerOfRound(int roundId)
	{
		LedgerExchangeRound ledgerExchangeRound;
		synchronized (exchangeRounds)
		{
			if (!exchangeRounds.containsKey(roundId))
			{
				return Optional.empty();
			}

			ledgerExchangeRound = getExistingOrCreateNewLedgerExchangeRound(roundId);
		}

		synchronized (ledgerExchangeRound)
		{
			if (ledgerExchangeRound.isClosed())
			{
				try
				{
					Ledger winner = ledgerExchangeRound.tryGetWinner();
					return Optional.of(LedgerDto.from(winner));
				}
				catch (Exception ex)
				{
					LOG.warn("Failed to get winning ledger", ex);
					return Optional.empty();
				}
			}
		}
		return Optional.empty();
	}

	private LedgerExchangeRound getExistingOrCreateNewLedgerExchangeRound(int roundId)
	{
		synchronized(exchangeRounds)
		{
			return exchangeRounds.computeIfAbsent(roundId, LedgerExchangeRound::createRound);
		}
	}

	public boolean isPresent(String server, int generation)
	{
		synchronized (exchangeRounds)
		{
			if (exchangeRounds.containsKey(generation))
			{
				LedgerExchangeRound ledgerExchangeRound = exchangeRounds.get(generation);
				return ledgerExchangeRound.hasLedgerFor(server);
			}
		}

		return false;
	}
}
