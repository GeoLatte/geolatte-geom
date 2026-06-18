package org.geolatte.geom.json;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.spi.JsonTreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Jackson-free reader for GeoJSON {@code Feature} objects.
 *
 * <p>Resulting features carry geometries decoded by {@link GeoJsonGeometryReader} and
 * properties materialised into a plain {@link HashMap}. Property values are deserialized by
 * delegating the whole {@code properties} node back to the host ObjectMapper (via
 * {@link JsonTreeNode#toJavaObject()}), so nested objects, arrays, numbers and booleans are
 * reconstructed faithfully rather than coerced to int/text.</p>
 */
public final class GeoJsonFeatureReader {

    private final GeoJsonGeometryReader geometryReader;

    public GeoJsonFeatureReader(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.geometryReader = new GeoJsonGeometryReader(defaultCrs, settings);
    }

    public GeoJsonFeature<?, Object> read(JsonTreeNode root) {
        JsonTreeNode geomNode = root.get("geometry");
        Geometry<?> geom = (geomNode == null || geomNode.isNull()) ? null : geometryReader.read(geomNode);

        Object id = null;
        JsonTreeNode idNode = root.get("id");
        if (idNode != null) {
            if (idNode.canConvertToLong()) {
                id = idNode.asLong();
            } else {
                id = idNode.asText();
            }
        }

        HashMap<String, Object> properties = new HashMap<>();
        JsonTreeNode propNode = root.get("properties");
        if (propNode != null && !propNode.isNull()) {
            Object obj = propNode.toJavaObject();
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) obj;
                properties.putAll(m);
            }
        }

        return new GeoJsonFeature<>(geom, id, properties);
    }
}
