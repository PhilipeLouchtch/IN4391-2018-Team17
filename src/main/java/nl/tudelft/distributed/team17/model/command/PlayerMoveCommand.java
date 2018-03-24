package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import distributed.systems.das.units.Unit;

public class PlayerMoveCommand
{
	@JsonProperty("direction")
	private Unit.Direction direction;

	@JsonProperty("playerId")
	private Integer playerId;

	@JsonProperty("clock")
	private Integer clock;

	public PlayerMoveCommand(Unit.Direction direction, Integer playerId, Integer clock)
	{
		this.direction = direction;
		this.playerId = playerId;
		this.clock = clock;
	}

	// For Jackson
	private PlayerMoveCommand()
	{
	}

	public Unit.Direction getDirection()
	{
		return direction;
	}

	public Integer getPlayerId()
	{
		return playerId;
	}

	public Integer getClock()
	{
		return clock;
	}
}
