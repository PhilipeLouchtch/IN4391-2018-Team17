package nl.tudelft.distributed.team17.model;

public class OccupiedLocationException extends RuntimeException
{
	public OccupiedLocationException(Location location)
	{
		super(String.format("Location is already occupied, was: [%s]", String.valueOf(location)));
	}
}
