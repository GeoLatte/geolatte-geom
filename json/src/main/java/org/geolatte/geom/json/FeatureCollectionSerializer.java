package org.geolatte.geom.json;

import org.geolatte.geom.Box;
import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.Position;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class FeatureCollectionSerializer<P extends Position, ID> extends ValueSerializer<FeatureCollection<P, ID>> {

    private final Settings settings;

    FeatureCollectionSerializer(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void serialize(FeatureCollection<P, ID> featureColl, JsonGenerator gen, SerializationContext serializers) {
        gen.writeStartObject();
        gen.writeStringProperty("type", FeatureCollection.TYPE);
        Box<?> box = featureColl.getBbox();
        if (box != null && !box.isEmpty() && settings.isSet(Setting.SERIALIZE_FEATURE_COLLECTION_BBOX)) {
            gen.writePOJOProperty("bbox", box);
        }
        gen.writeArrayPropertyStart("features");
        for (Feature<?, ?> f : featureColl.getFeatures()) {
            gen.writePOJO(f);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
}
