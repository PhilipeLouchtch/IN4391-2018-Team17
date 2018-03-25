package nl.tudelft.distributed.team17.service;

import nl.tudelft.distributed.team17.model.WorldState;
import org.springframework.stereotype.Service;

@Service
public class WorldStateGenerator
{
	public WorldState generateNewWorldState()
	{
		WorldState initialWorldState = WorldState.initial();

		// TODO: actually generate the world
		return initialWorldState;
	}
}
