package nl.tudelft.distributed.team17.model;

import java.util.Collection;
import java.util.List;

public class WorldState
{
	private Board board;
	private UnitsInWorld units;

	public static WorldState initial()
	{
		return new WorldState(Board.initial(), UnitsInWorld.initial());
	}

	public WorldState(Board board, UnitsInWorld units)
	{
		this.board = board;
		this.units = units;
	}

	public synchronized void movePlayer(Integer playerId, distributed.systems.das.units.Unit.Direction direction)
	{
		Unit unit = units.getPlayerUnitOrThrow(playerId);
		Unit movedUnit = unit.moved(direction);

		swapUnits(unit, movedUnit);
	}

	public synchronized void healPlayer(Integer playerId, Location locationToHeal)
	{
		Unit unit = units.getPlayerUnitOrThrow(playerId);

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
		Unit attacker = units.getUnitOrThrow(attackerId);
		int distance = attacker.getLocation().maxDistanceTo(locationToAttack);
		if (distance > 2)
		{
			throw new AttackRangeException(attacker, locationToAttack, distance);
		}

		Unit unitToAttack = board.getAt(locationToAttack);

		damageUnit(attacker, unitToAttack);
	}

	public synchronized void damageUnit(Unit attacker, Unit unitToAttack)
	{
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

	public List<Unit> playersInRangeOfUnit(Unit unit, int range)
	{
		return units.playersInRangeOfUnit(unit, range);
	}

	private synchronized void swapUnits(Unit oldUnit, Unit newUnit)
	{
		assertSameUnitId(oldUnit, newUnit);
		board.swapUnits(oldUnit, newUnit);
		units.update(newUnit);
	}

	private synchronized void removeUnitFromBoard(Unit oldUnit, Unit newUnit)
	{
		assertSameUnitId(oldUnit, newUnit);
		board.removeUnit(oldUnit);
		units.update(newUnit);
	}

	private static void assertSameUnitId(Unit unitOne, Unit unitTwo)
	{
		if(unitOne.getId().equals(unitTwo.getId()))
		{
			String message = String.format("Unit ids do not match, was: [%s] and [%s]", unitOne.getId(), unitTwo.getId());
			throw new IllegalArgumentException(message);
		}
	}

}
