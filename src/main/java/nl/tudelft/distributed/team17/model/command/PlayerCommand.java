package nl.tudelft.distributed.team17.model.command;

public abstract class PlayerCommand extends Command
{
    public PlayerCommand(String playerId, Integer clock, boolean isPriority)
    {
        super(playerId, clock, isPriority);
    }

    // Jackson
    protected PlayerCommand()
    {
    }
}
