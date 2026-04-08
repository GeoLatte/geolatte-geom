package org.geolatte.geom.json.jackson3;

import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeoJsonFeatureCollectionReader;
import org.geolatte.geom.json.Settings;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

@SuppressWarnings("rawtypes")
public class FeatureCollectionDeserializer extends ValueDeserializer<FeatureCollection> {

    private final GeoJsonFeatureCollectionReader reader;

    public FeatureCollectionDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.reader = new GeoJsonFeatureCollectionReader(defaultCrs, settings);
    }

    @Override
    public FeatureCollection<?, ?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        JsonNode root = jsonParser.readValueAsTree();
        return reader.read(new Jackson3JsonTreeNode(root));
    }
}
