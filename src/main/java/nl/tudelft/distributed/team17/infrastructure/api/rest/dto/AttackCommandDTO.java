package nl.tudelft.distributed.team17.infrastructure.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Location;

public class AttackCommandDTO
{
    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("clock")
    private Integer clock;

    @JsonProperty("locationToAttack")
    private Location locationToAttack;

    public AttackCommandDTO(String emailAddress, Integer clock, Location locationToAttack)
    {
        this.emailAddress = emailAddress;
        this.clock = clock;
        this.locationToAttack = locationToAttack;
    }

    // Jackson
    private AttackCommandDTO()
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

    public Location getLocationToAttack()
    {
        return locationToAttack;
    }
}
