package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;

import static org.geolatte.geom.GeometryType.POINT;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class PointDeserializer<P extends Position> extends AbstractGeometryDeserializer<P, Point<P>> {


    public PointDeserializer(Context<P> context) {
        super(context);
    }


    @Override
    public Point<P> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = checkRoot(p);

        PointHolder holder = getCoordinatesArrayAsSinglePosition(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyPoint(crs);
        }

        P pos = holder.toPosition(crs);
        return Geometries.mkPoint(pos, crs);
    }

    @Override
    public GeometryType forType() {
        return POINT;
    }

}
