package org.geolatte.geom.json.spi;

/**
 * A small streaming-writer abstraction used by the Jackson-free GeoJSON writers
 * in {@code geolatte-geojson-core}.
 *
 * <p>Each Jackson-version-specific adapter module provides an implementation that
 * delegates to the native {@code JsonGenerator} together with the surrounding
 * serialization context (so that {@link #writePojo} and {@link #writePojoProperty}
 * can recurse into the host {@code ObjectMapper} for nested values).</p>
 *
 * <p>Method names mirror the Jackson 3 vocabulary. The Jackson 2 adapter translates
 * each call to its older equivalent (for example, {@link #writeName} becomes
 * {@code writeFieldName}; {@link #writeStringProperty} becomes {@code writeStringField};
 * {@link #writePojo} becomes {@code writeObject}).</p>
 */
public interface GeoJsonWriter {

    void writeStartObject();

    void writeEndObject();

    void writeStartArray();

    void writeEndArray();

    void writeName(String name);

    void writeStringProperty(String name, String value);

    void writeArrayPropertyStart(String name);

    void writeDoubleArray(double[] values, int offset, int length);

    /**
     * Delegates to the host serializer for an arbitrary value. The host {@code ObjectMapper}
     * looks up the appropriate {@code JsonSerializer}/{@code ValueSerializer} for the value's
     * runtime type and writes it. This is how the GeoJSON writers recurse into nested geometries
     * and arbitrary feature property maps without re-implementing bean serialization in the core.
     */
    void writePojo(Object value);

    /**
     * Convenience that combines {@link #writeName} and {@link #writePojo}.
     */
    void writePojoProperty(String name, Object value);
}
