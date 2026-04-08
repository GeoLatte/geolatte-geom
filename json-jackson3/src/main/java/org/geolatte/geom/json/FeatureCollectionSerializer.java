package org.geolatte.geom.json;

import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.Position;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class FeatureCollectionSerializer<P extends Position, ID> extends ValueSerializer<FeatureCollection<P, ID>> {

    private final GeoJsonFeatureCollectionWriter writer;

    FeatureCollectionSerializer(Settings settings) {
        this.writer = new GeoJsonFeatureCollectionWriter(settings);
    }

    @Override
    public void serialize(FeatureCollection<P, ID> featureColl, JsonGenerator gen, SerializationContext serializers) {
        writer.write(new Jackson3GeoJsonWriter(gen, serializers), featureColl);
    }
}
