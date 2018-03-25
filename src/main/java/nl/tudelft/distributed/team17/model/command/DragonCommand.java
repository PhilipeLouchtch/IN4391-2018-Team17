package nl.tudelft.distributed.team17.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.model.Unit;

public abstract class DragonCommand extends Command
{
    @JsonProperty("dragon")
    private Unit dragon;

    public DragonCommand(Unit dragon, Integer clock)
    {
        super(dragon.getId(), clock, true);
        this.dragon = dragon;
    }

    // Jackson
    protected DragonCommand()
    {
    }

    public Unit getDragon()
    {
        return dragon;
    }
}
