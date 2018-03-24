package nl.tudelft.distributed.team17.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WorldState
{
	private Board board;
	private Map<Integer, Unit> units;

	public Optional<Unit> findUnit(Integer unitId)
	{
		return Optional.of(units.get(unitId));
	}

	public Board getBoard()
	{
		return board;
	}
}
