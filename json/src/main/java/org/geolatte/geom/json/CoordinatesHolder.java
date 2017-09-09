package org.geolatte.geom.json;

import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
abstract class CoordinatesHolder {

    abstract boolean isEmpty();

    abstract int getCoordinateDimension();

}
