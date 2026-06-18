package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.Feature;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeoJsonFeatureReader;
import org.geolatte.geom.json.Settings;

import java.io.IOException;

/**
 * Jackson 2 adapter that delegates GeoJSON feature decoding to the
 * Jackson-free {@link GeoJsonFeatureReader}.
 */
public class FeatureDeserializer extends JsonDeserializer<Feature> {

    private final GeoJsonFeatureReader reader;

    public FeatureDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.reader = new GeoJsonFeatureReader(defaultCrs, settings);
    }

    @Override
    public Feature<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = p.readValueAsTree();
        return reader.read(new Jackson2JsonTreeNode(root, ctxt));
    }
}
