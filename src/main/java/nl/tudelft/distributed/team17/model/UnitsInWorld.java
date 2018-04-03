package nl.tudelft.distributed.team17.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnitsInWorld
{
    @JsonProperty("units")
    private Map<String, Unit> units;

    private UnitsInWorld(Map<String, Unit> units)
    {
        this.units = units;
    }

    // JACKSON
    @JsonCreator
    private UnitsInWorld()
    {
    }

    public static UnitsInWorld initial()
    {
        return new UnitsInWorld(new HashMap<>());
    }

    public Optional<Unit> findUnit(String unitId)
    {
        return Optional.ofNullable(units.get(unitId));
    }

    public Optional<Unit> findPlayerUnit(String unitId)
    {
        Unit unit = units.get(unitId);

        if (unit == null || unit.getUnitType() == UnitType.DRAGON)
        {
            return Optional.empty();
        }

        return Optional.of(unit);
    }

    public Unit getUnitOrThrow(String unitId)
    {
        Unit unit = findUnit(unitId).orElseThrow(() -> new NoSuchUnitExistsException(unitId));

        return unit;
    }

    public void addUnit(Unit unit)
    {
        units.put(unit.getId(), unit);
    }

    public Unit getPlayerUnitOrThrow(String playerId)
    {
        Unit unit = getUnitOrThrow(playerId);
        assertUnitIsPlayer(unit);

        return unit;
    }

    public void update(Unit unit)
    {
        units.replace(unit.getId(), unit);
    }

    public List<Unit> playersInRangeOfUnit(Unit unit, int range)
    {
        assertValidRange(range);

        Location unitLocation = unit.getLocation();

        Predicate<Unit> filterCondition = (value) ->
        {
           boolean isAlive = value.isAlive();
           boolean isPlayer = value.getUnitType() == UnitType.PLAYER;
           boolean isInRange = value.getLocation().distanceTo(unitLocation) <= range;
           boolean isNotSameUnit = value != unit;

           return isAlive && isPlayer && isInRange && isNotSameUnit;
        };

        return units.values().stream()
                .filter(filterCondition)
                .collect(Collectors.toList());
    }

    public boolean anyDragonLeft()
    {
        return units.values().stream()
                .anyMatch(t -> t.getUnitType() == UnitType.DRAGON && t.isAlive());
    }

    public Optional<Unit> closestDragonTo(Unit unit)
    {
        Location unitLocation = unit.getLocation();
        LocationDistanceComparator comparator = new LocationDistanceComparator(unitLocation);

        return units.values().stream()
                .filter(t -> t.isAlive() && t.getUnitType() == UnitType.DRAGON)
                .min(comparator);
    }

    private static void assertValidRange(int range)
    {
        if (range <= 0)
        {
            throw new IllegalArgumentException(String.format("Expected range > 0, got [%s]", range));
        }
    }

    private static void assertUnitIsPlayer(Unit unit)
    {
        if (unit.getUnitType() != UnitType.PLAYER)
        {
            throw new RuntimeException("Unit is not a player");
        }
    }
}
