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
public class MultiLineStringParser<P extends Position> extends AbstractGeometryParser<P, MultiLineString<P>> {

    public MultiLineStringParser(Context context) {
        super(context);
    }

    @Override
    public GeometryType forType() {
        return GeometryType.MULTILINESTRING;
    }

    @Override
    public MultiLineString<P> parse(JsonNode root) throws GeoJsonProcessingException {
        LinearPositionsListHolder holder = getCoordinatesArrayAsPolygonal(root);
        CoordinateReferenceSystem<P> crs = resolveCrs(root, holder.getCoordinateDimension());

        if (holder.isEmpty()) {
            return Geometries.mkEmptyMultiLineString(crs);
        }

        List<LineString<P>> components = holder.toLineStrings(crs);
        return Geometries.mkMultiLineString(components);
    }


}

