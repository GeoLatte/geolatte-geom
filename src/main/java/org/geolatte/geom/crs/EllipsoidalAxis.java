package org.geolatte.geom.crs;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
abstract public class EllipsoidalAxis extends CoordinateSystemAxis {


    EllipsoidalAxis(String axisName, CoordinateSystemAxisDirection coordinateSystemAxisDirection, Unit unit) {
        super(axisName, coordinateSystemAxisDirection, unit);
    }
}
