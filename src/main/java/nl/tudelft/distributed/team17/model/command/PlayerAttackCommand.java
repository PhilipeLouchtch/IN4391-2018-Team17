package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.BoardLocation;
import nl.tudelft.distributed.team17.model.WorldState;

public class PlayerAttackCommand extends PlayerCommand
{
	@JsonProperty("playerId")
	private Integer playerId;

	@JsonProperty("boardLocation")
	private BoardLocation boardLocation;

	@JsonProperty("clock")
	private Integer clock;

	public PlayerAttackCommand(Integer playerId, BoardLocation boardLocation, Integer clock)
	{
		this.playerId = playerId;
		this.boardLocation = boardLocation;
		this.clock = clock;
	}

	// Jackson
	private PlayerAttackCommand()
	{
	}

	public BoardLocation getBoardLocation()
	{
		return boardLocation;
	}

	@Override
	public WorldState apply(WorldState worldState)
	{
		return null;
	}
}