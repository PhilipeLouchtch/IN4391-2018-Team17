package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rits.cloning.Immutable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import java.security.MessageDigest;
import java.util.Random;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Immutable
@JsonAutoDetect(isGetterVisibility = NONE, fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE)
public class Unit
{
	static private final int DRAGON_LOWER_AP_BOUND = 5;
	static private final int DRAGON_UPPER_AP_BOUND = 20;
	static private final int DRAGON_LOWER_HEALTH_BOUND = 50;
	static private final int DRAGON_UPPER_HEALTH_BOUND = 100;

	static private final int PLAYER_LOWER_AP_BOUND = 1;
	static private final int PLAYER_UPPER_AP_BOUND = 10;
	static private final int PLAYER_LOWER_HEALTH_BOUND = 10	;
	static private final int PLAYER_UPPER_HEALTH_BOUND = 20;

	@JsonProperty("unitType")
	private UnitType unitType;

	@JsonProperty("id")
	private String id;

	@JsonProperty("location")
	private Location location;

	@JsonProperty("unitHealth")
	private UnitHealth unitHealth;

	@JsonProperty("attackPower")
	private Integer attackPower;

	// JACKSON
	@JsonCreator
	private Unit()
	{
	}

	private Unit(UnitType unitType, String id, Location location, UnitHealth unitHealth, Integer attackPower)
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

	public static Unit constructRandomUnit(Random random, String unitId, UnitType unitType)
	{
		switch(unitType)
		{
			case DRAGON:
				return constructRandomDragon(random, unitId);
			case PLAYER:
				return constructRandomPlayer(random, unitId);
			default:
				throw new IllegalArgumentException(String.format("UnitType: [%s] is not supported", unitType));
		}
	}

	static private Unit constructRandomPlayer(Random random, String playerId)
	{
		return constructRandomUnit(random, playerId, UnitType.PLAYER,
				PLAYER_LOWER_AP_BOUND, PLAYER_UPPER_AP_BOUND, PLAYER_LOWER_HEALTH_BOUND, PLAYER_UPPER_HEALTH_BOUND);
	}

	private static Unit constructRandomDragon(Random random, String dragonId)
	{
		return constructRandomUnit(random, dragonId, UnitType.DRAGON,
				DRAGON_LOWER_AP_BOUND, DRAGON_UPPER_AP_BOUND, DRAGON_LOWER_HEALTH_BOUND, DRAGON_UPPER_HEALTH_BOUND);
	}

	private static Unit constructRandomUnit(Random random, String unitId, UnitType unitType,
											int lowerApBound, int upperApBound, int lowerHealthBound, int upperHealthBound)
	{
		int ap = getRandomValueBetween(random, lowerApBound, upperApBound);
		int health = getRandomValueBetween(random, lowerHealthBound, upperHealthBound);
		UnitHealth unitHealth = new UnitHealth(health, health);
		return new Unit(unitType, unitId, Location.INVALID_LOCATION, unitHealth, ap);
	}

	private static int getRandomValueBetween(Random random, int lowerBound, int upperBound)
	{
		return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
	}

	public Unit moved(Direction direction)
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

	public Unit placed(Location location)
	{
		return unitMoved(this, location);
	}

	public boolean isDead()
	{
		return getUnitHealth().isEmpty();
	}

	public UnitType getUnitType()
	{
		return unitType;
	}

	public String getId()
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

	public byte[] getHash()
	{
		MessageDigest messageDigest = new DigestUtils(MessageDigestAlgorithms.SHA_256).getMessageDigest();
		messageDigest = DigestUtils.updateDigest(messageDigest, id);
		messageDigest = DigestUtils.updateDigest(messageDigest, unitHealth.getHash());
		if (location != Location.INVALID_LOCATION)
		{
			messageDigest = DigestUtils.updateDigest(messageDigest, location.getHash());
		}

		return messageDigest.digest();
	}
}
