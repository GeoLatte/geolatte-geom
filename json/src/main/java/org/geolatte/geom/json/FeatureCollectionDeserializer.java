package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@SuppressWarnings("rawtypes")
public class FeatureCollectionDeserializer extends JsonDeserializer<FeatureCollection> {
    final private FeatureDeserializer fDeserializer;
    public FeatureCollectionDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.fDeserializer = new FeatureDeserializer(defaultCrs, settings);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FeatureCollection<?, ?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode root = oc.readTree(jsonParser);
        JsonNode featureNds = root.get("features");
        List<Feature<?, ?>> features = new ArrayList<>();
        for (Iterator<JsonNode> it = featureNds.elements(); it.hasNext(); ) {
            JsonNode fNode = it.next();
            features.add(fDeserializer.readFeature(oc, fNode));
        }
        return (GeoJsonFeatureCollection<?,?>)new GeoJsonFeatureCollection(features);
    }
}
