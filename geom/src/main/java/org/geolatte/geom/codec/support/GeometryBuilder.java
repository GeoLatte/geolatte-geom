package org.geolatte.geom.codec.support;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

abstract public class GeometryBuilder {
    public static GeometryBuilder create(GeometryType type) {
        switch (type) {
            case GEOMETRYCOLLECTION:
            case MULTIPOINT:
            case MULTILINESTRING:
            case MULTIPOLYGON:
                return new CollectionGeometryBuilder(type);
            default:
                return new SimpleGeometryBuilder(type);
        }
    }

    abstract public <P extends Position> Geometry<P> createGeometry(CoordinateReferenceSystem<P> crs);

    abstract public int getCoordinateDimension();

    abstract public void setPositions(Holder positions);

    abstract public boolean isEmpty();
}

