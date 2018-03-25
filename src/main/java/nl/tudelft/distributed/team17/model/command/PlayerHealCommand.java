package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Location;
import nl.tudelft.distributed.team17.model.WorldState;

public class PlayerHealCommand extends PlayerCommand
{
	@JsonProperty("locationToHeal")
	private Location locationToHeal;

	static public PlayerHealCommand createWithEmailAuthentication(
			String emailAddress,
			Integer clock,
			Location locationToHeal)
	{
		return new PlayerHealCommand(emailAddress, clock, locationToHeal);
	}

	private PlayerHealCommand(String emailAddress, Integer clock, Location locationToHeal)
	{
		super(emailAddress, clock, false);
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
		assertUnitAlive(worldState);
		worldState.healPlayer(getPlayerId(), getLocationToHeal());
	}
}