package org.geolatte.geom.crs;

/**
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
public class VerticalStraightLineAxis extends StraightLineAxis {
    /**
     * Creates an instance.
     *
     * @param axisName                      the name for this axis
     * @param unit                          the unit of this axis
     */
    public VerticalStraightLineAxis(String axisName,  LinearUnit unit) {
        super(axisName, CoordinateSystemAxisDirection.UP, unit);
    }

    public VerticalStraightLineAxis(String axisName, CoordinateSystemAxisDirection coordinateSystemAxisDirection, LinearUnit unit) {
        super(axisName, coordinateSystemAxisDirection, unit);
        if (coordinateSystemAxisDirection.defaultNormalOrder != 2)
            throw new IllegalArgumentException("Only UP and DOWN directions allowed");
    }

    @Override
    public LinearUnit getUnit() {
        return (LinearUnit)super.getUnit();
    }
}

