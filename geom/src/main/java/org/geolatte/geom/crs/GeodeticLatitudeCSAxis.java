package org.geolatte.geom.crs;

/**
 * A coordinate system axis for geodetic latitude, defined as the angle from the equatorial plane to the perpendicular
 * to the ellipsoid through a given point, northwards treated as positive
 *
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class GeodeticLatitudeCSAxis extends EllipsoidalAxis {

    /**
     * Creates an instance.
     *
     * @param axisName                      the name for this axis
     * @param unit                          the unit of this axis
     */
    public GeodeticLatitudeCSAxis(String axisName, AngularUnit unit) {
        super(axisName, CoordinateSystemAxisDirection.NORTH, unit);
    }

}
