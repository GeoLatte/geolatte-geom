package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.CrsRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.geolatte.geom.Geometries.mkEmptyGeometryCollection;
import static org.geolatte.geom.Geometries.mkGeometryCollection;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.mkCoordinateReferenceSystem;
import static org.geolatte.geom.crs.Unit.METER;

/**
 * A Parser for Geometry types
 * Created by Karel Maesen, Geovise BVBA on 13/09/17.
 */
public class GeometryDeserializer extends JsonDeserializer<Geometry> {

    private final CoordinateReferenceSystem<?> defaultCRS;
    private final Settings settings;

    public GeometryDeserializer(CoordinateReferenceSystem<?> defaultCRS, Settings settings) {
        this.defaultCRS = defaultCRS;
        this.settings = settings;
    }


    private JsonNode getRoot(JsonParser p) throws IOException, GeoJsonProcessingException {
        ObjectCodec oc = p.getCodec();
        return oc.readTree(p);
    }


    @Override
    public Geometry<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = getRoot(p);
        return parseGeometry(root);
    }

    Geometry<?> parseGeometry(JsonNode root) throws GeoJsonProcessingException {
        CoordinateReferenceSystem<?> crs = resolveBaseCrs(root);
        GeometryBuilder parser = GeometryBuilder.create(root);
        CoordinateReferenceSystem<?> adjustedCrs = adjustTo(crs, parser.getCoordinateDimension());
        return parser.parse(adjustedCrs);
    }

    protected CoordinateReferenceSystem<?> getDefaultCrs() {
        return defaultCRS;
    }

    private CoordinateReferenceSystem<?> adjustTo(CoordinateReferenceSystem<?> crs, int coordinateDimension)
            throws GeoJsonProcessingException {

        if (settings.isSet(Setting.FORCE_DEFAULT_CRS_DIMENSION)) return crs;

        if (coordinateDimension <= 2) {
            return mkCoordinateReferenceSystem(crs, null, null);
        }

        if (coordinateDimension == 3) {
            return mkCoordinateReferenceSystem(crs, METER, null);
        }

        if (coordinateDimension == 4) {
            return mkCoordinateReferenceSystem(crs, METER, METER);
        }

        throw new GeoJsonProcessingException("CoordinateDimension " + coordinateDimension + " less than 2 or larger than 4");
    }


    private CoordinateReferenceSystem<?> resolveBaseCrs(JsonNode root) throws GeoJsonProcessingException {
        CrsId id = getCrsId(root);
        return id.equals(CrsId.UNDEFINED) || settings.isSet(Setting.FORCE_DEFAULT_CRS_DIMENSION) ?
                this.defaultCRS :
                CrsRegistry.getCoordinateReferenceSystemForEPSG(id.getCode(), getDefaultCrs());
    }

    protected CrsId getCrsId(JsonNode root) throws GeoJsonProcessingException {
        JsonNode crs = root.get("crs");
        if (crs == null) return CrsId.UNDEFINED;

        String type = crs.get("type").asText();
        if (!type.equalsIgnoreCase("name")) {
            throw new GeoJsonProcessingException("Can parse only named crs elements");
        }

        String text = crs.get("properties").get("name").asText();
        return CrsId.parse(text);
    }
}

abstract class GeometryBuilder {

    GeometryBuilder(JsonNode root) {
    }

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
        super(root);
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
        super(root);
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
        return coordinates.toGeometry(crs, type);
    }

    protected Holder toHolder(GeometryType geomType, JsonNode root) throws GeoJsonProcessingException {
        switch (geomType) {
            case POINT:
                return toPointHolder(root);
            case LINESTRING:
                return toLinearPositionsHolder(root);
            case POLYGON:
                return toLinearPositionsListHolder(root);
            case MULTIPOINT:
                return toLinearPositionsHolder(root);
            case MULTILINESTRING:
                return toLinearPositionsListHolder(root);
            case MULTIPOLYGON:
                return toPolygonalListHolder(root);
            default:
                throw new GeoJsonProcessingException("Unsupported geometry type " + geomType.toString());
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

