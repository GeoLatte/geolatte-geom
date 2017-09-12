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
public class MultiPointParser<P extends Position> extends AbstractGeometryParser<P, MultiPoint<P>> {
    public MultiPointParser(Context context) {
        super(context);
    }

    @Override
    public GeometryType forType() {
        return GeometryType.MULTIPOINT;
    }

    @Override
    public MultiPoint<P> parse(JsonNode root) throws GeoJsonProcessingException {
        LinearPositionsHolder holder = getCoordinatesArrayAsLinear(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyMultiPoint(crs);
        }

        PositionSequence<P> positions = holder.toPositionSequence(crs);
        return Geometries.mkMultiPoint(positions, crs);
    }


}
