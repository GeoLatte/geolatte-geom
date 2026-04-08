package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.Box;
import org.geolatte.geom.Position;
import org.geolatte.geom.json.GeoJsonBoxWriter;

import java.io.IOException;

public class BoxSerializer<P extends Position> extends JsonSerializer<Box<P>> {

    private final GeoJsonBoxWriter writer = new GeoJsonBoxWriter();

    @Override
    public void serialize(Box<P> box, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        writer.write(new Jackson2GeoJsonWriter(gen, serializers), box);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Box<P> box) {
        return box == null || box.isEmpty();
    }
}
