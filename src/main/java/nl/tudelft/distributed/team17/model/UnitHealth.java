package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rits.cloning.Immutable;

@Immutable
public class UnitHealth
{
	@JsonProperty("current")
	private int current;

	@JsonProperty("maximum")
	private int maximum;

	public UnitHealth(int current, int maximum)
	{
		if (current < 0)
		{
			throw new RuntimeException("Unit health cannot be lower than zero");
		}

		this.current = current;
		this.maximum = maximum;
	}

	@JsonCreator
	private UnitHealth()
	{
	}

	public UnitHealth damaged(int damage)
	{
		int newHealth = Math.max(current - damage, 0);
		return new UnitHealth(newHealth, maximum);
	}

	public UnitHealth healed(int heal)
	{
		int newHealth = Math.min(current + heal, maximum);
		return new UnitHealth(newHealth, maximum);
	}

	public boolean isEmpty()
	{
		return current == 0;
	}

	public boolean halfHealthOrLess()
	{
		return current/maximum <= 1/2;
	}
}
