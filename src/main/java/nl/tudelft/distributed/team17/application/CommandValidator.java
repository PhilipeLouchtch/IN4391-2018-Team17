package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.Board;
import nl.tudelft.distributed.team17.model.Unit;
import nl.tudelft.distributed.team17.model.UnitType;
import nl.tudelft.distributed.team17.model.WorldState;
import nl.tudelft.distributed.team17.model.command.PlayerMoveCommand;
import org.springframework.stereotype.Service;

@Service
public class CommandValidator
{
	private static void assertUnitIsPlayer(Unit unit)
	{
		if (unit.getUnitType() != UnitType.PLAYER)
		{
			throw new RuntimeException("Unit is not a player");
		}
	}
}
