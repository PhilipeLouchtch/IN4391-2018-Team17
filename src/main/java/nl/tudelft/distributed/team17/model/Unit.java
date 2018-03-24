package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Unit
{
	@JsonProperty("unitType")
	private UnitType unitType;

	@JsonProperty("id")
	private Integer id;

	@JsonProperty("location")
	private Location location;

	@JsonProperty("unitHealth")
	private UnitHealth unitHealth;

	@JsonProperty("attackPower")
	private Integer attackPower;

	private Unit(UnitType unitType, Integer id, Location location, UnitHealth unitHealth, Integer attackPower)
	{
		this.unitType = unitType;
		this.id = id;
		this.location = location;
		this.unitHealth = unitHealth;
		this.attackPower = attackPower;
	}

	private static Unit unitWithModifiedHealth(Unit unit, UnitHealth newHealth)
	{
		return new Unit(unit.unitType, unit.id, unit.location, newHealth, unit.attackPower);
	}

	private static Unit unitMoved(Unit unit, Location location)
	{
		return new Unit(unit.unitType, unit.id, location, unit.unitHealth, unit.attackPower);
	}

	static Unit constructPlayer(Integer playerId, Location location, UnitHealth unitHealth, Integer attackPower)
	{
		return new Unit(UnitType.PLAYER, playerId, location, unitHealth, attackPower);
	}

	static Unit constructDragon(Integer playerId, Location location, UnitHealth unitHealth, Integer attackPower)
	{
		return new Unit(UnitType.DRAGON, playerId, location, unitHealth, attackPower);
	}

	public Unit moved(distributed.systems.das.units.Unit.Direction direction)
	{
		Location newLocation = location.moved(direction);
		return unitMoved(this, newLocation);
	}

	public Unit incurDamage(int damage)
	{
		UnitHealth newHealth = this.unitHealth.damaged(damage);
		return unitWithModifiedHealth(this, newHealth);
	}

	public Unit incurHeal(int heal)
	{
		UnitHealth newHealth = this.unitHealth.healed(heal);
		return unitWithModifiedHealth(this, newHealth);
	}

	public Unit attack(Unit unitUnderAttack)
	{
		return unitUnderAttack.incurDamage(this.attackPower);
	}

	public UnitType getUnitType()
	{
		return unitType;
	}

	public Integer getId()
	{
		return id;
	}

	public Location getLocation()
	{
		return location;
	}

	public UnitHealth getUnitHealth()
	{
		return unitHealth;
	}

	public Integer getAttackPower()
	{
		return attackPower;
	}
}
