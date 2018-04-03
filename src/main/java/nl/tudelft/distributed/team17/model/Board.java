package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonCreator;
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

	// JACKSON
	@JsonCreator
	private Board()
	{
	}

	public boolean isValidLocation(Location location)
	{
		if (location == Location.INVALID_LOCATION)
		{
			return false;
		}

		int x = location.getX(), y = location.getY();

		return (x < fields.length) && (y < fields.length) && (x >= 0) && (y >= 0);
	}

	public synchronized boolean isLocationOccupied(Location location)
	{
		return getAt(location).isPresent();
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

	public synchronized Optional<Unit> getAt(Location location)
	{
		assertIsValid(location);

		Unit unit = fields[location.getX()][location.getY()];

		if (unit == NO_UNIT_AT_LOCATION) {
			return Optional.empty();
		}

		return Optional.of(unit);
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
				setAt(location, placedUnit);

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
