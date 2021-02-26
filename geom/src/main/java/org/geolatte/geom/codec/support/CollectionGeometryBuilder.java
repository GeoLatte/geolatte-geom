package org.geolatte.geom.codec.support;

import org.geolatte.geom.Geometries;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionGeometryBuilder extends GeometryBuilder {
    final private List<GeometryBuilder> components = new ArrayList<>();

    public void push(GeometryBuilder builder) {
        components.add(builder);
    }

    public <P extends Position> Geometry<P> createGeometry(CoordinateReferenceSystem<P> crs) {
        if (components.isEmpty()) return Geometries.mkEmptyGeometry(GeometryType.GEOMETRYCOLLECTION, crs);
        List<Geometry<P>> geoms = components.stream()
                .map(c -> c.createGeometry(crs))
                .collect(Collectors.toList());
        return Geometries.mkGeometryCollection(geoms);
    }

    @Override
    public int getCoordinateDimension() {
        return components.isEmpty() ? 2 : components.get(0).getCoordinateDimension();
    }

    @Override
    public void setPositions(Holder positions) {
        throw new IllegalStateException("Can't set positions directly on this instance");
    }
}
