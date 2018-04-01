package nl.tudelft.distributed.team17.model;

public class LocationContaintsNoUnitsException extends RuntimeException
{
	public LocationContaintsNoUnitsException(Location location)
	{
		super(String.format("Location [%s] contained no units, action cannot be executed", location));
	}
}
