package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.WorldState;
import nl.tudelft.distributed.team17.model.command.Command;

import java.util.ArrayList;
import java.util.List;

public class Ledger
{
	private static final Ledger NO_PREVIOUS = null;

	boolean isClosed = false;

	private Ledger previous;

	// saves us the effort of rebuilding the state from the tx's (make transient?)
	private WorldState worldState;

	// These are the "transactions" of the ledger
	private List<Command> commands;

	// todo: ledger Id & accepted commands in chain fields / methods

	private Ledger(Ledger previous, WorldState worldState, boolean isClosed)
	{
		this.worldState = worldState;
		this.previous = previous;
		this.isClosed = isClosed;

		commands = new ArrayList<>();
	}

	public static Ledger genesis(WorldState worldState)
	{
		return new Ledger(NO_PREVIOUS, worldState, false);
	}

	public synchronized void applyCommand(Command command)
	{
		if (isClosed())
		{
			throw new IllegalStateException("Cannot apply command to a closed Ledger");
		}

		final List<Command> playerCommandsBeforeApply = new ArrayList<>(commands);

		try
		{
			commands.add(command);
			command.apply(worldState);
		}
		catch (Exception ex)
		{
			/*  todo: do we revert worldstate?
				Although we do the checks before modifying it, so 99.9999% exceptions will not mean
			    that the world state has been modified or is in inconsistent state
			 */
			commands = playerCommandsBeforeApply;
		}
	}


	/**
	 * Makes the given ledger the new head, in other words integrates the new ledger into our in memory chain
	 * @param losingLedger The ledger that was "current" to the instance of the application but failed to win in the consensus round
	 * @return The WorldState resulting from applying the commands contained in the ledger on the WordState of the 'previous' ledger
	 */
	public synchronized void replace(Ledger losingLedger)
	{
		this.previous = losingLedger.previous;

		if (!this.isGenesis())
		{
			WorldState worldStateResultingFromPreviousLedger = this.previous.worldState;

			try
			{
				this.worldState = worldStateResultingFromPreviousLedger;
				for (int i = 0; i < commands.size(); i++)
				{
					Command command = commands.get(i);
					command.apply(worldState);
				}
			}
			catch (Exception ex)
			{
				throw new Error("Fatal error: encountered an exception during application of newly accepted Ledger's commands on top of previous ledger", ex);
			}

			// no longer need to keep this in memory, removing reference for GC
			this.previous.worldState = null;
		}
	}

	public synchronized Ledger makeNewHead()
	{
		return new Ledger(this, this.worldState, false);
	}

	public void setClosed()
	{
		isClosed = true;
	}

	public boolean isClosed()
	{
		return isClosed;
	}

	public boolean isGenesis()
	{
		return previous == null;
	}
}
