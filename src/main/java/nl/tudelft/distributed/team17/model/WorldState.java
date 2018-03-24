package nl.tudelft.distributed.team17.model;

import nl.tudelft.distributed.team17.application.NoSuchUnitExistsException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WorldState
{
	private Board board;
	private Map<Integer, Unit> units;

	public static WorldState initial()
	{
		return new WorldState(Board.initial(), new HashMap<>());
	}

	public WorldState(Board board, Map<Integer, Unit> units)
	{
		this.board = board;
		this.units = units;
	}

	public Optional<Unit> findUnit(Integer unitId)
	{
		return Optional.of(units.get(unitId));
	}

	public Unit getPlayerUnitOrThrow(Integer playerId)
	{
		Unit unit = findUnit(playerId).orElseThrow(() -> new NoSuchUnitExistsException(playerId));
		assertUnitIsPlayer(unit);

		return unit;
	}

	public synchronized void movePlayer(Integer playerId, distributed.systems.das.units.Unit.Direction direction)
	{
		Unit unit = getPlayerUnitOrThrow(playerId);

		//TODO: ensure valid location

		Unit movedUnit = unit.moved(direction);

		swapUnits(unit, movedUnit);
	}

	public synchronized void healPlayer(Integer playerId, Location locationToHeal)
	{
		Unit unit = getPlayerUnitOrThrow(playerId);

		int distance = unit.getLocation().maxDistanceTo(locationToHeal);
		if (distance > 5)
		{
			throw new HealRangeException(unit, locationToHeal, distance);
		}

		Unit unitToHeal = board.getAt(locationToHeal);
		Unit healedUnit = unitToHeal.incurHeal(unit.getAttackPower());

		swapUnits(unitToHeal, healedUnit);
	}

	private synchronized void swapUnits(Unit oldUnit, Unit newUnit)
	{
		board.swapUnits(oldUnit, newUnit);
		units.replace(newUnit.getId(), newUnit);
	}

	private static void assertUnitIsPlayer(Unit unit)
	{
		if (unit.getUnitType() != UnitType.PLAYER)
		{
			throw new RuntimeException("Unit is not a player");
		}
	}
}
