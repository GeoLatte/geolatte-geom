package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiLineStringParser<P extends Position> extends AbstractGeometryParser<P, MultiLineString<P>> {

    public MultiLineStringParser(CoordinateReferenceSystem<P> defaultCRS,Settings settings) {
        super(defaultCRS, settings);
    }

    @Override
    public MultiLineString<P> parse(JsonNode root, CoordinateReferenceSystem<P> defaultCrs) throws GeoJsonProcessingException {
        LinearPositionsListHolder holder = getCoordinatesArrayAsPolygonal(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension(), defaultCrs);

        if (holder.isEmpty()) {
            return Geometries.mkEmptyMultiLineString(crs);
        }

        List<LineString<P>> components = holder.toLineStrings(crs);
        return Geometries.mkMultiLineString(components);
    }

    @Override
    protected void canHandle(JsonNode root) throws GeoJsonProcessingException {
        if (!getType(root).equals(GeometryType.MULTILINESTRING)) {
            throw new GeoJsonProcessingException(String.format("Can't parse %s with %s", getType(root).getCamelCased(), getClass().getCanonicalName()));
        }
    }


}

