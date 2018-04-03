package nl.tudelft.distributed.team17.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Random;

public class WorldStateTest
{
	WorldState worldState;

	@Before
	public void setUp() throws Exception
	{
		worldState = WorldState.initial();
	}

	@Test
	public void movePlayer()
	{
		Unit unit = worldState.spawnUnit("player", UnitType.PLAYER).get();

		for (Direction direction : Direction.values())
		{
			worldState.movePlayer(unit.getId(), direction);

			Unit movedUnit = worldState.findPlayerUnit(unit.getId()).get();
			Assert.assertNotEquals(unit, movedUnit.getLocation());
		}
	}

	@Test
	public void healPlayer()
	{
	}

	@Test
	public void damageUnit()
	{
		/* Prep work */
		UnitsInWorld unitsInWorld = worldState.getUnits();

		Field locationField = ReflectionUtils.findField(Unit.class, "location");
		ReflectionUtils.makeAccessible(locationField);

		/* Given */
		Unit player = Unit.constructRandomUnit(new Random(1), "player", UnitType.PLAYER);
		ReflectionUtils.setField(locationField, player, new Location(0,0));
		worldState.putUnitIntoWorld(player);

		Unit dragon = Unit.constructRandomUnit(new Random(1), "dragon", UnitType.DRAGON);
		ReflectionUtils.setField(locationField, dragon, new Location(1,0));
		worldState.putUnitIntoWorld(dragon);

		/* When */
		worldState.damageUnit(player, dragon);

		Unit dragonDamaged = unitsInWorld.getUnitOrThrow("dragon");

		/* Then */
		Assert.assertNotSame(dragon, dragonDamaged);
		Assert.assertNotEquals(dragon.getUnitHealth(), dragonDamaged.getUnitHealth());

		Assert.assertNotNull(dragonDamaged.getLocation());

		/* Now Test until unit dies and if that goes well... */
		while (dragonDamaged.isAlive())
		{
			worldState.damageUnit(player, dragonDamaged);
			dragonDamaged = unitsInWorld.getUnitOrThrow("dragon");
		}

		Assert.assertNull(dragonDamaged.getLocation());
		Assert.assertTrue(dragonDamaged.getUnitHealth().isEmpty());
	}

	@Test
	public void spawnUnit()
	{
	}
}