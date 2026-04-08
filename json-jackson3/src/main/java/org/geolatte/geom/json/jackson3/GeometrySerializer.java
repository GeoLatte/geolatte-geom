package org.geolatte.geom.json.jackson3;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.json.GeoJsonGeometryWriter;
import org.geolatte.geom.json.Settings;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.WritableTypeId;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;

public class GeometrySerializer<P extends Position> extends ValueSerializer<Geometry<P>> {

    private final GeoJsonGeometryWriter writer;

    public GeometrySerializer(Settings settings) {
        this.writer = new GeoJsonGeometryWriter(settings);
    }

    @Override
    public void serialize(Geometry<P> geometry, JsonGenerator gen, SerializationContext serializers) {
        writer.write(new Jackson3GeoJsonWriter(gen, serializers), geometry);
    }

    @Override
    public void serializeWithType(Geometry<P> value, JsonGenerator gen, SerializationContext serializers, TypeSerializer typeSer) {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, serializers,
                typeSer.typeId(value, value.getClass(), JsonToken.VALUE_STRING));
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, serializers, typeIdDef);
    }
}
