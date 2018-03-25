package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import distributed.systems.das.units.Unit;
import nl.tudelft.distributed.team17.model.WorldState;

public class PlayerMoveCommand extends Command
{
	@JsonProperty("direction")
	private Unit.Direction direction;

	static public PlayerMoveCommand createWithEmailAuthentication(
			String emailAddress,
			Integer clock,
			Unit.Direction direction)
	{
		return new PlayerMoveCommand(emailAddress, clock, direction);
	}

	public PlayerMoveCommand(String emailAddress, Integer clock, Unit.Direction direction)
	{
		super(emailAddress, clock, false);
		this.direction = direction;
	}

	// For Jackson
	private PlayerMoveCommand()
	{
	}

	public Unit.Direction getDirection()
	{
		return direction;
	}

	@Override
	public void apply(WorldState worldState)
	{
		worldState.movePlayer(getPlayerId(), direction);
	}
}
