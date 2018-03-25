package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.command.PlayerCommand;
import org.springframework.stereotype.Service;

@Service
public class CommandProcessor
{
	// create a new buffer / ledger and any new commands need to lock on ledger

	private CommandValidator commandValidator;
	private CurrentWorldState currentWorldState;

	public CommandProcessor(CommandValidator commandValidator, CurrentWorldState currentWorldState)
	{
		this.commandValidator = commandValidator;
		this.currentWorldState = currentWorldState;
	}

	/**
	 * Applies the given command to the consideredWorldState
	 * @param playerCommand The command to apply
	 * @return boolean indicating if the command was successfully applied to the consideredWorldState, false otherwise
	 */
	public boolean applyCommand(PlayerCommand playerCommand)
	{
		try
		{
			currentWorldState.applyToConsideredWorldState(playerCommand::apply);
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}
}
