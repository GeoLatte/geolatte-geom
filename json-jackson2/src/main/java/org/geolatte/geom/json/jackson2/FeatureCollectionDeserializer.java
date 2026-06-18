package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeoJsonFeatureCollectionReader;
import org.geolatte.geom.json.Settings;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class FeatureCollectionDeserializer extends JsonDeserializer<FeatureCollection> {

    private final GeoJsonFeatureCollectionReader reader;

    public FeatureCollectionDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.reader = new GeoJsonFeatureCollectionReader(defaultCrs, settings);
    }

    @Override
    public FeatureCollection<?, ?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode root = jsonParser.readValueAsTree();
        return reader.read(new Jackson2JsonTreeNode(root, deserializationContext));
    }
}
