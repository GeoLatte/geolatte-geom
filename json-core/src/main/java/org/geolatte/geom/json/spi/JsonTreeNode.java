package org.geolatte.geom.json.spi;

/**
 * A small abstraction over a JSON tree node, used by the Jackson-free GeoJSON
 * readers in {@code geolatte-geojson-core}.
 *
 * <p>Each Jackson-version-specific adapter module provides an implementation that
 * delegates to the native {@code JsonNode} (e.g. {@code com.fasterxml.jackson.databind.JsonNode}
 * for Jackson 2.x or {@code tools.jackson.databind.JsonNode} for Jackson 3.x).</p>
 *
 * <p>The surface area of this interface is intentionally narrow: it covers only the
 * methods actually invoked by the GeoJSON readers, not the full {@code JsonNode} API.</p>
 */
public interface JsonTreeNode {

    boolean isArray();

    boolean isNull();

    int size();

    /**
     * Returns the value associated with the given field name, or {@code null} if absent.
     */
    JsonTreeNode get(String fieldName);

    /**
     * Returns the array element at the given index.
     */
    JsonTreeNode get(int index);

    String asText();

    double asDouble();

    int asInt();

    long asLong();

    boolean canConvertToInt();

    boolean canConvertToLong();

    /**
     * Returns the property names of an object node, in iteration order.
     */
    Iterable<String> propertyNames();

    /**
     * Materialises this node into its natural Java representation by delegating to the host
     * ObjectMapper: objects &rarr; Map, arrays &rarr; List, scalars &rarr; Number/Boolean/String/null.
     */
    Object toJavaObject();
}
