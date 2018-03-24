package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	private UnitHealth()
	{
	}

	UnitHealth damaged(int damage)
	{
		int newHealth = Math.max(current - damage, 0);
		return new UnitHealth(newHealth, maximum);
	}

	UnitHealth healed(int heal)
	{
		int newHealth = Math.min(current + heal, maximum);
		return new UnitHealth(newHealth, maximum);
	}
}
