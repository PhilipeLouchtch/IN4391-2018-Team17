package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.command.Command;
import nl.tudelft.distributed.team17.service.WorldStateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.PriorityQueue;

/**
 * Wraps a world state and provides a single abstraction with which synchronous access to a WorldState can be made
 */
@Component
public class CurrentWorldState
{
	private Ledger currentLedger;

	private PriorityQueue<Command> priorityCommandsQueue;

	@Autowired
	public CurrentWorldState(WorldStateGenerator worldStateGenerator)
	{
		this.currentLedger = Ledger.genesis(worldStateGenerator.generateNewWorldState());
		priorityCommandsQueue = new PriorityQueue<>();
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

		// Switch from current ledger head (loser) to the winner head
		doSwitchoverToAcceptedLedger(newlyAcceptedLedger);

		// exec priority commands on the new head
		applyPriorityCommands();
	}

	public synchronized void runInCriticalSection(CurrentWorldStateCriticalSection criticalSectionCode)
	{
		criticalSectionCode.runInCriticalSectionOfCurrentWorldState(currentLedger);
	}

	private synchronized void applyPriorityCommands()
	{
		priorityCommandsQueue.forEach(currentLedger::applyCommand);
		priorityCommandsQueue.clear();
	}

	private void doSwitchoverToAcceptedLedger(Ledger newlyAcceptedLedger)
	{
		// todo: check if need to migrate queued commands or missing messages?
		Ledger previouslyCurrentLedger = currentLedger;

		currentLedger = newlyAcceptedLedger;
		currentLedger.replace(previouslyCurrentLedger);

		Ledger newHead = currentLedger.makeNewHead();
		currentLedger = newHead;
	}

	public synchronized void addCommand(Command command)
	{
		if (command.isPriority())
		{
			// todo: optimization, don't queue if the ledger only contains priority commands because in that case we can apply it directly
			priorityCommandsQueue.add(command);
		}

		// if ledger is being published then must wait on new accepted state before applying command,
		// all other callers will be blocked anyway as one caller is waiting inside

		currentLedger.applyCommand(command);
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
}
