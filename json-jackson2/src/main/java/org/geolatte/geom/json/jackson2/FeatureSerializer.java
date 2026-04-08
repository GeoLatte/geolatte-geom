package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.Feature;
import org.geolatte.geom.Position;
import org.geolatte.geom.json.GeoJsonFeatureWriter;
import org.geolatte.geom.json.Settings;

import java.io.IOException;

public class FeatureSerializer<P extends Position, ID> extends JsonSerializer<Feature<P, ID>> {

    private final GeoJsonFeatureWriter writer;

    public FeatureSerializer(Settings settings) {
        this.writer = new GeoJsonFeatureWriter(settings);
    }

    @Override
    public void serialize(Feature<P, ID> feature, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        writer.write(new Jackson2GeoJsonWriter(gen, serializers), feature);
    }
}
