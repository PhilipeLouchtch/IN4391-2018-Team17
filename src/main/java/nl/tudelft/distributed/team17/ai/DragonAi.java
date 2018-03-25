package nl.tudelft.distributed.team17.ai;

import nl.tudelft.distributed.team17.model.Unit;
import nl.tudelft.distributed.team17.model.UnitType;
import nl.tudelft.distributed.team17.model.WorldState;
import nl.tudelft.distributed.team17.model.command.DragonAttackCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
    Usage in Ledger:
        - reseed(hashOfPreviousLedger)
        - doesDragonPerformAction() -> no do nothing
        - generateDragonAttackCommand(currentLedgerClock, consideredWorldState, dragonUnit) -> and add it to the ledger
**/
@Component
public class DragonAi
{
    private static final int CHANCE_TO_DO_ACTION = 10; // 1 in 10
    private Random random;

    private DragonAi(Random random)
    {
        this.random = random;
    }

    @Autowired
    public DragonAi()
    {
        this(new Random(1));
    }

    public void reseed(long seed)
    {
        random.setSeed(seed);
    }

    public boolean doesDragonPerformAction()
    {
        return chance(CHANCE_TO_DO_ACTION);
    }

    public DragonAttackCommand generateDragonAttackCommand(Integer clock, WorldState worldState, Unit unit)
    {
        assertUnitIsDragon(unit);

        List<Unit> attackablePlayers = worldState.playersInRangeOfUnit(unit, 2);
        Unit unitToAttack = pickRandomUnitFrom(attackablePlayers);
        DragonAttackCommand dragonAttackCommand =
                DragonAttackCommand.createDragonAttackCommand(unit, clock, unitToAttack);
        return dragonAttackCommand;
    }

    private Unit pickRandomUnitFrom(List<Unit> units)
    {
        int size = units.size();
        int index = random.nextInt(size);
        return units.get(index);
    }

    private boolean chance(int oneInX)
    {
        return random.nextInt(oneInX) == 0;
    }

    static private void assertUnitIsDragon(Unit dragon)
    {
        if(dragon.getUnitType() == UnitType.DRAGON)
        {
            throw new IllegalArgumentException(String.format("Expected unit DRAGON, got [%s]", dragon.getUnitType()));
        }
    }
}
