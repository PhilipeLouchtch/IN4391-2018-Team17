package nl.tudelft.distributed.team17.model.command;

import nl.tudelft.distributed.team17.model.UnitType;
import nl.tudelft.distributed.team17.model.WorldState;

public class PlayerSpawnCommand extends PlayerCommand
{
    static public PlayerSpawnCommand createWithEmailAuthentication(
        String emailAddress,
        Integer clock)
    {
        return new PlayerSpawnCommand(emailAddress, clock);
    }

    private PlayerSpawnCommand(String emailAddress, Integer clock)
    {
        super(emailAddress, clock, true);
    }

    // Jackson
    private PlayerSpawnCommand()
    {
    }

    @Override
    public void apply(WorldState worldState)
    {
        assertUnitAlive(worldState);
        worldState.spawnUnit(getPlayerId(), UnitType.PLAYER);
    }
}
