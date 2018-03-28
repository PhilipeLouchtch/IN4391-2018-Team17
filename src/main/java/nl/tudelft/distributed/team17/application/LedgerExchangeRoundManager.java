package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.infrastructure.LedgerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LedgerExchangeRoundManager
{
	private final static Logger LOG = LoggerFactory.getLogger(LedgerExchangeRoundManager.class);

	/* RoundId --> Round{ ServerId -> Ledger } */
	private final Map<Integer, LedgerExchangeRound> exchangeRounds;

	private LedgerExchangeRoundManager()
	{
		this.exchangeRounds = new HashMap<>();
	}

	public synchronized void accept(String serverId, LedgerDto ledgerDto) throws LedgerExchangeRound.LedgerExchangeRoundIsClosedException
	{
		int roundId = ledgerDto.getGeneration();

		final LedgerExchangeRound ledgerExchangeRound = getExistingOrCreateNewLedgerExchangeRound(roundId);
		synchronized(ledgerExchangeRound)
		{
			if (!ledgerExchangeRound.isClosed())
			{
				ledgerExchangeRound.accept(serverId, ledgerDto);
			}
		}
	}

	public Collection<LedgerDto> concludeRound(int roundId) throws LedgerExchangeRound.LedgerExchangeRoundIsClosedException
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
			Collection<LedgerDto> ledgersCollectedDuringRound = ledgerExchangeRound.closeRound();

			return ledgersCollectedDuringRound;
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
					LedgerDto winner = ledgerExchangeRound.tryGetWinner();
					return Optional.of(winner);
				}
				catch (Exception ex)
				{
					LOG.debug("Failed to get winning ledger", ex);
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
}
