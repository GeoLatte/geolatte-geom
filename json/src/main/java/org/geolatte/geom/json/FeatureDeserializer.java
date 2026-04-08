package org.geolatte.geom.json;

import org.geolatte.geom.Feature;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.HashMap;

/**
 * Created by Karel Maesen, Geovise BVBA on 13/07/2018.
 */
public class FeatureDeserializer extends ValueDeserializer<Feature> {

    final private CoordinateReferenceSystem<?> defaultCrs;
    final private Settings settings;
    final private GeometryDeserializer geomParser;

    public FeatureDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.defaultCrs = defaultCrs;
        this.settings = settings;
        geomParser = new GeometryDeserializer(defaultCrs, settings);
    }

    @Override
    public Feature<?,?> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode root = p.readValueAsTree();
        return readFeature(p, root);
    }

    GeoJsonFeature<?, Object> readFeature(JsonParser p, JsonNode root) throws JacksonException {
        JsonNode geomNode = root.get("geometry");
        Geometry<?> geom = (null == geomNode || geomNode.isNull()) ? null : geomParser.parseGeometry(geomNode);

        Object id = null;
        JsonNode idNode = root.get("id");
        if(idNode != null) {
            if (idNode.canConvertToLong()) {
                id = idNode.asLong();
            } else {
                id = idNode.asText();
            }
        }

        HashMap<String, Object> properties = new HashMap<>();
        JsonNode propNode = root.get("properties");
        if(propNode != null) {
            for (String property: propNode.propertyNames()) {
                Object value = null;
                JsonNode valueNode = propNode.get(property);
                if (valueNode == null) continue;

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
