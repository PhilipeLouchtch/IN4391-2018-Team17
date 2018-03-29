package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;
import java.util.Random;

public class Board
{
	static final int BOARD_SIZE = 25;
	static final Unit NO_UNIT_AT_LOCATION = null;

	@JsonProperty("fields")
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

	public Unit getAt(Location location)
	{
		assertIsValid(location);

		return fields[location.getX()][location.getY()];
	}

	public synchronized Optional<Unit> placeUnitOnRandomEmptyLocation(Random random, Unit unit)
	{
		int x;
		int y;
		Location location;

		final int MAX_ATTEMPTS = 20;

		for (int i = 0; i < MAX_ATTEMPTS; i++)
		{
			x = random.nextInt(BOARD_SIZE);
			y = random.nextInt(BOARD_SIZE);
			location = new Location(x, y);

			if (!isLocationOccupied(location))
			{
				Unit placedUnit = unit.placed(location);
				setAt(location, unit);

				return Optional.of(placedUnit);
			}
		}

		return Optional.empty();
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

	private void setAt(Location location, Unit unit)
	{
		assertIsValid(location);

		fields[location.getX()][location.getY()] = unit;
	}
}
