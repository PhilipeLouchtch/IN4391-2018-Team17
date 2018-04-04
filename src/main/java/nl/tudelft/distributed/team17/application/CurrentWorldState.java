package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.WorldState;
import nl.tudelft.distributed.team17.model.command.Command;
import nl.tudelft.distributed.team17.service.WorldStateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Wraps a world state and provides a single abstraction with which synchronous access to a WorldState can be made
 */
@Component
public class CurrentWorldState
{
	private final static Logger LOG = LoggerFactory.getLogger(CurrentWorldState.class);

	private Ledger currentLedger;

	private Queue<Command> priorityCommandsQueue;

	@Autowired
	public CurrentWorldState(WorldStateGenerator worldStateGenerator)
	{
		this.currentLedger = Ledger.genesis(worldStateGenerator.generateNewWorldState());

		// Genesis does not get exchanged
		this.currentLedger = this.currentLedger.makeNewHead();

		priorityCommandsQueue = new ArrayDeque<>();
	}

	public synchronized void switchToNewAcceptedLedger(Ledger newlyAcceptedLedger)
	{
		if (!currentLedger.isClosed())
		{
			throw new IllegalStateException("Sanity check: Cannot switch to a new accepted ledger while current is not closed");
		}

		if (!newlyAcceptedLedger.isClosed())
		{
			throw new IllegalStateException("Sanity check: Cannot switch to a new accepted ledger if it is not marked closed");
		}

		LOG.debug("Switching to newly accepted ledger");
		// Switch from current ledger head (loser) to the winner head
		doSwitchoverToAcceptedLedger(newlyAcceptedLedger);

		LOG.debug("Executing queued priority commands on new ledger head");
		// exec priority commands on the new head
		applyPriorityCommands();
	}

	public synchronized void runInCriticalSection(CurrentWorldStateCriticalSection criticalSectionCode)
	{
		LOG.debug("Entered CurrentWorldState critical section");
		Ledger agreedUponLedger = criticalSectionCode.runInCriticalSectionOfCurrentWorldState(currentLedger);
		switchToNewAcceptedLedger(agreedUponLedger);
		LOG.debug("Leaving CurrentWorldState critical section");
	}

	private void applyPriorityCommands()
	{
		priorityCommandsQueue.forEach(currentLedger::applyCommand);
		priorityCommandsQueue.clear();
	}

	private void doSwitchoverToAcceptedLedger(Ledger newlyAcceptedLedger)
	{
		newlyAcceptedLedger.replace(currentLedger);

		Ledger newHead = newlyAcceptedLedger.makeNewHead();
		currentLedger = newHead;
	}

	public synchronized void addCommand(Command command)
	{
		// TODO: hash commands so we make sure not to duplicate them for current ledger
		// TODO: don't search further than the worldstate that the command was based on
		// TODO: make sure that the worldstate clock is properly maintained
		if (command.isPriority())
		{
			// todo: optimization, don't queue if the ledger only contains priority commands because in that case we can apply it directly
			priorityCommandsQueue.add(command);
		}
		else
		{
			currentLedger.applyCommand(command);
		}

	}

	@FunctionalInterface
	public interface CurrentWorldStateCriticalSection
	{
		/**
		 * Function to run within the Critical Section of the CurrentWorldState instance, the returned Ledger will be made the new head
		 * @param currentHeadLedger The current "head" ledger
		 * @return The Ledger to make into the new head
		 */
		Ledger runInCriticalSectionOfCurrentWorldState(Ledger currentHeadLedger);
	}

	public WorldState getLastCheckpoint()
	{
		// TODO: optimize
		return currentLedger.getLastAcceptedWorldState();
	}
}
