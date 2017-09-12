package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;

import static org.geolatte.geom.GeometryType.POINT;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class PointParser<P extends Position> extends AbstractGeometryParser<P, Point<P>> {


    public PointParser(Context<P> context) {
        super(context);
    }


    @Override
    public GeometryType forType() {
        return POINT;
    }

    @Override
    public Point<P> parse(JsonNode root) throws GeoJsonProcessingException {
        PointHolder holder = getCoordinatesArrayAsSinglePosition(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyPoint(crs);
        }

        P pos = holder.toPosition(crs);
        return Geometries.mkPoint(pos, crs);
    }



}
