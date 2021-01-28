package org.geolatte.geom.codec.support;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
abstract public class Holder {

    abstract public boolean isEmpty();

    abstract public int getCoordinateDimension();

    abstract public <P extends Position> Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs, GeometryType geomType);

}
