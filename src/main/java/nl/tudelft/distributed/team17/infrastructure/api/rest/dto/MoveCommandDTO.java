package nl.tudelft.distributed.team17.infrastructure.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Direction;

public class MoveCommandDTO
{
    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("clock")
    private Integer clock;

    @JsonProperty("direction")
    private Direction direction;

    public MoveCommandDTO(String emailAddress, Integer clock, Direction direction)
    {
        this.emailAddress = emailAddress;
        this.clock = clock;
        this.direction = direction;
    }

    // Jackson
    private MoveCommandDTO()
    {
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public Integer getClock()
    {
        return clock;
    }

    public Direction getDirection()
    {
        return direction;
    }
}
