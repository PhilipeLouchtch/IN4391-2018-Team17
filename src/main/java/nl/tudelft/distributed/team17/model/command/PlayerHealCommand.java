package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.BoardLocation;
import nl.tudelft.distributed.team17.model.WorldState;

public class PlayerHealCommand extends PlayerCommand
{
	@JsonProperty("playerId")
	private Integer playerId;

	@JsonProperty("clock")
	private Integer clock;

	@JsonProperty("locationToHeal")
	private BoardLocation locationToHeal;

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

	public BoardLocation getLocationToHeal()
	{
		return locationToHeal;
	}

	@Override
	public WorldState apply(WorldState worldState)
	{
		return null;
	}
}