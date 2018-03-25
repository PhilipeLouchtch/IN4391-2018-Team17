package nl.tudelft.distributed.team17.infrastructure.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Location;

public class HealCommandDTO
{
    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("clock")
    private Integer clock;

    @JsonProperty("locationToHeal")
    private Location locationToHeal;

    public HealCommandDTO(String emailAddress, Integer clock, Location locationToHeal)
    {
        this.emailAddress = emailAddress;
        this.clock = clock;
        this.locationToHeal = locationToHeal;
    }

    // Jackson
    private HealCommandDTO()
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

    public Location getLocationToHeal()
    {
        return locationToHeal;
    }
}
