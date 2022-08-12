package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.Box;
import org.geolatte.geom.Feature;
import org.geolatte.geom.Position;

import java.io.IOException;

public class FeatureSerializer<P extends Position, ID> extends JsonSerializer<Feature<P, ID>> {

    private final Settings settings;

    public FeatureSerializer(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void serialize(Feature<P, ID> feature, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", Feature.TYPE);
        if (feature.getId() != null) {
            gen.writeFieldName("id");
            gen.writeObject(feature.getId());
        }
        Box<?> box = feature.getBbox();
        if (box != null && !box.isEmpty() && settings.isSet(Setting.SERIALIZE_FEATURE_BBOX)) {
            gen.writeFieldName("bbox");
            gen.writeObject(box);
        }
        gen.writeFieldName("geometry");
        gen.writeObject(feature.getGeometry());
        gen.writeFieldName("properties");
        gen.writeObject(feature.getProperties());
        gen.writeEndObject();
    }
}
