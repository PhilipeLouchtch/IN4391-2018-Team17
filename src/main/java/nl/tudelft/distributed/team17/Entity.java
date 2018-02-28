package nl.tudelft.distributed.team17;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Entity
{
	@JsonProperty("data")
	String data;

	@JsonProperty("name")
	String name;

	private Entity()
	{
	}

	public Entity(String data, String name)
	{
		this.data = data;
		this.name = name;
	}

	public String getData()
	{
		return data;
	}

	public String getName()
	{
		return name;
	}
}
