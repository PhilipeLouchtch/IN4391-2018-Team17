package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rits.cloning.Immutable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Immutable
@JsonAutoDetect(isGetterVisibility = NONE, fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE)
public class Location
{
	@JsonProperty("x")
	private int x;
	@JsonProperty("y")
	private int y;

	static public final Location INVALID_LOCATION = null;

	public Location(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	// JACKSON
	@JsonCreator
	private Location()
	{
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public Location moved(Direction direction)
	{
		int x = this.x, y = this.y;

		switch (direction)
		{
			case up:
				y++;
				break;

			case right:
				x++;
				break;

			case down:
				y--;
				break;

			case left:
				x--;
				break;

			default:
				throw new IllegalArgumentException(String.format("No such direction supported, was: [%s]", direction));
		}

		return new Location(x, y);
	}

	public int distanceTo(Location other)
	{
		return Math.abs(this.x - other.getX()) + Math.abs(this.y - other.getY());
	}

	public List<Direction> getMoveDirectionsTowards(Location targetLocation)
	{
		List<Direction> moveDirections = new ArrayList<>();
		Integer deltaX = getX() - targetLocation.getX();
		Integer deltaY = getY() - targetLocation.getY();

		if(deltaX > 0)
		{
			moveDirections.add(Direction.left);
		}
		else if(deltaX < 0)
		{
			moveDirections.add(Direction.right);
		}

		if(deltaY > 0)
		{
			moveDirections.add(Direction.down);
		}
		else if(deltaY < 0)
		{
			moveDirections.add(Direction.up);
		}

		return moveDirections;
	}

	@Override
	public String toString()
	{
		return "Location{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	public byte[] getHash()
	{
		MessageDigest messageDigest = new DigestUtils(MessageDigestAlgorithms.SHA_256).getMessageDigest();
		messageDigest = DigestUtils.updateDigest(messageDigest, ByteBuffer.allocate(4).putInt(x));
		messageDigest = DigestUtils.updateDigest(messageDigest, ByteBuffer.allocate(4).putInt(y));

		return messageDigest.digest();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		Location location = (Location) o;
		return x == location.x &&
				y == location.y;
	}
}
