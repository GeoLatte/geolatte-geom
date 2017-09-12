package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class PolygonParser<P extends Position> extends AbstractGeometryParser<P, Polygon<P>> {

    public PolygonParser(Context<P> ctxt) {
        super(ctxt);
    }

    @Override
    public GeometryType forType() {
        return GeometryType.POLYGON;
    }

    @Override
    public Polygon<P> parse(JsonNode root) throws GeoJsonProcessingException {
        LinearPositionsListHolder holder = getCoordinatesArrayAsPolygonal(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if(holder.isEmpty()) {
            return Geometries.mkEmptyPolygon(crs);
        }
        return Geometries.mkPolygon(holder.toLinearRings(crs));
    }


}
