package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeoJsonGeometryReader;
import org.geolatte.geom.json.Settings;

import java.io.IOException;

/**
 * Jackson 2 adapter that delegates GeoJSON geometry decoding to the
 * Jackson-free {@link GeoJsonGeometryReader}.
 */
public class GeometryDeserializer extends JsonDeserializer<Geometry<?>> {

    private final GeoJsonGeometryReader reader;

    public GeometryDeserializer(CoordinateReferenceSystem<?> defaultCRS, Settings settings) {
        this.reader = new GeoJsonGeometryReader(defaultCRS, settings);
    }

    @Override
    public Geometry<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = p.readValueAsTree();
        return reader.read(new Jackson2JsonTreeNode(root, ctxt));
    }
}
