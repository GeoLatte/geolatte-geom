package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeoJsonCrsReader;
import org.geolatte.geom.json.Settings;

import java.io.IOException;

public class CrsDeserializer extends JsonDeserializer<CoordinateReferenceSystem> {

    private final GeoJsonCrsReader reader;

    public CrsDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.reader = new GeoJsonCrsReader(defaultCrs, settings);
    }

    @Override
    public CoordinateReferenceSystem<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = p.readValueAsTree();
        return reader.resolve(new Jackson2JsonTreeNode(root));
    }
}
