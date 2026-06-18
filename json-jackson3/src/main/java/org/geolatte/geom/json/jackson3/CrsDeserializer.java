package org.geolatte.geom.json.jackson3;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeoJsonCrsReader;
import org.geolatte.geom.json.Settings;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class CrsDeserializer extends ValueDeserializer<CoordinateReferenceSystem> {

    final private GeoJsonCrsReader reader;

    public CrsDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.reader = new GeoJsonCrsReader(defaultCrs, settings);
    }

    @Override
    public CoordinateReferenceSystem<?> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode root = p.readValueAsTree();
        return reader.resolve(new Jackson3JsonTreeNode(root, ctxt));
    }
}
