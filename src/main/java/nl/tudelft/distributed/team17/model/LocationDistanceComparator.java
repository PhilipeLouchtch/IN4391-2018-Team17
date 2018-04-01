package nl.tudelft.distributed.team17.model;

import java.util.Comparator;

public class LocationDistanceComparator implements Comparator<Unit>
{
    private Location comparedLocation;

    public LocationDistanceComparator(Location comparedLocation)
    {
        this.comparedLocation = comparedLocation;
    }

    @Override
    public int compare(Unit t1, Unit t2)
    {
        Integer distance1 = comparedLocation.distanceTo(t1.getLocation());
        Integer distance2 = comparedLocation.distanceTo(t2.getLocation());

        return distance1.compareTo(distance2);
    }
}
