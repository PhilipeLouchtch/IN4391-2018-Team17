package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.infrastructure.LedgerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

	public synchronized void accept(String serverId, LedgerDto ledgerDto) throws LedgerExchangeRound.LedgerExchangeRoundIsClosedException
	{
		Objects.requireNonNull(ledgerDto, "LedgerDto cannot be null");

		int roundId = ledgerDto.getGeneration();

		final LedgerExchangeRound ledgerExchangeRound = getExistingOrCreateNewLedgerExchangeRound(roundId);
		synchronized(ledgerExchangeRound)
		{
			if (!ledgerExchangeRound.isClosed())
			{
				ledgerExchangeRound.accept(serverId, ledgerDto);
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
			List<Ledger> ledgersInRound = ledgerExchangeRound.received().stream().map(LedgerDto::toLedger).collect(Collectors.toList());

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
}
