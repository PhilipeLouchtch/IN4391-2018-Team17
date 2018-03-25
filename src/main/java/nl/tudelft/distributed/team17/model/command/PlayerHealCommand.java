package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Location;
import nl.tudelft.distributed.team17.model.WorldState;

public class PlayerHealCommand extends Command
{
	@JsonProperty("playerId")
	private Integer playerId;

	@JsonProperty("clock")
	private Integer clock;

	@JsonProperty("locationToHeal")
	private Location locationToHeal;

	public PlayerHealCommand(Integer playerId, Location locationToHeal, Integer clock)
	{
		super(playerId, clock, false);
		this.locationToHeal = locationToHeal;
	}

	// Jackson
	private PlayerHealCommand()
	{
	}

	public Location getLocationToHeal()
	{
		return locationToHeal;
	}

	@Override
	public void apply(WorldState worldState)
	{
		worldState.healPlayer(playerId, locationToHeal);
	}
}