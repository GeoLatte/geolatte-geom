package org.geolatte.geom.json;

import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class CrsSerializer<P extends Position> extends ValueSerializer<CoordinateReferenceSystem<P>> {

    final private CoordinateReferenceSystem<P> defaultCRS;
    final private Settings settings;

    public CrsSerializer(CoordinateReferenceSystem<P> defaultCRS, Settings settings) {
        this.defaultCRS = defaultCRS;
        this.settings = settings;
    }

    @Override
    public void serialize(CoordinateReferenceSystem<P> crs, JsonGenerator gen, SerializationContext serializers) {
        writeCrs(gen, crs);
    }

    private void writeCrs(JsonGenerator gen, CoordinateReferenceSystem<P> crs) {
        writeNamedCrs(gen, crs);
    }

    private void writeNamedCrs(JsonGenerator gen, CoordinateReferenceSystem<P> crs) {
        gen.writeStartObject();
        gen.writeStringProperty("type", "name");
        gen.writeName("properties");
        if (settings.isSet(Setting.SERIALIZE_CRS_AS_URN)) {
            writeCrsName(gen, crs.getCrsId().toUrn());
        } else {
            writeCrsName(gen, crs.getCrsId().toString());
        }
        gen.writeEndObject();
    }

    private void writeCrsName(JsonGenerator gen, String epsgString) {
        gen.writeStartObject();
        gen.writeStringProperty("name", epsgString);
        gen.writeEndObject();
    }
}
