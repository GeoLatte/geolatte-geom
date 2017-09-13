package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class PointParser<P extends Position> extends AbstractGeometryParser<P, Point<P>> {


    public PointParser(Context<P> context) {
        super(context);
    }


    @Override
    public Point<P> parse(JsonNode root, CoordinateReferenceSystem<P> defaultCrs) throws GeoJsonProcessingException {
        PointHolder holder = getCoordinatesArrayAsSinglePosition(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension(), defaultCrs);

        if (holder.isEmpty()) {
            return Geometries.mkEmptyPoint(crs);
        }

        P pos = holder.toPosition(crs);
        return Geometries.mkPoint(pos, crs);
    }

    @Override
    protected void canHandle(JsonNode root) throws GeoJsonProcessingException {
        if (!getType(root).equals(GeometryType.POINT)) {
            throw new GeoJsonProcessingException(String.format("Can't parse %s with %s", getType(root).getCamelCased(), getClass().getCanonicalName()));
        }
    }


}
