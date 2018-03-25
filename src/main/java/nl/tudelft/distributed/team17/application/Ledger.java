package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.WorldState;
import nl.tudelft.distributed.team17.model.command.PlayerCommand;

import java.util.ArrayList;
import java.util.List;

public class Ledger
{
	boolean isClosed = false;

	private Ledger previous;

	// saves us the effort of rebuilding the state from the tx's
	private WorldState worldState;

	// These are the "transactions" of the ledger
	private List<PlayerCommand> commands;

	public Ledger()
	{
		isClosed = false;
		previous = null;
		commands = new ArrayList<>();
	}

	public static Ledger genesis()
	{
		return new Ledger();
	}

}
