package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Unit
{
	@JsonProperty("unitType")
	private UnitType unitType;

	@JsonProperty("id")
	private Integer id;

	@JsonProperty("boardLocation")
	private BoardLocation boardLocation;

	@JsonProperty("unitHealth")
	private UnitHealth unitHealth;

	@JsonProperty("attackPower")
	private Integer attackPower;

	private Unit(UnitType unitType, Integer id, BoardLocation boardLocation, UnitHealth unitHealth, Integer attackPower)
	{
		this.unitType = unitType;
		this.id = id;
		this.boardLocation = boardLocation;
		this.unitHealth = unitHealth;
		this.attackPower = attackPower;
	}

	private static Unit unitWithModifiedHealth(Unit unit, UnitHealth newHealth)
	{
		return new Unit (unit.unitType, unit.id, unit.boardLocation, newHealth, unit.attackPower);
	}

	static Unit constructPlayer(Integer playerId, BoardLocation boardLocation, UnitHealth unitHealth, Integer attackPower)
	{
		return new Unit(UnitType.PLAYER, playerId, boardLocation, unitHealth, attackPower);
	}

	static Unit constructDragon(Integer playerId, BoardLocation boardLocation, UnitHealth unitHealth, Integer attackPower)
	{
		return new Unit(UnitType.DRAGON, playerId, boardLocation, unitHealth, attackPower);
	}

	public UnitType getUnitType()
	{
		return unitType;
	}

	public Integer getId()
	{
		return id;
	}

	public BoardLocation getBoardLocation()
	{
		return boardLocation;
	}

	public UnitHealth getUnitHealth()
	{
		return unitHealth;
	}

	public Integer getAttackPower()
	{
		return attackPower;
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
}
