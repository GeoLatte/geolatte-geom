package org.geolatte.geom.json;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.support.DecodeException;
import org.geolatte.geom.codec.support.Holder;
import org.geolatte.geom.codec.support.LinearPositionsHolder;
import org.geolatte.geom.codec.support.LinearPositionsListHolder;
import org.geolatte.geom.codec.support.PointHolder;
import org.geolatte.geom.codec.support.PolygonListHolder;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.json.spi.JsonTreeNode;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.geolatte.geom.Geometries.mkEmptyGeometryCollection;
import static org.geolatte.geom.Geometries.mkGeometryCollection;

/**
 * Jackson-free reader for GeoJSON geometry objects.
 *
 * <p>This is the canonical implementation of GeoJSON geometry decoding. The Jackson
 * adapter modules call into this class via {@link JsonTreeNode}; they do not duplicate
 * the decoding logic.</p>
 */
public final class GeoJsonGeometryReader {

    private final CoordinateReferenceSystem<?> defaultCrs;
    private final Settings settings;
    private final GeoJsonCrsReader crsReader;

    public GeoJsonGeometryReader(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.defaultCrs = defaultCrs;
        this.settings = settings;
        this.crsReader = new GeoJsonCrsReader(defaultCrs, settings);
    }

    public Geometry<?> read(JsonTreeNode root) {
        CoordinateReferenceSystem<?> crs = resolveBaseCrs(root);
        GeometryBuilder builder = GeometryBuilder.create(root);
        CoordinateReferenceSystem<?> adjustedCrs = settings.isSet(Setting.FORCE_DEFAULT_CRS_DIMENSION) ||
                settings.isSet(Setting.IGNORE_CRS) ?
                crs :
                CoordinateReferenceSystems.adjustTo(crs, builder.getCoordinateDimension());
        return builder.parse(adjustedCrs);
    }

    private CoordinateReferenceSystem<?> resolveBaseCrs(JsonTreeNode root) {
        JsonTreeNode crsNode = root.get("crs");
        if (crsNode == null || settings.isSet(Setting.FORCE_DEFAULT_CRS_DIMENSION) ||
                settings.isSet(Setting.IGNORE_CRS)) {
            return defaultCrs;
        }
        return crsReader.resolve(crsNode);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Internal builder hierarchy — translated from the previous GeometryDeserializer
    // ─────────────────────────────────────────────────────────────────────────────

    abstract static class GeometryBuilder {

        static GeometryBuilder create(JsonTreeNode root) {
            GeometryType type = getType(root);
            if (type == GeometryType.GEOMETRYCOLLECTION) {
                return new GeometryCollectionBuilder(root);
            }
            return new SimpleGeometryBuilder(root);
        }

        abstract int getCoordinateDimension();

        abstract <P extends Position> Geometry<P> parse(CoordinateReferenceSystem<P> crs);

        static GeometryType getType(JsonTreeNode root) {
            String type = root.get("type").asText();
            try {
                return GeometryType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new GeoJsonException(String.format("Can't parse GeoJson of type %s", type));
            }
        }
    }

    static final class GeometryCollectionBuilder extends GeometryBuilder {

        private final List<GeometryBuilder> components = new ArrayList<>();

        GeometryCollectionBuilder(JsonTreeNode root) {
            JsonTreeNode geometriesNode = root.get("geometries");
            for (int i = 0; i < geometriesNode.size(); i++) {
                components.add(GeometryBuilder.create(geometriesNode.get(i)));
            }
        }

        @Override
        int getCoordinateDimension() {
            return components.stream()
                    .map(GeometryBuilder::getCoordinateDimension)
                    .filter(d -> d > 0).findFirst().orElse(2);
        }

        @Override
        <P extends Position> Geometry<P> parse(CoordinateReferenceSystem<P> crs) {
            List<Geometry<P>> collect;
            try {
                collect = components.stream()
                        .map(b -> toComponentGeom(crs, b)).collect(toList());
            } catch (RuntimeException e) {
                throw new GeoJsonException(e.getMessage());
            }
            return collect.isEmpty() ? mkEmptyGeometryCollection(crs) : mkGeometryCollection(collect);
        }

        private <P extends Position> Geometry<P> toComponentGeom(CoordinateReferenceSystem<P> crs, GeometryBuilder b) {
            return b.parse(crs);
        }
    }

    static final class SimpleGeometryBuilder extends GeometryBuilder {

        private final GeometryType type;
        private final Holder coordinates;

        SimpleGeometryBuilder(JsonTreeNode root) {
            this.type = getType(root);
            this.coordinates = toHolder(type, root.get("coordinates"));
        }

        @Override
        int getCoordinateDimension() {
            return this.coordinates.getCoordinateDimension();
        }

        @Override
        <P extends Position> Geometry<P> parse(CoordinateReferenceSystem<P> crs) {
            try {
                return coordinates.toGeometry(crs, type);
            } catch (DecodeException e) {
                throw new GeoJsonException(e);
            }
        }

        private static Holder toHolder(GeometryType geomType, JsonTreeNode root) {
            switch (geomType) {
                case POINT:
                    return toPointHolder(root);
                case LINESTRING:
                case MULTIPOINT:
                    return toLinearPositionsHolder(root);
                case POLYGON:
                case MULTILINESTRING:
                    return toLinearPositionsListHolder(root);
                case MULTIPOLYGON:
                    return toPolygonalListHolder(root);
                default:
                    throw new GeoJsonException("Unsupported geometry type " + geomType);
            }
        }

        private static PointHolder toPointHolder(JsonTreeNode coordinates) {
            if (!coordinates.isArray()) {
                throw new GeoJsonException("Parser expects coordinate as array");
            }

            if (coordinates.size() == 0) {
                return new PointHolder();
            }

            int coDim = coordinates.size();

            if (coDim < 2) throw new GeoJsonException("Need at least 2 coordinate values in array");

            double[] co = new double[coDim];

            for (int i = 0; i < coordinates.size() && i < coDim; i++) {
                co[i] = coordinates.get(i).asDouble();
            }
            return new PointHolder(co);
        }

        private static LinearPositionsHolder toLinearPositionsHolder(JsonTreeNode coordinates) {
            if (!coordinates.isArray()) {
                throw new GeoJsonException("Parser expects coordinate as array");
            }
            LinearPositionsHolder holder = new LinearPositionsHolder();
            for (int i = 0; i < coordinates.size(); i++) {
                holder.push(toPointHolder(coordinates.get(i)));
            }
            return holder;
        }

        private static LinearPositionsListHolder toLinearPositionsListHolder(JsonTreeNode coordinates) {
            if (!coordinates.isArray()) {
                throw new GeoJsonException("Parser expects coordinate as array");
            }
            LinearPositionsListHolder holder = new LinearPositionsListHolder();
            for (int i = 0; i < coordinates.size(); i++) {
                holder.push(toLinearPositionsHolder(coordinates.get(i)));
            }
            return holder;
        }

        private static PolygonListHolder toPolygonalListHolder(JsonTreeNode coordinates) {
            if (!coordinates.isArray()) {
                throw new GeoJsonException("Parser expects coordinate as array");
            }
            PolygonListHolder holder = new PolygonListHolder();
            for (int i = 0; i < coordinates.size(); i++) {
                holder.push(toLinearPositionsListHolder(coordinates.get(i)));
            }
            return holder;
        }
    }
}
