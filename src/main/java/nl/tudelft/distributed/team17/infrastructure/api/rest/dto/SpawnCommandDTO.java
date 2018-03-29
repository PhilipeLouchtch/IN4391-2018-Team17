package nl.tudelft.distributed.team17.infrastructure.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpawnCommandDTO
{
    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("clock")
    private Integer clock;

    public SpawnCommandDTO(String emailAddress, Integer clock)
    {
        this.emailAddress = emailAddress;
        this.clock = clock;
    }

    // Jackson
    private SpawnCommandDTO()
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
}
