package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.WorldState;

public abstract class PlayerCommand
{
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("clock")
    private Integer clock;

    public Integer getPlayerId()
    {
        return playerId;
    }

    public Integer getClock()
    {
        return clock;
    }

    public PlayerCommand(Integer playerId, Integer clock)
    {
        this.playerId = playerId;
        this.clock = clock;
    }

    // Jackson
    protected PlayerCommand()
    {
    }

    public abstract void apply(WorldState worldState);

}