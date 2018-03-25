package nl.tudelft.distributed.team17.model;

public class AttackRangeException extends RuntimeException
{
    public AttackRangeException(Unit unit, Location locationToAttack, int distance)
    {
        super(String.format("Unit with id [%s] cannot attack location [%s], distance [%d] is larger than allowed", unit.getId(), locationToAttack, distance));
    }
}
