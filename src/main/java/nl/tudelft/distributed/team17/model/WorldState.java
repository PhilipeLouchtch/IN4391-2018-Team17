package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class WorldState
{
	@JsonProperty("boards")
	private Board board;

	@JsonProperty("units")
	private UnitsInWorld units;

	@JsonProperty("worldStateClock")
	private Integer worldStateClock;

	public static WorldState initial()
	{
		final int INITIAL_WORLDSTATE_CLOCK = 0;
		return new WorldState(Board.initial(), UnitsInWorld.initial(), INITIAL_WORLDSTATE_CLOCK);
	}

	public WorldState(Board board, UnitsInWorld units, Integer worldStateClock)
	{
		this.board = board;
		this.units = units;
		this.worldStateClock = worldStateClock;
	}

	// JACKSON
	@JsonCreator
	private WorldState()
	{
	}

	public synchronized boolean isUnitDead(String unitId)
	{
		Unit unit = units.getUnitOrThrow(unitId);
		return unit.isDead();
	}

	public synchronized void movePlayer(String playerId, Direction direction)
	{
		Unit unit = units.getPlayerUnitOrThrow(playerId);
		assertUnitIsAlive(unit);

		Unit movedUnit = unit.moved(direction);
		swapUnits(unit, movedUnit);

		incrementClock();
	}

	public synchronized void healPlayer(String playerId, Location locationToHeal)
	{
		Unit unit = units.getPlayerUnitOrThrow(playerId);
		assertUnitIsAlive(unit);

		int distance = unit.getLocation().maxDistanceTo(locationToHeal);
		if (distance > 5)
		{
			throw new HealRangeException(unit, locationToHeal, distance);
		}

		Unit unitToHeal = board.getAt(locationToHeal);
		Unit healedUnit = unitToHeal.incurHeal(unit.getAttackPower());
		swapUnits(unitToHeal, healedUnit);

		incrementClock();
	}

	public synchronized void damageUnit(String attackerId, Location locationToAttack)
	{
		Unit attacker = units.getUnitOrThrow(attackerId);
		assertUnitIsAlive(attacker);

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
		assertUnitIsAlive(attacker);

		Unit attackedUnit = unitToAttack.incurDamage(attacker.getAttackPower());

		if(attackedUnit.isDead())
		{
			removeUnitFromBoard(unitToAttack, attackedUnit);
		}
		else
		{
			swapUnits(unitToAttack, attackedUnit);
		}

		incrementClock();
	}

	public synchronized Optional<Unit> spawnUnit(String unitId, UnitType unitType)
	{
		// ensure that randomness is determinisitc
		Random random = new Random(unitId.hashCode());

		Unit createdUnit = Unit.constructRandomUnit(random, unitId, unitType);
		Optional<Unit> spawnedUnit = board.placeUnitOnRandomEmptyLocation(random, createdUnit);

		spawnedUnit.ifPresent(units::addUnit);
		incrementClock();

		return spawnedUnit;
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
		if(!unitOne.getId().equals(unitTwo.getId()))
		{
			String message = String.format("Unit ids do not match, was: [%s] and [%s]", unitOne.getId(), unitTwo.getId());
			throw new IllegalArgumentException(message);
		}
	}

	private void assertUnitIsAlive(Unit unit)
	{
		if (unit.isDead())
		{
			String msg = String.format("Cannot complete requested action, unit is dead (unit was: [%s])", unit.getId());
			throw new IllegalStateException(msg);
		}
	}

	public Optional<Unit> findPlayerUnit(String playerId)
	{
		return units.findPlayerUnit(playerId);
	}

	public boolean playerUnitInGame(String playerId)
	{
		Optional<Unit> player = units.findUnit(playerId);
		return player.isPresent();
	}

	public Integer getWorldStateClock()
	{
		return worldStateClock;
	}

	public Optional<Unit> getClosestDragonToUnit(String unitId)
	{
		Unit unit = units.getPlayerUnitOrThrow(unitId);
		Optional<Unit> closestDragon = units.closestDragonTo(unit);
		return closestDragon;
	}

	public boolean locationOccupied(Location location)
	{
		return board.isLocationOccupied(location);
	}

	public boolean anyDragonsLeft()
	{
		return units.anyDragonLeft();
	}

	public void incrementClock()
	{
		worldStateClock++;
	}
}
