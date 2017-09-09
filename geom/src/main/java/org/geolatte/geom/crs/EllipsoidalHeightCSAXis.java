package org.geolatte.geom.crs;

/**
 * A coordinate system axis for ellipsoidal height, defined as the distance of a point from the ellipsoid measured along
 * the perpendicular from the ellipsoid to this point, positive if upwards or outside of the ellipsoid
 *
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class EllipsoidalHeightCSAXis extends StraightLineAxis {

    /**
     * Creates an instance.
     *
     * @param axisName                      the name for this axis
     * @param unit                          the unit of this axis
     */
    EllipsoidalHeightCSAXis(String axisName, LinearUnit unit) {
        super(axisName, CoordinateSystemAxisDirection.UP, unit);
    }

}
