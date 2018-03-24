package nl.tudelft.distributed.team17.model;

public class HealRangeException extends RuntimeException
{
	public HealRangeException(Unit unit, Location locationToHeal, int distance)
	{
		super(String.format("Unit with id [%s] cannot heal location [%s], distance [%d] is larger than allowed", unit.getId(), locationToHeal, distance));
	}
}
