package nl.tudelft.distributed.team17.model;

import distributed.systems.das.units.Unit;

public class Location
{
	private int x;
	private int y;

	public Location(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public Location moved(Unit.Direction direction)
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

	public int maxDistanceTo(Location other)
	{
		return Math.abs(this.x - other.getX()) + Math.abs(this.y - other.getY());
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
