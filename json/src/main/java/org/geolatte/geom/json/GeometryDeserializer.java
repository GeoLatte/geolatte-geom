package org.geolatte.geom.json;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.support.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.CrsRegistry;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.geolatte.geom.Geometries.mkEmptyGeometryCollection;
import static org.geolatte.geom.Geometries.mkGeometryCollection;

/**
 * A Parser for Geometry types
 * Created by Karel Maesen, Geovise BVBA on 13/09/17.
 */
public class GeometryDeserializer extends ValueDeserializer<Geometry<?>> {

    private final CoordinateReferenceSystem<?> defaultCRS;
    private final Settings settings;
    private final CrsDeserializer crsDeser;

    public GeometryDeserializer(CoordinateReferenceSystem<?> defaultCRS, Settings settings) {
        this.defaultCRS = defaultCRS;
        this.settings = settings;
        this.crsDeser = new CrsDeserializer(this.defaultCRS, settings);
    }

    private JsonNode getRoot(JsonParser p) {
        return p.readValueAsTree();
    }

    @Override
    public Geometry<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonNode root = getRoot(p);
        return parseGeometry(root);
    }

    Geometry<?> parseGeometry(JsonNode root) throws GeoJsonProcessingException {
        CoordinateReferenceSystem<?> crs = resolveBaseCrs(root);
        GeometryBuilder parser = GeometryBuilder.create(root);
        CoordinateReferenceSystem<?> adjustedCrs = settings.isSet(Setting.FORCE_DEFAULT_CRS_DIMENSION) ||
                settings.isSet(Setting.IGNORE_CRS) ?
                crs :
                CoordinateReferenceSystems.adjustTo(crs, parser.getCoordinateDimension());
        return parser.parse(adjustedCrs);
    }

    protected CoordinateReferenceSystem<?> getDefaultCrs() {
        return defaultCRS;
    }

    private CoordinateReferenceSystem<?> resolveBaseCrs(JsonNode root) throws GeoJsonProcessingException {
        CrsId id = getCrsId(root);
        return id.equals(CrsId.UNDEFINED) ||
                settings.isSet(Setting.FORCE_DEFAULT_CRS_DIMENSION) ||
                settings.isSet(Setting.IGNORE_CRS) ?
                this.defaultCRS :
                CrsRegistry.getCoordinateReferenceSystemForEPSG(id.getCode(), getDefaultCrs());
    }

    protected CrsId getCrsId(JsonNode root) throws GeoJsonProcessingException {
        JsonNode crs = root.get("crs");
        return crsDeser.getCrsId(crs);
    }
}

abstract class GeometryBuilder {

    static GeometryBuilder create(JsonNode root) throws GeoJsonProcessingException {
        GeometryType type = getType(root);
        if (type == GeometryType.GEOMETRYCOLLECTION) {
            return new GeometryCollectionBuilder(root);
        }
        return new SimpleGeometryBuilder(root);
    }

    abstract int getCoordinateDimension();

    abstract <P extends Position> Geometry<P> parse(CoordinateReferenceSystem<P> crs) throws GeoJsonProcessingException;

    static GeometryType getType(JsonNode root) throws GeoJsonProcessingException {
        String type = root.get("type").asText();
        try {
            return GeometryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new GeoJsonProcessingException(String.format("Can't parse GeoJson of type %s", type));
        }
    }
}

class GeometryCollectionBuilder extends GeometryBuilder {

    final private List<GeometryBuilder> components = new ArrayList<>();

    GeometryCollectionBuilder(JsonNode root) throws GeoJsonProcessingException {

        JsonNode geometriesNode = root.get("geometries");
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
    <P extends Position> Geometry<P> parse(CoordinateReferenceSystem<P> crs) throws GeoJsonProcessingException {
        List<Geometry<P>> collect;
        try {
            collect = components.stream()
                    .map(b -> toComponentGeom(crs, b)).collect(toList());
        } catch (RuntimeException e) {
            throw new GeoJsonProcessingException(e.getMessage());
        }
        return collect.isEmpty() ? mkEmptyGeometryCollection(crs) : mkGeometryCollection(collect);
    }

    private <P extends Position> Geometry<P> toComponentGeom(CoordinateReferenceSystem<P> crs, GeometryBuilder b) {
        try {
            return b.parse(crs);
        } catch (GeoJsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

class SimpleGeometryBuilder extends GeometryBuilder {
    final private GeometryType type;
    final private Holder coordinates;

    SimpleGeometryBuilder(JsonNode root) throws GeoJsonProcessingException {
        this.type = getType(root);
        this.coordinates = toHolder(type, getCoordinates(root));
    }

    private JsonNode getCoordinates(JsonNode root) {
        return root.get("coordinates");
    }

    int getCoordinateDimension() {
        return this.coordinates.getCoordinateDimension();
    }

    <P extends Position> Geometry<P> parse(CoordinateReferenceSystem<P> crs) throws GeoJsonProcessingException {
        try {
            return coordinates.toGeometry(crs, type);
        } catch (DecodeException e) {
            throw new GeoJsonProcessingException(e);
        }
    }

    protected Holder toHolder(GeometryType geomType, JsonNode root) throws GeoJsonProcessingException {
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
                throw new GeoJsonProcessingException("Unsupported geometry type " + geomType);
        }
    }

    private PointHolder toPointHolder(JsonNode coordinates) throws GeoJsonProcessingException {
        if (!coordinates.isArray()) {
            throw new GeoJsonProcessingException("Parser expects coordinate as array");
        }

        if (coordinates.size() == 0) {
            return new PointHolder();
        }

        int coDim = coordinates.size();

        if (coDim < 2) throw new GeoJsonProcessingException("Need at least 2 coordinate values in array");

        double[] co = new double[coDim];

        for (int i = 0; i < coordinates.size() && i < coDim; i++) {
            co[i] = coordinates.get(i).asDouble();
        }
        return new PointHolder(co);
    }

    private LinearPositionsHolder toLinearPositionsHolder(JsonNode coordinates) throws GeoJsonProcessingException {
        if (!coordinates.isArray()) {
            throw new GeoJsonProcessingException("Parser expects coordinate as array");
        }
        LinearPositionsHolder holder = new LinearPositionsHolder();
        for (int i = 0; i < coordinates.size(); i++) {
            holder.push(toPointHolder(coordinates.get(i)));
        }
        return holder;
    }

    private LinearPositionsListHolder toLinearPositionsListHolder(JsonNode coordinates) throws GeoJsonProcessingException {
        if (!coordinates.isArray()) {
            throw new GeoJsonProcessingException("Parser expects coordinate as array");
        }
        LinearPositionsListHolder holder = new LinearPositionsListHolder();
        for (int i = 0; i < coordinates.size(); i++) {
            holder.push(toLinearPositionsHolder(coordinates.get(i)));
        }
        return holder;
    }

    private PolygonListHolder toPolygonalListHolder(JsonNode coordinates) throws GeoJsonProcessingException {
        if (!coordinates.isArray()) {
            throw new GeoJsonProcessingException("Parser expects coordinate as array");
        }
        PolygonListHolder holder = new PolygonListHolder();
        for (int i = 0; i < coordinates.size(); i++) {
            holder.push(toLinearPositionsListHolder(coordinates.get(i)));
        }
        return holder;
    }
}
