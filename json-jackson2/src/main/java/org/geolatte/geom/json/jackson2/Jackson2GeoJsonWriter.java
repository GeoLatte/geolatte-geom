package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.json.spi.GeoJsonWriter;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Jackson 2 implementation of {@link GeoJsonWriter} that wraps a native
 * {@link JsonGenerator} together with the surrounding {@link SerializerProvider}.
 *
 * <p>Jackson 2's {@link JsonGenerator} methods declare {@code throws IOException};
 * the SPI is unchecked, so each method here translates {@code IOException} into
 * {@link UncheckedIOException}. The host {@code JsonSerializer.serialize} method
 * unwraps it again at the boundary.</p>
 */
final class Jackson2GeoJsonWriter implements GeoJsonWriter {

    private final JsonGenerator gen;
    private final SerializerProvider provider;

    Jackson2GeoJsonWriter(JsonGenerator gen, SerializerProvider provider) {
        this.gen = gen;
        this.provider = provider;
    }

    @Override
    public void writeStartObject() {
        try {
            gen.writeStartObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeEndObject() {
        try {
            gen.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeStartArray() {
        try {
            gen.writeStartArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeEndArray() {
        try {
            gen.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeName(String name) {
        try {
            gen.writeFieldName(name);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeStringProperty(String name, String value) {
        try {
            gen.writeStringField(name, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeArrayPropertyStart(String name) {
        try {
            gen.writeArrayFieldStart(name);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDoubleArray(double[] values, int offset, int length) {
        try {
            gen.writeArray(values, offset, length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writePojo(Object value) {
        try {
            gen.writeObject(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writePojoProperty(String name, Object value) {
        try {
            gen.writeObjectField(name, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
