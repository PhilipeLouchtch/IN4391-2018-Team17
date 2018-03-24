package nl.tudelft.distributed.team17.model;

public class Board
{
	static final int BOARD_SIZE = 25;
	static final Unit NO_UNIT_AT_LOCATION = null;

	private Unit[][] fields;

	private Board(int dimension)
	{
		this.fields = new Unit[dimension][dimension];
	}

	public static Board initial()
	{
		return new Board(BOARD_SIZE);
	}

	public boolean isValidLocation(Location location)
	{
		int x = location.getX(), y = location.getY();

		return (x < fields.length) && (y < fields.length) && (0 <= x) && (0 <= y);
	}

	public boolean isLocationOccupied(Location location)
	{
		Unit unitAtLocation = getAt(location);
		if (unitAtLocation == NO_UNIT_AT_LOCATION)
		{
			return false;
		}

		return true;
	}

	public synchronized void removeUnit(Unit unit)
	{
		setAt(unit.getLocation(), NO_UNIT_AT_LOCATION);
	}

	public synchronized void placeUnit(Unit unit)
	{
		setAt(unit.getLocation(), unit);
	}

	public synchronized void swapUnits(Unit oldUnit, Unit newUnit)
	{
		Location newLocation = newUnit.getLocation();

		assertIsValid(newLocation);
		assertNotOccupied(newLocation);

		// out with the old, in with the new
		removeUnit(oldUnit);
		placeUnit(newUnit);
	}

	private void assertNotOccupied(Location newLocation)
	{
		if (isLocationOccupied(newLocation))
		{
			throw new OccupiedLocationException(newLocation);
		}
	}

	private void assertIsValid(Location newLocation)
	{
		if (!isValidLocation(newLocation))
		{
			throw new InvalidLocationException(newLocation);
		}
	}

	Unit getAt(Location location)
	{
		assertIsValid(location);

		return fields[location.getX()][location.getY()];
	}

	private void setAt(Location location, Unit unit)
	{
		assertIsValid(location);

		fields[location.getX()][location.getY()] = unit;
	}
}
