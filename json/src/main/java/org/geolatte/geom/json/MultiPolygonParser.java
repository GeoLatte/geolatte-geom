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
    public GeometryType forType() {
        return GeometryType.MULTIPOLYGON;
    }

    @Override
    public MultiPolygon<P> parse(JsonNode root) throws GeoJsonProcessingException {
        PolygonListHolder holder = getCoordinatesArrayAsPolygonList(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyMultiPolygon(crs);
        }
        return Geometries.mkMultiPolygon(holder.toPolygons(crs));
    }
}

