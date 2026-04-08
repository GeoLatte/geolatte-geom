package org.geolatte.geom.json;

import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class FeatureCollectionDeserializer extends ValueDeserializer<FeatureCollection> {
    final private FeatureDeserializer fDeserializer;
    public FeatureCollectionDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.fDeserializer = new FeatureDeserializer(defaultCrs, settings);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FeatureCollection<?, ?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        JsonNode root = jsonParser.readValueAsTree();
        JsonNode featureNds = root.get("features");
        List<Feature<?, ?>> features = new ArrayList<>();
        for (JsonNode feature : featureNds) {
            features.add(fDeserializer.readFeature(jsonParser, feature));
        }
        return (GeoJsonFeatureCollection<?,?>)new GeoJsonFeatureCollection(features);
    }
}
