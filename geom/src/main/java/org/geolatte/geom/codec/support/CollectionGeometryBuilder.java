package org.geolatte.geom.codec.support;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds a geometry collection, or one of the homogeneous collections (MultiPoint, MultiLineString, MultiPolygon),
 * from the builders of its members.
 * <p>
 * Members are built as full geometries, so that empty members (e.g. {@code MULTIPOINT((1 2),EMPTY)}) are
 * preserved. For a homogeneous collection, an empty member of another geometry type is accepted and converted
 * to an empty member of the expected type: some WKB dialects encode an empty point as an empty collection.
 */
public class CollectionGeometryBuilder extends GeometryBuilder {
    final private GeometryType type;
    final private List<GeometryBuilder> components = new ArrayList<>();

    public CollectionGeometryBuilder() {
        this(GeometryType.GEOMETRYCOLLECTION);
    }

    public CollectionGeometryBuilder(GeometryType type) {
        switch (type) {
            case GEOMETRYCOLLECTION:
            case MULTIPOINT:
            case MULTILINESTRING:
            case MULTIPOLYGON:
                this.type = type;
                break;
            default:
                throw new IllegalArgumentException("Not a collection type: " + type);
        }
    }

    public void push(GeometryBuilder builder) {
        components.add(builder);
    }

    public <P extends Position> Geometry<P> createGeometry(CoordinateReferenceSystem<P> crs) {
        if (components.isEmpty()) return Geometries.mkEmptyGeometry(type, crs);
        List<Geometry<P>> geoms = components.stream()
                .map(c -> c.createGeometry(crs))
                .collect(Collectors.toList());
        switch (type) {
            case MULTIPOINT:
                return Geometries.mkMultiPoint(this.<P, Point<P>>members(geoms, GeometryType.POINT, crs));
            case MULTILINESTRING:
                return Geometries.mkMultiLineString(this.<P, LineString<P>>members(geoms, GeometryType.LINESTRING, crs));
            case MULTIPOLYGON:
                return Geometries.mkMultiPolygon(this.<P, Polygon<P>>members(geoms, GeometryType.POLYGON, crs));
            default:
                return Geometries.mkGeometryCollection(geoms);
        }
    }

    @SuppressWarnings("unchecked")
    private <P extends Position, G extends Geometry<P>> List<G> members(List<Geometry<P>> geoms,
                                                                        GeometryType memberType,
                                                                        CoordinateReferenceSystem<P> crs) {
        List<G> members = new ArrayList<>(geoms.size());
        for (Geometry<P> geom : geoms) {
            if (geom.getGeometryType() == memberType) {
                members.add((G) geom);
            } else if (geom.isEmpty()) {
                members.add((G) Geometries.mkEmptyGeometry(memberType, crs));
            } else {
                throw new DecodeException(String.format("Expected %s element in %s, but found %s",
                        memberType, type, geom.getGeometryType()));
            }
        }
        return members;
    }

    @Override
    public int getCoordinateDimension() {
        return components.stream()
                .filter(c -> !c.isEmpty())
                .mapToInt(GeometryBuilder::getCoordinateDimension)
                .max()
                .orElse(2);
    }

    @Override
    public void setPositions(Holder positions) {
        throw new IllegalStateException("Can't set positions directly on this instance");
    }

    /**
     * A collection is empty when it has no members, or when all its members are empty.
     */
    @Override
    public boolean isEmpty() {
        return components.stream().allMatch(GeometryBuilder::isEmpty);
    }
}
