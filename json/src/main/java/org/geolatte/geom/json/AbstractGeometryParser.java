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

import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasMeasureAxis;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.mkCoordinateReferenceSystem;
import static org.geolatte.geom.crs.Unit.METER;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public abstract class AbstractGeometryParser<P extends Position, G extends Geometry<P>> extends JsonDeserializer<G> {

    private final CoordinateReferenceSystem<P> defaultCRS;
    private final Settings settings;

    protected AbstractGeometryParser(CoordinateReferenceSystem<P> defaultCRS, Settings settings) {
        this.defaultCRS = defaultCRS;
        this.settings = settings;
    }

    private G parse(JsonNode root) throws GeoJsonProcessingException {
        return parse(root, getDefaultCrs());
    }

    public abstract G parse(JsonNode root, CoordinateReferenceSystem<P> crs) throws GeoJsonProcessingException;

    @Override
    public G deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return parse(getRoot(p));
    }

    private JsonNode getRoot(JsonParser p) throws IOException, GeoJsonProcessingException {
        ObjectCodec oc = p.getCodec();
        JsonNode root = oc.readTree(p);
        canHandle(root);
        return root;
    }


    protected abstract void canHandle(JsonNode root) throws GeoJsonProcessingException;

    protected CoordinateReferenceSystem<P> getDefaultCrs() {
        return defaultCRS;
    }

    protected PointHolder getCoordinatesArrayAsSinglePosition(JsonNode root) throws GeoJsonProcessingException {
        JsonNode coordinates = root.get("coordinates");
        return toPointHolder(coordinates);
    }

    protected LinearPositionsHolder getCoordinatesArrayAsLinear(JsonNode root) throws GeoJsonProcessingException {
        JsonNode coordinates = root.get("coordinates");
        return toLinearPositionsHolder(coordinates);
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

    protected LinearPositionsListHolder getCoordinatesArrayAsPolygonal(JsonNode root) throws GeoJsonProcessingException {
        JsonNode coordinates = root.get("coordinates");
        return toLinearPositionsListHolder(coordinates);
    }

    protected LinearPositionsListHolder toLinearPositionsListHolder(JsonNode coordinates) throws GeoJsonProcessingException {
        if (!coordinates.isArray()) {
            throw new GeoJsonProcessingException("Parser expects coordinate as array");
        }
        LinearPositionsListHolder holder = new LinearPositionsListHolder();
        for (int i = 0; i < coordinates.size(); i++) {
            holder.push(toLinearPositionsHolder(coordinates.get(i)));
        }
        return holder;
    }

    protected PolygonListHolder getCoordinatesArrayAsPolygonList(JsonNode root) throws GeoJsonProcessingException {
        JsonNode coordinates = root.get("coordinates");
        if (!coordinates.isArray()) {
            throw new GeoJsonProcessingException("Parser expects coordinate as array");
        }
        PolygonListHolder holder = new PolygonListHolder();
        for (int i = 0; i < coordinates.size(); i++) {
            holder.push(toLinearPositionsListHolder(coordinates.get(i)));
        }
        return holder;
    }

    /**
     * Reads the JSON array of numbers as a coordinate array.
     *
     * <p>
     * We always assume that the coordinates are in normal order, consistent with RFC 7946. If a third coordinate value is provided, it will
     * always be interpreted as elevation. A fourth coordinate value will always be interpreted as a measure value.
     * </p>
     *
     * @param coordinates
     * @return
     * @throws GeoJsonProcessingException
     */
    protected PointHolder toPointHolder(JsonNode coordinates) throws GeoJsonProcessingException {
        if (!coordinates.isArray()) {
            throw new GeoJsonProcessingException("Parser expects coordinate as array");
        }

        if (coordinates.size() == 0) {
            return new PointHolder();
        }

        int coDim = (settings.isSet(Setting.FORCE_DEFAULT_CRS_DIMENSION)) ?
                getDefaultCrs().getCoordinateDimension() :
                coordinates.size();

        if (coDim < 2) throw new GeoJsonProcessingException("Need at least 2 coordinate values in array");

        double[] co = new double[coDim];

        for (int i = 0; i < coordinates.size() && i < coDim; i++) {
            co[i] = coordinates.get(i).asDouble();
        }
        return new PointHolder(co);
    }

    protected boolean isFeatureSet(Setting f) {
        return settings.isSet(f);
    }

    protected GeometryType getType(JsonNode root) throws GeoJsonProcessingException {
        String type = root.get("type").asText();
        try {
            return GeometryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new GeoJsonProcessingException(String.format("Can't parse GeoJson of type", type));
        }
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


    @SuppressWarnings("unchecked")
    protected CoordinateReferenceSystem<P> resolveCrs(JsonNode root, int coordinateDimension, CoordinateReferenceSystem<P> defaultCrs)
            throws GeoJsonProcessingException {
        CrsId id = getCrsId(root);
        CoordinateReferenceSystem<?> base = id.equals(CrsId.UNDEFINED) ||
                settings.isSet(Setting.FORCE_DEFAULT_CRS_DIMENSION) ? defaultCrs :
                CrsRegistry.getCoordinateReferenceSystemForEPSG(id.getCode(), getDefaultCrs());

        if (coordinateDimension == 0) {
            return (CoordinateReferenceSystem<P>) base;
        }

        if (coordinateDimension == 2) {
            return (CoordinateReferenceSystem<P>) mkCoordinateReferenceSystem(base, null, null);
        }

        if (coordinateDimension == 3) {
            if (hasMeasureAxis(base)) return (CoordinateReferenceSystem<P>) base;
            return (CoordinateReferenceSystem<P>) mkCoordinateReferenceSystem(base, METER, null);
        }

        if (coordinateDimension == 4) {
            return (CoordinateReferenceSystem<P>) mkCoordinateReferenceSystem(base, METER, METER);
        }

        throw new GeoJsonProcessingException("CoordinateDimension " + coordinateDimension + " less than 2 or larger than 4");
    }
}
