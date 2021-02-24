package org.geolatte.geom.codec.support;

import org.geolatte.geom.Geometries;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.WktDecodeException;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

