package nl.tudelft.distributed.team17.infrastructure.api.rest;

import nl.tudelft.distributed.team17.application.CommandForwarder;
import nl.tudelft.distributed.team17.application.CurrentWorldState;
import nl.tudelft.distributed.team17.application.LedgerController;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.AttackCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.HealCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.MoveCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.SpawnCommandDTO;
import nl.tudelft.distributed.team17.model.Unit;
import nl.tudelft.distributed.team17.model.command.PlayerAttackCommand;
import nl.tudelft.distributed.team17.model.command.PlayerHealCommand;
import nl.tudelft.distributed.team17.model.command.PlayerMoveCommand;
import nl.tudelft.distributed.team17.model.WorldState;
import nl.tudelft.distributed.team17.model.command.PlayerSpawnCommand;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(path = "player")
public class PlayerEndpoints
{
	private CurrentWorldState currentWorldState;
	private CommandForwarder commandForwarder;
	private LedgerController ledgerController;

	public PlayerEndpoints(CurrentWorldState currentWorldState, CommandForwarder commandForwarder, LedgerController ledgerController)
	{
		this.currentWorldState = currentWorldState;
		this.commandForwarder = commandForwarder;
		this.ledgerController = ledgerController;
	}

	@PostMapping(path = "move")
	public void move(@RequestBody MoveCommandDTO moveCommandDTO)
	{
		ledgerController.startRunning();

		PlayerMoveCommand playerMoveCommand = PlayerMoveCommand.createWithEmailAuthentication(
				moveCommandDTO.getEmailAddress(),
				moveCommandDTO.getClock(),
				moveCommandDTO.getDirection());

		currentWorldState.addCommand(playerMoveCommand);
		commandForwarder.forward(playerMoveCommand);

		// if command is for a more recent worldstate, we forward to another server via load balancer
		// if we get the same command AGAIN, we reply and the original server tries again
		// this to prevent recursion

		// push command to game / ledger (blocking op)
		// WorldState worldState = null;
		// Object callback = (state) -> worldState = state;

		// Send the command (if accepted) to other servers
	}

	@PostMapping(path = "heal")
	public void heal(@RequestBody HealCommandDTO healCommandDTO)
	{
		ledgerController.startRunning();

		PlayerHealCommand playerHealCommand = PlayerHealCommand.createWithEmailAuthentication(
				healCommandDTO.getEmailAddress(),
				healCommandDTO.getClock(),
				healCommandDTO.getLocationToHeal());

		currentWorldState.addCommand(playerHealCommand);
		commandForwarder.forward(playerHealCommand);
	}

	@PostMapping(path = "attack")
	public void attack(@RequestBody AttackCommandDTO attackCommandDTO)
	{
		ledgerController.startRunning();

		PlayerAttackCommand playerAttackCommand = PlayerAttackCommand.createWithEmailAuthentication(
				attackCommandDTO.getEmailAddress(),
				attackCommandDTO.getClock(),
				attackCommandDTO.getLocationToAttack());

		currentWorldState.addCommand(playerAttackCommand);
		commandForwarder.forward(playerAttackCommand);
	}

	@PostMapping(path = "spawn")
	public Unit spawn(@RequestBody SpawnCommandDTO spawnCommandDTO)
	{
		ledgerController.startRunning();

		PlayerSpawnCommand playerSpawnCommand = PlayerSpawnCommand.createWithEmailAuthentication(
				spawnCommandDTO.getEmailAddress(),
				spawnCommandDTO.getClock());

		// Push the command and wait for the unit to appear in a Ledger that has been accepted by the servers
		// Limit waiting to two ledgers, achieved by looking at the amount of times the worldstate changes "clock"

		int numWorldStateChangesWaited = 0;
		Integer worldStateClockBeginWait = currentWorldState.getLastCheckpoint().getWorldStateClock();

		currentWorldState.addCommand(playerSpawnCommand);
		commandForwarder.forward(playerSpawnCommand);

		while (numWorldStateChangesWaited < 2)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException ex)
			{
				Thread.currentThread().interrupt();
				throw new RuntimeException(ex);
			}

			Integer worldStateClock = currentWorldState.getLastCheckpoint().getWorldStateClock();
			if (worldStateClock > worldStateClockBeginWait)
			{
				Optional<Unit> playerUnit = currentWorldState.getLastCheckpoint().findPlayerUnit(playerSpawnCommand.getPlayerId());
				if (playerUnit.isPresent())
				{
					return playerUnit.get();
				}

				numWorldStateChangesWaited++;
			}
		}

		return null;
	}

	@PostMapping(path = "worldstate")
	public WorldState worldState()
	{
		ledgerController.startRunning();

		return currentWorldState.getLastCheckpoint();
	}
}
