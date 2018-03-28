package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Direction;
import nl.tudelft.distributed.team17.model.WorldState;

public class PlayerMoveCommand extends PlayerCommand
{
	@JsonProperty("direction")
	private Direction direction;

	static public PlayerMoveCommand createWithEmailAuthentication(
			String emailAddress,
			Integer clock,
			Direction direction)
	{
		return new PlayerMoveCommand(emailAddress, clock, direction);
	}

	public PlayerMoveCommand(String emailAddress, Integer clock, Direction direction)
	{
		super(emailAddress, clock, false);
		this.direction = direction;
	}

	// For Jackson
	private PlayerMoveCommand()
	{
	}

	public Direction getDirection()
	{
		return direction;
	}

	@Override
	public void apply(WorldState worldState)
	{
		assertUnitAlive(worldState);
		worldState.movePlayer(getPlayerId(), getDirection());
	}
}
