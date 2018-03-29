package nl.tudelft.distributed.team17.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LedgerConsensus
{
	private final static Logger LOG = LoggerFactory.getLogger(LedgerConsensus.class);

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
				if (ledgerOne.hashCode() == ledgerTwo.hashCode()) {
					return ledgerOne;
				}

				if (ledgerOne.getCommands().size() != ledgerTwo.getCommands().size()){
					LOG.error("~~~~~~~~ COMMAND SIZES MIS-MATCH");
				}
				else if (ledgerOne.getCommands().size() == 0) {
					return ledgerOne;
				}

				RuntimeException fatalException = new RuntimeException("Unrecoverable error: ledgers are both as-good and have same tie breaker, cannot pick!");
				LOG.error("LedgerConsensus run into a critical error", fatalException);

				throw fatalException;
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
