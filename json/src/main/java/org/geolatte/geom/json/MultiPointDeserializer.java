package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiPointDeserializer<P extends Position> extends AbstractGeometryDeserializer<P, MultiPoint<P>> {
    public MultiPointDeserializer(Context context) {
        super(context);
    }

    @Override
    public MultiPoint<P> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = checkRoot(p);
        LinearPositionsHolder holder = getCoordinatesArrayAsLinear(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyMultiPoint(crs);
        }

        PositionSequence<P> positions = holder.toPositionSequence(crs);
        return Geometries.mkMultiPoint(positions, crs);
    }

    @Override
    public GeometryType forType() {
        return GeometryType.MULTIPOINT;
    }

}
