package nl.tudelft.distributed.team17.service;

import nl.tudelft.distributed.team17.model.UnitType;
import nl.tudelft.distributed.team17.model.WorldState;
import org.springframework.stereotype.Service;

@Service
public class WorldStateGenerator
{
	public WorldState generateNewWorldState()
	{
		WorldState initialWorldState = WorldState.initial();

		initialWorldState.spawnUnit("henk", UnitType.DRAGON);
		initialWorldState.spawnUnit("jan", UnitType.DRAGON);
		initialWorldState.spawnUnit("klaas", UnitType.DRAGON);
		initialWorldState.spawnUnit("albert", UnitType.DRAGON);
		initialWorldState.spawnUnit("bob", UnitType.DRAGON);

		// TODO: actually generate the world
		return initialWorldState;
	}
}
