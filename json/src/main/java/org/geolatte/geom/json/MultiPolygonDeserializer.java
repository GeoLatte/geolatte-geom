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
public class MultiPolygonDeserializer<P extends Position> extends AbstractGeometryDeserializer<P, MultiPolygon<P>> {

    public MultiPolygonDeserializer(Context<P> ctxt) {
        super(ctxt);
    }

    @Override
    public MultiPolygon<P> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = checkRoot(p);
        PolygonListHolder holder = getCoordinatesArrayAsPolygonList(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyMultiPolygon(crs);
        }
        return Geometries.mkMultiPolygon(holder.toPolygons(crs));
    }


    @Override
    public GeometryType forType() {
        return GeometryType.MULTIPOLYGON;
    }
}

