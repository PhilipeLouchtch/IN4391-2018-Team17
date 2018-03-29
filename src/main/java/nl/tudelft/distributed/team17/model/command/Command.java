package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import nl.tudelft.distributed.team17.model.UnitDeadException;
import nl.tudelft.distributed.team17.model.WorldState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(DragonAttackCommand.class),
        @JsonSubTypes.Type(PlayerAttackCommand.class),
        @JsonSubTypes.Type(PlayerHealCommand.class),
        @JsonSubTypes.Type(PlayerMoveCommand.class),
        @JsonSubTypes.Type(PlayerSpawnCommand.class)
})
public abstract class Command
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(Command.class);

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("clock")
    private Integer clock;

    @JsonProperty("isPriority")
    private boolean isPriority;

    public String getPlayerId()
    {
        return playerId;
    }

    public Integer getClock()
    {
        return clock;
    }

    public boolean isPriority()
    {
        return isPriority;
    }

    public Command(String playerId, Integer clock, boolean isPriority)
    {
        this.playerId = playerId;
        this.clock = clock;
        this.isPriority = isPriority;
    }

    // Jackson
    protected Command()
    {
    }

    protected void assertUnitAlive(WorldState worldState)
    {
        if(worldState.isUnitDead(getPlayerId()))
        {
            LOGGER.info(String.format("Player [%s] is dead, no interactions possible", getPlayerId()));
            throw new UnitDeadException(getPlayerId());
        }
    }

    public abstract void apply(WorldState worldState);
}