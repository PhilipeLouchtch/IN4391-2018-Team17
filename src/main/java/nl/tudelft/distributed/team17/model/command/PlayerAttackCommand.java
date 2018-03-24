package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.BoardLocation;

public class PlayerAttackCommand
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

	public Integer getPlayerId()
	{
		return playerId;
	}

	public BoardLocation getBoardLocation()
	{
		return boardLocation;
	}

	public Integer getClock()
	{
		return clock;
	}
}
