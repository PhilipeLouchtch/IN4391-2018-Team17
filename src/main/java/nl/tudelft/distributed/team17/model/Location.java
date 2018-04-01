package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rits.cloning.Immutable;

import java.util.ArrayList;
import java.util.List;

@Immutable
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
}
