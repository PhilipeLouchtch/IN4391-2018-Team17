package nl.tudelft.distributed.team17.model.command;

import nl.tudelft.distributed.team17.model.Unit;
import nl.tudelft.distributed.team17.model.UnitType;
import nl.tudelft.distributed.team17.model.WorldState;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;
import java.util.Optional;

public class PlayerSpawnCommand extends PlayerCommand
{
    static public PlayerSpawnCommand createWithEmailAuthentication(
        String emailAddress,
        Integer clock)
    {
        return new PlayerSpawnCommand(emailAddress, clock);
    }

    private PlayerSpawnCommand(String emailAddress, Integer clock)
    {
        super(emailAddress, clock, true);
    }

    // Jackson
    private PlayerSpawnCommand()
    {
    }

    @Override
    public void apply(WorldState worldState)
    {
        LOGGER.info(String.format("Player [%s] requests spawning", getPlayerId()));
        Optional<Unit> unit = worldState.spawnUnit(getPlayerId(), UnitType.PLAYER);

        if (unit.isPresent())
        {
            LOGGER.info(String.format("Player [%s] successfully spawned", getPlayerId()));
        }
        else
        {
            LOGGER.info(String.format("Player [%s] could not be spawned", getPlayerId()));
        }
    }

    @Override
    public byte[] getHash()
    {
        MessageDigest messageDigest = getDigestOfBase();

        return messageDigest.digest();
    }
}
