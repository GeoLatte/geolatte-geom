package org.geolatte.geom.crs;

/**
 * A straight line axis for us in cartesian, vertical or linear coordinate systems
 *
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class StraightLineAxis extends CoordinateSystemAxis {
    /**
     * Creates an instance.
     *
     * @param axisName                      the name for this axis
     * @param coordinateSystemAxisDirection the direction for this axis
     * @param unit                          the unit of this axis
     */
    public StraightLineAxis(String axisName, CoordinateSystemAxisDirection coordinateSystemAxisDirection, LinearUnit
            unit) {
        super(axisName, coordinateSystemAxisDirection, unit);
    }

    public StraightLineAxis(String axisName, CoordinateSystemAxisDirection coordinateSystemAxisDirection, int normalOrder, Unit unit) {
        super(axisName, coordinateSystemAxisDirection, normalOrder, unit);
    }
}
