package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.WorldState;
import nl.tudelft.distributed.team17.model.command.PlayerAttackCommand;
import nl.tudelft.distributed.team17.model.command.PlayerCommand;
import nl.tudelft.distributed.team17.model.command.PlayerHealCommand;
import nl.tudelft.distributed.team17.model.command.PlayerMoveCommand;

import java.util.ArrayList;
import java.util.List;

public class Ledger
{
    transient WorldState worldStateWithAppliedCommands;
    private Ledger previous;
    //@SpringSomething?
    private CommandValidator commandValidator;

    public Ledger(Ledger previous, WorldState worldState)
    {
        this.previous = previous;
        this.worldStateWithAppliedCommands = worldState;

        validCommands = new ArrayList<>();
    }

    private List<PlayerCommand> validCommands;

    public boolean applyCommand(PlayerCommand playerCommand)
    {

    }
}