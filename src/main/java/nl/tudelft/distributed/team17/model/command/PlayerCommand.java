package nl.tudelft.distributed.team17.model.command;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;

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

    @Override
    protected MessageDigest getDigestOfBase()
    {
        return super.getDigestOfBase();
    }
}
