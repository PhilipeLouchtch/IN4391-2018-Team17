package nl.tudelft.distributed.team17.application;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
		LOG.info("Starting Consensus determination for round [{}], with [{}] ledgers", generationToRunFor, ledgers.size());

		LOG.debug("Received the following Ledgers:");
		if (LOG.isDebugEnabled())
		{
			ledgers.forEach(ledger -> LOG.debug(ledger.toString()));
		}

		// First check if any of the ledgers share a hashcode, this means that we're in a "slave" round.
		// Another machine has already decided on the winner which is reason why we have more than 1 ledger with same hash,
		// in this case we need to accept the majority
		Map<String, List<Ledger>> ledgersGroupedByHashHex = ledgers.stream()
				.collect(Collectors.groupingBy(Ledger::getHashHex));

		Optional<List<Ledger>> max = ledgersGroupedByHashHex.values().stream().filter(list -> list.size() > 1).max(Comparator.comparingInt(List::size));
		if (max.isPresent())
		{
			Ledger ledger = max.get().get(0);
			LOG.info("Received more than one Ledger with same hash (%s), picking it as winner", ledger.getHashHex());

			return ledger;
		}
		else
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
			// Ledgers are same, any one will do
			return ledgerOne;
		}

		int acceptedCommandsInLedgerOneChain = ledgerOne.getNumCommandsAcceptedSoFar();
		int acceptedCommandsInLedgerTwoChain = ledgerTwo.getNumCommandsAcceptedSoFar();

		// cannot discern best by the largest amount of commands
		if (acceptedCommandsInLedgerOneChain == acceptedCommandsInLedgerTwoChain)
		{
			if (ledgerOne.getTieBreaker() == ledgerTwo.getTieBreaker())
			{
				RuntimeException fatalException = new RuntimeException("Unrecoverable error: ledgers are both as-good and have same tie breaker but are not same (hashes do not match), cannot pick");
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
