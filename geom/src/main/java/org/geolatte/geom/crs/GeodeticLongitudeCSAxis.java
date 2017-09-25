package org.geolatte.geom.crs;

/**
 * A coordinate system axis for geodetic longitude, defined as the angle from the prime meridian plane to the meridian
 * plane of a given point, eastward treated as positive
 *
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class GeodeticLongitudeCSAxis extends EllipsoidalAxis {

    /**
     * Creates an instance.
     *
     * @param axisName                      the name for this axis
     * @param unit                          the unit of this axis
     */
    public GeodeticLongitudeCSAxis(String axisName, AngularUnit unit) {
        super(axisName, CoordinateSystemAxisDirection.EAST, unit);
    }
}
