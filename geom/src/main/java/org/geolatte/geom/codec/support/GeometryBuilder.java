package org.geolatte.geom.codec.support;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

abstract public class GeometryBuilder {
    public static GeometryBuilder create(GeometryType type) {
        if (type == GeometryType.GEOMETRYCOLLECTION) {
            return new CollectionGeometryBuilder();
        } else {
            return new SimpleGeometryBuilder(type);
        }
    }

    abstract public <P extends Position> Geometry<P> createGeometry(CoordinateReferenceSystem<P> crs);

    abstract public int getCoordinateDimension();

    abstract public void setPositions(Holder positions);
}

