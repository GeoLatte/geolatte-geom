package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiLineStringDeserializer<P extends Position> extends AbstractGeometryDeserializer<P, MultiLineString<P>> {

    public MultiLineStringDeserializer(Context context) {
        super(context);
    }

    @Override
    public MultiLineString<P> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = checkRoot(p);
        LinearPositionsListHolder holder = getCoordinatesArrayAsPolygonal(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyMultiLineString(crs);
        }

        List<LineString<P>> components = holder.toLineStrings(crs);
        return Geometries.mkMultiLineString(components);

    }

    @Override
    public GeometryType forType() {
        return GeometryType.MULTILINESTRING;
    }
}

