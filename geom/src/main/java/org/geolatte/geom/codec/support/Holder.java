package org.geolatte.geom.json;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
abstract class Holder {

    abstract boolean isEmpty();

    abstract int getCoordinateDimension();

    abstract <P extends Position> Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs, GeometryType geomType) throws GeoJsonProcessingException;

}
