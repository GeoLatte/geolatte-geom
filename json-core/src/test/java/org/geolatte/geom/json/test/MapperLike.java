package org.geolatte.geom.json.test;

/**
 * A small abstraction over a Jackson {@code ObjectMapper}, used by the shared
 * {@link AbstractGeoJsonContract} so the same test logic can drive both the
 * Jackson 2 and Jackson 3 adapters.
 *
 * <p>Each adapter module provides an implementation that wraps a native
 * {@code ObjectMapper} configured with its {@code GeolatteGeomModule}.</p>
 */
public interface MapperLike {

    /** Serializes the given value to a JSON string. */
    String writeAsString(Object value);

    /** Deserializes the JSON string into an instance of the given type. */
    <T> T readValue(String json, Class<T> type);
}
