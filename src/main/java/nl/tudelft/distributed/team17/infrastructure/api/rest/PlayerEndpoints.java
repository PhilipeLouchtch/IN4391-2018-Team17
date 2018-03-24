package nl.tudelft.distributed.team17.infrastructure.api.rest;

import nl.tudelft.distributed.team17.model.command.PlayerAttackCommand;
import nl.tudelft.distributed.team17.model.command.PlayerHealCommand;
import nl.tudelft.distributed.team17.model.command.PlayerMoveCommand;
import nl.tudelft.distributed.team17.model.WorldState;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "player")
public class PlayerEndpoints
{
	@PostMapping(path = "move")
	public WorldState move(@RequestBody PlayerMoveCommand playerMoveCommand)
	{
		// if command is for a more recent worldstate, we forward to another server via load balancer
		// if we get the same command AGAIN, we reply and the original server tries again
		// this to prevent recursion

		// push command to game / ledger (blocking op)

		// return the new world state
		return null;
	}

	@PostMapping(path = "heal")
	public WorldState heal(@RequestBody PlayerHealCommand playerHealCommand)
	{
		// push command to game

		// return the new world state
		return null;
	}

	@PostMapping(path = "attack")
	public WorldState attack(@RequestBody PlayerAttackCommand playerAttackCommand)
	{
		// push command to game

		// return the new world state
		return null;
	}
}
