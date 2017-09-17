package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class LineStringParser<P extends Position> extends AbstractGeometryParser<P, LineString<P>> {
    public LineStringParser(Context context) {
        super(context);
    }

    @Override
    public LineString<P> parse(JsonNode root, CoordinateReferenceSystem<P> defaultCrs) throws GeoJsonProcessingException {
        LinearPositionsHolder holder = getCoordinatesArrayAsLinear(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension(), defaultCrs);

        if (holder.isEmpty()) {
            return Geometries.mkEmptyLineString(crs);
        }

        PositionSequence<P> pos = holder.toPositionSequence(crs);
        return Geometries.mkLineString(pos, crs);
    }

    @Override
    protected void canHandle(JsonNode root) throws GeoJsonProcessingException {
        if (!getType(root).equals(GeometryType.LINESTRING)) {
            throw new GeoJsonProcessingException(String.format("Can't parse %s with %s",  getType(root).getCamelCased(), getClass().getCanonicalName()));
        }
    }



}
