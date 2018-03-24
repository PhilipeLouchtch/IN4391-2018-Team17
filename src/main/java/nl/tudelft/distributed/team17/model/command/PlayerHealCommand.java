package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.BoardLocation;

public class PlayerHealCommand
{
	@JsonProperty("playerId")
	private Integer playerId;

	@JsonProperty("locationToHeal")
	private BoardLocation locationToHeal;

	@JsonProperty("clock")
	private Integer clock;

	public PlayerHealCommand(Integer playerId, BoardLocation locationToHeal, Integer clock)
	{
		this.playerId = playerId;
		this.locationToHeal = locationToHeal;
		this.clock = clock;
	}

	// Jackson
	private PlayerHealCommand()
	{
	}

	public Integer getPlayerId()
	{
		return playerId;
	}

	public BoardLocation getLocationToHeal()
	{
		return locationToHeal;
	}

	public Integer getClock()
	{
		return clock;
	}
}
