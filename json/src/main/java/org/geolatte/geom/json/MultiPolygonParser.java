package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiPolygonParser<P extends Position> extends AbstractGeometryParser<P, MultiPolygon<P>> {

    public MultiPolygonParser(Context<P> ctxt) {
        super(ctxt);
    }

    @Override
    public MultiPolygon<P> parse(JsonNode root, CoordinateReferenceSystem<P> defaultCrs) throws GeoJsonProcessingException {
        PolygonListHolder holder = getCoordinatesArrayAsPolygonList(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension(), defaultCrs);

        if (holder.isEmpty()) {
            return Geometries.mkEmptyMultiPolygon(crs);
        }
        return Geometries.mkMultiPolygon(holder.toPolygons(crs));
    }

    @Override
    protected void canHandle(JsonNode root) throws GeoJsonProcessingException {
        if (!getType(root).equals(GeometryType.MULTIPOLYGON)) {
            throw new GeoJsonProcessingException(String.format("Can't parse %s with %s",  getType(root).getCamelCased(), getClass().getCanonicalName()));
        }
    }

}

