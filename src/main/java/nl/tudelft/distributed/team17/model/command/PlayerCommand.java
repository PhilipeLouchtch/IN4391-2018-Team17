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

    public abstract WorldState apply(WorldState worldState);

}