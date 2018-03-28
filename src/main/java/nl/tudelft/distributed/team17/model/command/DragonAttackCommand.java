package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Unit;
import nl.tudelft.distributed.team17.model.WorldState;

public class DragonAttackCommand extends DragonCommand
{

    @JsonProperty("playerToAttack")
    private Unit playerToAttack;

    static public DragonAttackCommand createDragonAttackCommand(Unit dragon, Integer clock, Unit playerToAttack)
    {
        return new DragonAttackCommand(dragon, clock, playerToAttack);
    }

    private DragonAttackCommand(Unit dragon, Integer clock, Unit playerToAttack)
    {
        super(dragon, clock);
        this.playerToAttack = playerToAttack;
    }

    // Jackson
    protected DragonAttackCommand()
    {
    }

    public Unit getPlayerToAttack()
    {
        return playerToAttack;
    }

    @Override
    public void apply(WorldState worldState)
    {
        LOGGER.info(String.format("Dragon [%s] tries attacking player [%s]", getPlayerId(), playerToAttack.getId()));
        assertUnitAlive(worldState);
        worldState.damageUnit(getDragon(), getPlayerToAttack());
        LOGGER.info(String.format("Dragon [%s] successfully attacked player [%s]", getPlayerId(), playerToAttack.getId()));
    }


}
