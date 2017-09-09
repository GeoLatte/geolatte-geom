package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LinearUnit;

import java.io.IOException;
import java.util.Arrays;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addVerticalSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public abstract class AbstractGeometryDeserializer<P extends Position, G extends Geometry<P>> extends JsonDeserializer<G> {
    protected final Context<P> context;

    public AbstractGeometryDeserializer(Context<P> context) {
        this.context = context;
    }

    @Override
    public abstract G deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException;

    protected JsonNode checkRoot(JsonParser p) throws IOException {
        JsonNode root = getRoot(p);
        GeometryType type = getType(root);
        canHandle(type);
        return root;
    }

    public abstract GeometryType forType();

    protected JsonNode getRoot(JsonParser p) throws IOException {
        ObjectCodec oc = p.getCodec();
        return oc.readTree(p);
    }

    protected void canHandle(GeometryType type) throws GeoJsonProcessingException {
        if (type != forType()) {
            throw new GeoJsonProcessingException(String.format("Can't parse type %s with %s Deserializer", type, forType().toString()));
        }
    }

    protected CoordinateReferenceSystem<P> getDefaultCrs() {
        return this.context.getDefaultCrs();
    }

    protected SinglePositionCoordinatesHolder getCoordinatesArrayAsSinglePosition(JsonNode root) throws GeoJsonProcessingException {
        JsonNode coordinates = root.get("coordinates");
        return toSinglePositionCoordinatesHolder(coordinates);
    }

    protected PositionSequenceCoordinatesHolder getCoordinatesArrayAsPositionSequence(JsonNode root) throws GeoJsonProcessingException {
        JsonNode coordinates = root.get("coordinates");
        if (!coordinates.isArray()) {
            throw new GeoJsonProcessingException("Parser expects coordinate as array");
        }
        PositionSequenceCoordinatesHolder holder = new PositionSequenceCoordinatesHolder();
        for( int i = 0; i < coordinates.size(); i++) {
            holder.push(toSinglePositionCoordinatesHolder(coordinates.get(i)));
        }
        return holder;
    }

    protected SinglePositionCoordinatesHolder toSinglePositionCoordinatesHolder(JsonNode coordinates) throws GeoJsonProcessingException {
        if (!coordinates.isArray()) {
            throw new GeoJsonProcessingException("Parser expects coordinate as array");
        }
        int coDim = (context.isFeatureSet(Feature.FORCE_DEFAULT_CRS_DIMENSION)) ?
                getDefaultCrs().getCoordinateDimension() :
                coordinates.size();

        double[] co = new double[coDim];
        for (int i = 0; i < coordinates.size() && i < coDim; i++) {
            co[i] = coordinates.get(i).asDouble();
        }
        return new SinglePositionCoordinatesHolder(co);
    }

    protected boolean isFeatureSet(Feature f) {
        return context.isFeatureSet(f);
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
    protected CoordinateReferenceSystem<P> resolveCrs(JsonNode root, int coordinateDimension) throws GeoJsonProcessingException {
        CrsId id = getCrsId(root);
        CoordinateReferenceSystem<?> base = id.equals(CrsId.UNDEFINED) ? getDefaultCrs() :
                CrsRegistry.getCoordinateReferenceSystemForEPSG(id.getCode(), getDefaultCrs());

        int dimensionDifference = coordinateDimension - base.getCoordinateDimension();

        if (dimensionDifference <= 0) {
            return (CoordinateReferenceSystem<P>) base;
        } else if (dimensionDifference == 1) {
            return (CoordinateReferenceSystem<P>) addVerticalSystem(base, LinearUnit.METER);
        } else {
            return (CoordinateReferenceSystem<P>) addLinearSystem(
                    addVerticalSystem(base, LinearUnit.METER),
                    LinearUnit.METER
            );
        }

    }
}
