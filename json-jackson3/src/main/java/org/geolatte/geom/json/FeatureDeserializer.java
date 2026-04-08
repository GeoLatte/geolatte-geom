package org.geolatte.geom.json;

import org.geolatte.geom.Feature;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Jackson 3 adapter that delegates GeoJSON feature decoding to the
 * Jackson-free {@link GeoJsonFeatureReader}.
 */
public class FeatureDeserializer extends ValueDeserializer<Feature> {

    private final GeoJsonFeatureReader reader;

    public FeatureDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.reader = new GeoJsonFeatureReader(defaultCrs, settings);
    }

    @Override
    public Feature<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode root = p.readValueAsTree();
        return reader.read(new Jackson3JsonTreeNode(root));
    }

    /**
     * Used by {@link FeatureCollectionDeserializer} to read a feature from an
     * already-materialized sub-tree.
     */
    GeoJsonFeature<?, Object> readFeature(JsonParser p, JsonNode root) {
        return reader.read(new Jackson3JsonTreeNode(root));
    }
}
