package nl.tudelft.distributed.team17.model;

public class UnitDeadException extends RuntimeException
{
    public UnitDeadException(String unitId)
    {
        super(String.format("Unit [%s] is dead, cannot perform command", String.valueOf(unitId)));
    }
}
