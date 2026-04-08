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
 * properties materialised into a plain {@link HashMap}. The minimal value coercion
 * (int / long / text) mirrors the original {@code FeatureDeserializer} behavior.</p>
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
        if (propNode != null) {
            for (String property : propNode.propertyNames()) {
                JsonTreeNode valueNode = propNode.get(property);
                if (valueNode == null) continue;

                Object value;
                if (valueNode.canConvertToInt()) {
                    value = valueNode.asInt();
                } else {
                    value = valueNode.asText();
                }
                properties.put(property, value);
            }
        }

        return new GeoJsonFeature<>(geom, id, properties);
    }
}
