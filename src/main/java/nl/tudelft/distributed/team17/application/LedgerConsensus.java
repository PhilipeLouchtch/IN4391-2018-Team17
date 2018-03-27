package nl.tudelft.distributed.team17.application;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LedgerConsensus
{
	public LedgerConsensus()
	{
	}

	public Ledger runConsensus(List<Ledger> ledgers, int generationToRunFor)
	{
		Ledger bestLedger = ledgers.stream()
				.filter(ledger -> ledger.getGeneration() == generationToRunFor)
				.reduce(null, this::pickBest);

		if (bestLedger == null)
		{
			throw new Error("Unrecoverable error: consensus failed to find a ledger");
		}

		return bestLedger;
	}

	private Ledger pickBest(Ledger ledgerOne, Ledger ledgerTwo)
	{
		/* handle identities */
		if (ledgerOne == null && ledgerTwo == null)
		{
			return null;
		}
		if (ledgerOne == null && ledgerTwo != null)
		{
			return ledgerTwo;
		}
		if (ledgerOne != null && ledgerTwo == null)
		{
			return ledgerOne;
		}
		if (ledgerOne.equals(ledgerTwo))
		{
			return ledgerOne;
		}

		int acceptedCommandsInLedgerOneChain = ledgerOne.getNumCommandsAcceptedSoFar();
		int acceptedCommandsInLedgerTwoChain = ledgerTwo.getNumCommandsAcceptedSoFar();

		// cannot discern best by the largest amount of commands
		if (acceptedCommandsInLedgerOneChain == acceptedCommandsInLedgerTwoChain)
		{
			if (ledgerOne.getTieBreaker() == ledgerTwo.getTieBreaker())
			{
				throw new Error("Unrecoverable error: ledgers are both as-good and have same tie breaker, cannot pick!");
			}

			if (ledgerOne.getTieBreaker() > ledgerTwo.getTieBreaker())
			{
				return ledgerOne;
			}
			else
			{
				return ledgerTwo;
			}
		}

		return acceptedCommandsInLedgerOneChain > acceptedCommandsInLedgerTwoChain ? ledgerOne : ledgerTwo;
	}
}
