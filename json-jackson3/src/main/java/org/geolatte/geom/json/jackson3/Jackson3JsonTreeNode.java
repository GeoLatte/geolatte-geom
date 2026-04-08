package org.geolatte.geom.json.jackson3;

import org.geolatte.geom.json.spi.JsonTreeNode;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Jackson 3 implementation of {@link JsonTreeNode} that wraps a native
 * {@link JsonNode}.
 */
final class Jackson3JsonTreeNode implements JsonTreeNode {

    private final JsonNode node;

    Jackson3JsonTreeNode(JsonNode node) {
        this.node = node;
    }

    @Override
    public boolean isArray() {
        return node.isArray();
    }

    @Override
    public boolean isNull() {
        return node.isNull();
    }

    @Override
    public int size() {
        return node.size();
    }

    @Override
    public JsonTreeNode get(String fieldName) {
        JsonNode child = node.get(fieldName);
        return child == null ? null : new Jackson3JsonTreeNode(child);
    }

    @Override
    public JsonTreeNode get(int index) {
        JsonNode child = node.get(index);
        return child == null ? null : new Jackson3JsonTreeNode(child);
    }

    @Override
    public String asText() {
        return node.asText();
    }

    @Override
    public double asDouble() {
        return node.asDouble();
    }

    @Override
    public int asInt() {
        return node.asInt();
    }

    @Override
    public long asLong() {
        return node.asLong();
    }

    @Override
    public boolean canConvertToInt() {
        return node.canConvertToInt();
    }

    @Override
    public boolean canConvertToLong() {
        return node.canConvertToLong();
    }

    @Override
    public Iterable<String> propertyNames() {
        // Materialise into a list so callers don't depend on Jackson types in the iterator.
        List<String> names = new ArrayList<>();
        for (String name : node.propertyNames()) {
            names.add(name);
        }
        return names;
    }
}
