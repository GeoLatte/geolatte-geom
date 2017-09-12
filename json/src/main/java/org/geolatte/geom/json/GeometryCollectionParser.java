package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.geolatte.geom.*;

import java.io.IOException;

/**
 * Created by Karel Maesen, Geovise BVBA on 12/09/17.
 */
public class GeometryCollectionParser<P extends Position> extends AbstractGeometryParser<P, GeometryCollection<P, Geometry<P>>> {

    private DeserializationContext ctxt;
    public GeometryCollectionParser(Context<P> ctxt) {
        super(ctxt);
    }


    @Override
    public GeometryType forType() {
        return GeometryType.GEOMETRYCOLLECTION;
    }

    @Override
    public GeometryCollection<P, Geometry<P>> parse(JsonNode root) throws GeoJsonProcessingException {
        return null;
    }


}
