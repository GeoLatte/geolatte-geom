package org.geolatte.geom.json;

import org.geolatte.geom.Feature;
import org.geolatte.geom.Position;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class FeatureSerializer<P extends Position, ID> extends ValueSerializer<Feature<P, ID>> {

    private final GeoJsonFeatureWriter writer;

    public FeatureSerializer(Settings settings) {
        this.writer = new GeoJsonFeatureWriter(settings);
    }

    @Override
    public void serialize(Feature<P, ID> feature, JsonGenerator gen, SerializationContext serializers) {
        writer.write(new Jackson3GeoJsonWriter(gen, serializers), feature);
    }
}
