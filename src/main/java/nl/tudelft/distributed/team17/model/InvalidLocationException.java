package nl.tudelft.distributed.team17.model;

public class InvalidLocationException extends RuntimeException
{
	public InvalidLocationException(Location location)
	{
		super(String.format("Invalid location: [%s]", String.valueOf(location)));
	}
}
