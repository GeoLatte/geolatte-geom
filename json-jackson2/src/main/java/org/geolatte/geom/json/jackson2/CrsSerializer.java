package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeoJsonCrsWriter;
import org.geolatte.geom.json.Settings;

import java.io.IOException;

public class CrsSerializer<P extends Position> extends JsonSerializer<CoordinateReferenceSystem<P>> {

    private final GeoJsonCrsWriter writer;

    public CrsSerializer(CoordinateReferenceSystem<P> defaultCRS, Settings settings) {
        this.writer = new GeoJsonCrsWriter(settings);
    }

    @Override
    public void serialize(CoordinateReferenceSystem<P> crs, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        writer.writeNamedCrs(new Jackson2GeoJsonWriter(gen, serializers), crs);
    }
}
