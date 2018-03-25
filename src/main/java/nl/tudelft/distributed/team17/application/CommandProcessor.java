package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.command.InvalidCommandException;
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

	public boolean applyCommand(PlayerCommand playerCommand)
	{
		try
		{
			currentWorldState.applyToConsideredWorldState(playerCommand::apply);
		}
		catch (InvalidCommandException ex)
		{
			return false;
		}

		return true;
	}
}
