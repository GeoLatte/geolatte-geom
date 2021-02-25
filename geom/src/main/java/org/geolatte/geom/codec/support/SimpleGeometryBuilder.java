package org.geolatte.geom.codec.support;

import org.geolatte.geom.Geometries;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.WktDecodeException;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

public class SimpleGeometryBuilder extends GeometryBuilder {
    final private GeometryType type;
    private Holder positions;

    public SimpleGeometryBuilder(GeometryType type) {
        this.type = type;
    }

    public <P extends Position> Geometry<P> createGeometry(CoordinateReferenceSystem<P> crs) {
        if (positions == null || positions.isEmpty()) return Geometries.mkEmptyGeometry(type, crs);
        return positions.toGeometry(crs, type);
    }

    @Override
    public int getCoordinateDimension() {
        return positions == null || positions.isEmpty() ? 2 : positions.getCoordinateDimension();
    }

    @Override
    public void setPositions(Holder positions) {
        this.positions = positions;
    }

}
