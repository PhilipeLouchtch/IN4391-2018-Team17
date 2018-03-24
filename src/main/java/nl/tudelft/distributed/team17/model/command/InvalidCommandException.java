package nl.tudelft.distributed.team17.model.command;

public class InvalidCommandException extends Exception
{
	public InvalidCommandException(String name, String reason)
	{
		super(String.format("Command [%s] was invalid due to [%s], rejecting", name, reason));
	}
}
