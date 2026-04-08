package org.geolatte.geom.json.jackson3;

import org.geolatte.geom.json.spi.GeoJsonWriter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

/**
 * Jackson 3 implementation of {@link GeoJsonWriter} that wraps a native
 * {@link JsonGenerator} together with the surrounding {@link SerializationContext}.
 *
 * <p>The {@link #writePojo(Object)} and {@link #writePojoProperty(String, Object)} hooks
 * delegate back to the host {@code ObjectMapper} so that nested geometries and arbitrary
 * feature property values flow through whichever serializer the user has configured.</p>
 */
final class Jackson3GeoJsonWriter implements GeoJsonWriter {

    private final JsonGenerator gen;
    private final SerializationContext ctxt;

    Jackson3GeoJsonWriter(JsonGenerator gen, SerializationContext ctxt) {
        this.gen = gen;
        this.ctxt = ctxt;
    }

    @Override
    public void writeStartObject() {
        gen.writeStartObject();
    }

    @Override
    public void writeEndObject() {
        gen.writeEndObject();
    }

    @Override
    public void writeStartArray() {
        gen.writeStartArray();
    }

    @Override
    public void writeEndArray() {
        gen.writeEndArray();
    }

    @Override
    public void writeName(String name) {
        gen.writeName(name);
    }

    @Override
    public void writeStringProperty(String name, String value) {
        gen.writeStringProperty(name, value);
    }

    @Override
    public void writeArrayPropertyStart(String name) {
        gen.writeArrayPropertyStart(name);
    }

    @Override
    public void writeDoubleArray(double[] values, int offset, int length) {
        gen.writeArray(values, offset, length);
    }

    @Override
    public void writePojo(Object value) {
        gen.writePOJO(value);
    }

    @Override
    public void writePojoProperty(String name, Object value) {
        gen.writePOJOProperty(name, value);
    }
}
