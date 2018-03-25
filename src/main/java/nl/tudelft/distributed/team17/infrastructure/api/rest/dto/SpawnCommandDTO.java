package nl.tudelft.distributed.team17.infrastructure.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpawnCommandDTO
{
    @JsonProperty("emailAddress")
    private String emailAddress;

    public SpawnCommandDTO(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    // Jackson
    private SpawnCommandDTO()
    {
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }
}
