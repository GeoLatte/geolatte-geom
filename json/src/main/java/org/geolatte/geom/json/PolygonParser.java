package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class PolygonParser<P extends Position> extends AbstractGeometryParser<P, Polygon<P>> {

    public PolygonParser(CoordinateReferenceSystem<P> defaultCRS,Settings settings) {
        super(defaultCRS, settings);
    }

    @Override
    public Polygon<P> parse(JsonNode root, CoordinateReferenceSystem<P> defaultCrs) throws GeoJsonProcessingException {
        LinearPositionsListHolder holder = getCoordinatesArrayAsPolygonal(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension(), defaultCrs);

        if (holder.isEmpty()) {
            return Geometries.mkEmptyPolygon(crs);
        }
        return Geometries.mkPolygon(holder.toLinearRings(crs));
    }

    @Override
    protected void canHandle(JsonNode root) throws GeoJsonProcessingException {
        if (!getType(root).equals(GeometryType.POLYGON)) {
            throw new GeoJsonProcessingException(String.format("Can't parse %s with %s", getType(root).getCamelCased(), getClass().getCanonicalName()));
        }
    }


}
