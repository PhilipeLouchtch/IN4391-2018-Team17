package nl.tudelft.distributed.team17.infrastructure.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import distributed.systems.das.units.Unit;

public class MoveCommandDTO
{
    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("clock")
    private Integer clock;

    @JsonProperty("direction")
    private Unit.Direction direction;

    public MoveCommandDTO(String emailAddress, Integer clock, Unit.Direction direction)
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

    public Unit.Direction getDirection()
    {
        return direction;
    }
}
