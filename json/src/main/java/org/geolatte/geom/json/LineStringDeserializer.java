package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class LineStringDeserializer<P extends Position> extends AbstractGeometryDeserializer<P, LineString<P>> {
    public LineStringDeserializer(Context context) {
        super(context);
    }

    @Override
    public LineString<P> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = checkRoot(p);
        LinearPositionsHolder holder = getCoordinatesArrayAsLinear(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyLineString(crs);
        }

        PositionSequence<P> pos = holder.toPositionSequence(crs);
        return Geometries.mkLineString(pos, crs);

    }

    @Override
    public GeometryType forType() {
        return GeometryType.LINESTRING;
    }
}
