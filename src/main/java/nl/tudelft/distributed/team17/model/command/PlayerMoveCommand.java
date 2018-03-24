package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import distributed.systems.das.units.Unit;
import nl.tudelft.distributed.team17.model.WorldState;

public class PlayerMoveCommand extends PlayerCommand
{
	@JsonProperty("direction")
	private Unit.Direction direction;

	@JsonProperty("playerId")
	private Integer playerId;

	@JsonProperty("clock")
	private Integer clock;

	public PlayerMoveCommand(Unit.Direction direction, Integer playerId, Integer clock)
	{
		super(playerId, clock);
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
		worldState.movePlayer(playerId, direction);
	}
}
