package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.Board;
import nl.tudelft.distributed.team17.model.Unit;
import nl.tudelft.distributed.team17.model.UnitType;
import nl.tudelft.distributed.team17.model.WorldState;
import nl.tudelft.distributed.team17.model.command.PlayerMoveCommand;

import java.util.Optional;

public class CommandValidator
{
	public void validate(PlayerMoveCommand playerMoveCommand, WorldState worldState)
	{
		Integer playerId = playerMoveCommand.getPlayerId();
		Unit unit = worldState.findUnit(playerId).orElseThrow(CommandValidator::noSuchUnitExistsException);

		assertUnitIsPlayer(unit);

		Board board = worldState.getBoard();
	}

	private static RuntimeException noSuchUnitExistsException()
	{
		return new RuntimeException("No such unit exists");
	}

	private static void assertUnitIsPlayer(Unit unit)
	{
		if (unit.getUnitType() != UnitType.PLAYER) {
			throw new RuntimeException("Unit is not a player");
		}
	}
}
