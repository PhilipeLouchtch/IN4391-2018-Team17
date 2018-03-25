package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.command.Command;
import org.springframework.stereotype.Service;

@Service
public class CommandProcessor
{
	// create a new buffer / ledger and any new commands need to lock on ledger

	private CurrentWorldState currentWorldState;
	private Ledger ledger;

	public CommandProcessor(CurrentWorldState currentWorldState, Ledger ledger)
	{
		this.currentWorldState = currentWorldState;
		this.ledger = ledger;
	}

	/**
	 * Applies the given command to the consideredWorldState
	 * @param command The command to apply
	 * @return boolean indicating if the command was successfully applied to the consideredWorldState, false otherwise
	 */
	public boolean applyCommand(Command command)
	{
		try
		{
			currentWorldState.applyToConsideredWorldState(command::apply);
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}
}
