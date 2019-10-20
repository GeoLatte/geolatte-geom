package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;

public class CrsSerializer<P extends Position> extends JsonSerializer<CoordinateReferenceSystem<P>> {

    final private CoordinateReferenceSystem<P> defaultCRS;
    final private Settings settings;

    public CrsSerializer(CoordinateReferenceSystem<P> defaultCRS, Settings settings) {
        this.defaultCRS = defaultCRS;
        this.settings = settings;
    }

    @Override
    public void serialize(CoordinateReferenceSystem<P> crs, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        writeCrs(gen, crs);
    }

    private void writeCrs(JsonGenerator gen, CoordinateReferenceSystem<P> crs) throws IOException {
        writeNamedCrs(gen, crs);
    }

    private void writeNamedCrs(JsonGenerator gen, CoordinateReferenceSystem<P> crs) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", "name");
        gen.writeFieldName("properties");
        if (settings.isSet(Setting.SERIALIZE_CRS_AS_URN)) {
            writeCrsName(gen, crs.getCrsId().toUrn());
        } else {
            writeCrsName(gen, crs.getCrsId().toString());
        }
        gen.writeEndObject();
    }

    private void writeCrsName(JsonGenerator gen, String epsgString) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", epsgString);
        gen.writeEndObject();
    }
}
