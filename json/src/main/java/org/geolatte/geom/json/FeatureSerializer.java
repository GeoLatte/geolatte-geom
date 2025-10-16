package org.geolatte.geom.json;

import org.geolatte.geom.Box;
import org.geolatte.geom.Feature;
import org.geolatte.geom.Position;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class FeatureSerializer<P extends Position, ID> extends ValueSerializer<Feature<P, ID>> {

    private final Settings settings;

    public FeatureSerializer(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void serialize(Feature<P, ID> feature, JsonGenerator gen, SerializationContext serializers) {
        gen.writeStartObject();
        gen.writeStringProperty("type", Feature.TYPE);
        if (feature.getId() != null) {
            gen.writePOJOProperty("id", feature.getId());
        }
        Box<?> box = feature.getBbox();
        if (box != null && !box.isEmpty() && settings.isSet(Setting.SERIALIZE_FEATURE_BBOX)) {
            gen.writePOJOProperty("bbox", box);
        }
        gen.writePOJOProperty("geometry", feature.getGeometry());
        gen.writePOJOProperty("properties", feature.getProperties());
        gen.writeEndObject();
    }
}
