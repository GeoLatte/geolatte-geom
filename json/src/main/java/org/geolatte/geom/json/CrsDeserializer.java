package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.CrsRegistry;

import java.io.IOException;

public class CrsDeserializer extends JsonDeserializer<CoordinateReferenceSystem> {

    final private CoordinateReferenceSystem<?> defaultCrs;
    final private Settings settings;

    public CrsDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.defaultCrs = defaultCrs;
        this.settings = settings;
    }

    private JsonNode getRoot(JsonParser p) throws IOException, GeoJsonProcessingException {
        ObjectCodec oc = p.getCodec();
        return oc.readTree(p);
    }

    @Override
    public CoordinateReferenceSystem<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = getRoot(p);
        return resolveBaseCrs(root);
    }

    protected CoordinateReferenceSystem<?> getDefaultCrs() {
        return defaultCrs;
    }

    private CoordinateReferenceSystem<?> resolveBaseCrs(JsonNode root) throws GeoJsonProcessingException {
        CrsId id = getCrsId(root);
        return id.equals(CrsId.UNDEFINED) || settings.isSet(Setting.FORCE_DEFAULT_CRS_DIMENSION) ?
                this.defaultCrs :
                CrsRegistry.getCoordinateReferenceSystemForEPSG(id.getCode(), getDefaultCrs());
    }

    protected CrsId getCrsId(JsonNode root) throws GeoJsonProcessingException {
        JsonNode crs = root;//root.get("crs");
        if (crs == null) return CrsId.UNDEFINED;

        String type = crs.get("type").asText();

        if (type.equalsIgnoreCase("name")) {
            String text = crs.get("properties").get("name").asText();
            return CrsId.parse(text);
        }

        if (type.equalsIgnoreCase("link")) {
            String text = crs.get("properties").get("href").asText();
            String[] components = text.split("/");
            int last = components.length - 1;
            return CrsId.valueOf(components[last-1], Integer.decode(components[last]));
        }

        throw new GeoJsonProcessingException("Can parse only named crs elements");
    }
}
