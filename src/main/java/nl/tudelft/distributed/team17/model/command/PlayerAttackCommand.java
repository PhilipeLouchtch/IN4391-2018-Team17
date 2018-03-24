package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Location;
import nl.tudelft.distributed.team17.model.WorldState;

public class PlayerAttackCommand extends PlayerCommand
{
	@JsonProperty("playerId")
	private Integer playerId;

	@JsonProperty("location")
	private Location location;

	@JsonProperty("clock")
	private Integer clock;

	public PlayerAttackCommand(Integer playerId, Location location, Integer clock)
	{
		super(playerId, clock);
		this.location = location;
	}

	// Jackson
	private PlayerAttackCommand()
	{
	}

	public Location getLocation()
	{
		return location;
	}

	@Override
	public WorldState apply(WorldState worldState)
	{
		return null;
	}
}