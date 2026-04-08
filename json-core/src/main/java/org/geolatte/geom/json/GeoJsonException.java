package org.geolatte.geom.json;

/**
 * Thrown by the Jackson-free core readers and writers when GeoJSON cannot be processed.
 *
 * <p>This is the base exception used inside {@code geolatte-geojson-core}. The Jackson
 * adapter modules catch it at their boundary and either rethrow as the framework's native
 * Jackson exception type ({@code JsonProcessingException} for Jackson 2,
 * {@code JacksonException} for Jackson 3) or let it propagate as the unchecked exception
 * it already is.</p>
 */
public class GeoJsonException extends RuntimeException {

    public GeoJsonException(String msg) {
        super(msg);
    }

    public GeoJsonException(Throwable cause) {
        super(cause);
    }

    public GeoJsonException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
