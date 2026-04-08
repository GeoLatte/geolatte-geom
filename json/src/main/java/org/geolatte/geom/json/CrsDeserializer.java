package org.geolatte.geom.json;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsId;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class CrsDeserializer extends ValueDeserializer<CoordinateReferenceSystem> {

    final private GeoJsonCrsReader reader;

    public CrsDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.reader = new GeoJsonCrsReader(defaultCrs, settings);
    }

    @Override
    public CoordinateReferenceSystem<?> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode root = p.readValueAsTree();
        return reader.resolve(new Jackson3JsonTreeNode(root));
    }

    /**
     * Used by other deserializers (notably {@link GeometryDeserializer}) to extract the CRS id
     * from a sibling {@code crs} node that has already been read into a tree.
     */
    CrsId getCrsId(JsonNode root) {
        JsonNode crs = root;
        if (crs == null) return CrsId.UNDEFINED;
        return reader.readCrsId(new Jackson3JsonTreeNode(crs));
    }

    protected CoordinateReferenceSystem<?> getDefaultCrs() {
        return reader.getDefaultCrs();
    }
}
