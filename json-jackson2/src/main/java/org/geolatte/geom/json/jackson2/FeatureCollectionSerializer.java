package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.Position;
import org.geolatte.geom.json.GeoJsonFeatureCollectionWriter;
import org.geolatte.geom.json.Settings;

import java.io.IOException;

public class FeatureCollectionSerializer<P extends Position, ID> extends JsonSerializer<FeatureCollection<P, ID>> {

    private final GeoJsonFeatureCollectionWriter writer;

    FeatureCollectionSerializer(Settings settings) {
        this.writer = new GeoJsonFeatureCollectionWriter(settings);
    }

    @Override
    public void serialize(FeatureCollection<P, ID> featureColl, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        writer.write(new Jackson2GeoJsonWriter(gen, serializers), featureColl);
    }
}
