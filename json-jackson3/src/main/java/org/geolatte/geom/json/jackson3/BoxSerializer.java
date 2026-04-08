package org.geolatte.geom.json.jackson3;

import org.geolatte.geom.Box;
import org.geolatte.geom.Position;
import org.geolatte.geom.json.GeoJsonBoxWriter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class BoxSerializer<P extends Position> extends ValueSerializer<Box<P>> {

    private final GeoJsonBoxWriter writer = new GeoJsonBoxWriter();

    @Override
    public void serialize(Box<P> box, JsonGenerator gen, SerializationContext serializers) {
        writer.write(new Jackson3GeoJsonWriter(gen, serializers), box);
    }

    @Override
    public boolean isEmpty(SerializationContext provider, Box<P> box) {
        return box == null || box.isEmpty();
    }
}
