package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.json.spi.JsonTreeNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Jackson 2 implementation of {@link JsonTreeNode} that wraps a native
 * {@link JsonNode}.
 */
final class Jackson2JsonTreeNode implements JsonTreeNode {

    private final JsonNode node;

    Jackson2JsonTreeNode(JsonNode node) {
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
        return child == null ? null : new Jackson2JsonTreeNode(child);
    }

    @Override
    public JsonTreeNode get(int index) {
        JsonNode child = node.get(index);
        return child == null ? null : new Jackson2JsonTreeNode(child);
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
        // Jackson 2 exposes Iterator<String> via fieldNames(); materialise into a list
        // so the SPI doesn't leak the Iterator-vs-Iterable difference.
        List<String> names = new ArrayList<>();
        Iterator<String> it = node.fieldNames();
        while (it.hasNext()) {
            names.add(it.next());
        }
        return names;
    }
}
