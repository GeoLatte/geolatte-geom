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
public class LineStringParser<P extends Position> extends AbstractGeometryParser<P, LineString<P>> {
    public LineStringParser(Context context) {
        super(context);
    }

    @Override
    public GeometryType forType() {
        return GeometryType.LINESTRING;
    }

    @Override
    public LineString<P> parse(JsonNode root) throws GeoJsonProcessingException {
        LinearPositionsHolder holder = getCoordinatesArrayAsLinear(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyLineString(crs);
        }

        PositionSequence<P> pos = holder.toPositionSequence(crs);
        return Geometries.mkLineString(pos, crs);
    }


}
