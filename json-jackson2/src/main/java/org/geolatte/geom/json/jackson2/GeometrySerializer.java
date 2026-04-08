package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.json.GeoJsonGeometryWriter;
import org.geolatte.geom.json.Settings;

import java.io.IOException;

public class GeometrySerializer<P extends Position> extends JsonSerializer<Geometry<P>> {

    private final GeoJsonGeometryWriter writer;

    public GeometrySerializer(Settings settings) {
        this.writer = new GeoJsonGeometryWriter(settings);
    }

    @Override
    public void serialize(Geometry<P> geometry, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        writer.write(new Jackson2GeoJsonWriter(gen, serializers), geometry);
    }

    @Override
    public void serializeWithType(Geometry<P> value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen,
                typeSer.typeId(value, value.getClass(), JsonToken.VALUE_STRING));
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }
}
