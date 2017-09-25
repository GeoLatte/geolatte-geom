package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiPointParser<P extends Position> extends AbstractGeometryParser<P, MultiPoint<P>> {
    public MultiPointParser(Context context) {
        super(context);
    }

    @Override
    public MultiPoint<P> parse(JsonNode root, CoordinateReferenceSystem<P> defaultCrs) throws GeoJsonProcessingException {
        LinearPositionsHolder holder = getCoordinatesArrayAsLinear(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension(), defaultCrs);

        if (holder.isEmpty()) {
            return Geometries.mkEmptyMultiPoint(crs);
        }

        PositionSequence<P> positions = holder.toPositionSequence(crs);
        return Geometries.mkMultiPoint(positions, crs);
    }

    @Override
    protected void canHandle(JsonNode root) throws GeoJsonProcessingException {
        if (!getType(root).equals(GeometryType.MULTIPOINT)) {
            throw new GeoJsonProcessingException(String.format("Can't parse %s with %s",  getType(root).getCamelCased(), getClass().getCanonicalName()));
        }
    }


}
