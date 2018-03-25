package nl.tudelft.distributed.team17.model;

public class NoSuchUnitExistsException extends RuntimeException
{
	public NoSuchUnitExistsException(Integer unitId)
	{
		super(String.format("No such unit exists, unit was: [%s]", String.valueOf(unitId)));
	}
}
