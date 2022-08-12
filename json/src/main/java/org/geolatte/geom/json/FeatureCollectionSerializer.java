package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.Box;
import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.Position;

import java.io.IOException;

public class FeatureCollectionSerializer<P extends Position, ID> extends JsonSerializer<FeatureCollection<P, ID>> {

    private final Settings settings;

    FeatureCollectionSerializer(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void serialize(FeatureCollection<P, ID> featureColl, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", FeatureCollection.TYPE);
        Box<?> box = featureColl.getBbox();
        if (box != null && !box.isEmpty() && settings.isSet(Setting.SERIALIZE_FEATURE_COLLECTION_BBOX)) {
            gen.writeFieldName("bbox");
            gen.writeObject(featureColl.getBbox());
        }
        gen.writeArrayFieldStart("features");
        for (Feature<?, ?> f : featureColl.getFeatures()) {
            gen.writeObject(f);
        }
        gen.writeEndArray();
        gen.writeEndObject();

    }
}
