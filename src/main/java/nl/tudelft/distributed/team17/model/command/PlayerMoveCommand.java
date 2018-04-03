package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Direction;
import nl.tudelft.distributed.team17.model.WorldState;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

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
		LOGGER.info(String.format("Player [%s] tries to move in direction %s", getPlayerId(), getDirection()));
		assertUnitAlive(worldState);
		worldState.movePlayer(getPlayerId(), getDirection());
		LOGGER.info(String.format("Player [%s] successfully moved in direction %s", getPlayerId(), getDirection()));
	}

	@Override
	public byte[] getHash()
	{
		MessageDigest messageDigest = getDigestOfBase();
		messageDigest = DigestUtils.updateDigest(messageDigest, ByteBuffer.allocate(4).putInt(direction.ordinal()));

		return messageDigest.digest();
	}
}
