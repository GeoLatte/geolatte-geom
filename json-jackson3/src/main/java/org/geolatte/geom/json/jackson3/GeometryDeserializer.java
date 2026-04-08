package org.geolatte.geom.json.jackson3;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeoJsonGeometryReader;
import org.geolatte.geom.json.Settings;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Jackson 3 adapter that delegates GeoJSON geometry decoding to the
 * Jackson-free {@link GeoJsonGeometryReader}.
 */
public class GeometryDeserializer extends ValueDeserializer<Geometry<?>> {

    private final GeoJsonGeometryReader reader;

    public GeometryDeserializer(CoordinateReferenceSystem<?> defaultCRS, Settings settings) {
        this.reader = new GeoJsonGeometryReader(defaultCRS, settings);
    }

    @Override
    public Geometry<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonNode root = p.readValueAsTree();
        return reader.read(new Jackson3JsonTreeNode(root));
    }

    /**
     * Used by {@link FeatureDeserializer} to parse a geometry from an already-read sub-tree.
     */
    Geometry<?> parseGeometry(JsonNode root) {
        return reader.read(new Jackson3JsonTreeNode(root));
    }
}
