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
		Unit unit = getUnitOrThrow(playerId);
		assertUnitIsPlayer(unit);

		return unit;
	}

	public Unit getUnitOrThrow(Integer unitId)
	{
		Unit unit = findUnit(unitId).orElseThrow(() -> new NoSuchUnitExistsException(unitId));

		return unit;
	}

	public synchronized void movePlayer(Integer playerId, distributed.systems.das.units.Unit.Direction direction)
	{
		Unit unit = getPlayerUnitOrThrow(playerId);
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

	public synchronized void damageUnit(Integer attackerId, Location locationToAttack)
	{
		Unit attacker = getUnitOrThrow(attackerId);

		int distance = attacker.getLocation().maxDistanceTo(locationToAttack);
		if (distance > 2)
		{
			throw new AttackRangeException(attacker, locationToAttack, distance);
		}

		Unit unitToAttack = board.getAt(locationToAttack);
		Unit attackedUnit = unitToAttack.incurDamage(attacker.getAttackPower());

		if(attackedUnit.isDead())
		{
			removeUnitFromBoard(unitToAttack, attackedUnit);
		}
		else
		{
			swapUnits(unitToAttack, attackedUnit);
		}
	}

	private synchronized void swapUnits(Unit oldUnit, Unit newUnit)
	{
		assertSameUnitId(oldUnit, newUnit);
		board.swapUnits(oldUnit, newUnit);
		units.replace(newUnit.getId(), newUnit);
	}

	private synchronized void removeUnitFromBoard(Unit oldUnit, Unit newUnit)
	{
		assertSameUnitId(oldUnit, newUnit);
		board.removeUnit(oldUnit);
		units.replace(newUnit.getId(), newUnit);
	}

	private static void assertSameUnitId(Unit unitOne, Unit unitTwo)
	{
		if(unitOne.getId().equals(unitTwo.getId()))
		{
			String message = String.format("Unit ids do not match, was: [%s] and [%s]", unitOne.getId(), unitTwo.getId());
			throw new IllegalArgumentException(message);
		}
	}

	private static void assertUnitIsPlayer(Unit unit)
	{
		if (unit.getUnitType() != UnitType.PLAYER)
		{
			throw new RuntimeException("Unit is not a player");
		}
	}
}
