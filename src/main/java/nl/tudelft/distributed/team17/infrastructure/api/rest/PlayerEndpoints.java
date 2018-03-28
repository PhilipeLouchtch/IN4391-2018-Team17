package nl.tudelft.distributed.team17.infrastructure.api.rest;

import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.AttackCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.HealCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.MoveCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.SpawnCommandDTO;
import nl.tudelft.distributed.team17.model.Unit;
import nl.tudelft.distributed.team17.model.command.PlayerAttackCommand;
import nl.tudelft.distributed.team17.model.command.PlayerHealCommand;
import nl.tudelft.distributed.team17.model.command.PlayerMoveCommand;
import nl.tudelft.distributed.team17.model.WorldState;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "player")
public class PlayerEndpoints
{
	@PostMapping(path = "move")
	public WorldState move(@RequestBody MoveCommandDTO moveCommandDTO)
	{
		PlayerMoveCommand playerMoveCommand = PlayerMoveCommand.createWithEmailAuthentication(
				moveCommandDTO.getEmailAddress(),
				moveCommandDTO.getClock(),
				moveCommandDTO.getDirection());

		// if command is for a more recent worldstate, we forward to another server via load balancer
		// if we get the same command AGAIN, we reply and the original server tries again
		// this to prevent recursion

		// push command to game / ledger (blocking op)
		// WorldState worldState = null;
		// Object callback = (state) -> worldState = state;

		// Send the command (if accepted) to other servers

		//spin/sleep until worldstate is set

		// return the new world state
		return null;
	}

	@PostMapping(path = "heal")
	public WorldState heal(@RequestBody HealCommandDTO healCommandDTO)
	{
		PlayerHealCommand playerHealCommand = PlayerHealCommand.createWithEmailAuthentication(
				healCommandDTO.getEmailAddress(),
				healCommandDTO.getClock(),
				healCommandDTO.getLocationToHeal());
		// push command to game

		// return the new world state
		return null;
	}

	@PostMapping(path = "attack")
	public WorldState attack(@RequestBody AttackCommandDTO attackCommandDTO)
	{
		PlayerAttackCommand playerAttackCommand = PlayerAttackCommand.createWithEmailAuthentication(
				attackCommandDTO.getEmailAddress(),
				attackCommandDTO.getClock(),
				attackCommandDTO.getLocationToAttack());
		// push command to game

		// return the new world state
		return null;
	}

	@PostMapping(path = "spawn")
	public Unit spawn(@RequestBody SpawnCommandDTO spawnCommandDTO)
	{

		return null;
	}
}
