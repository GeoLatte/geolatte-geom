package org.geolatte.geom.json;

import org.geolatte.geom.Geometries;
import org.geolatte.geom.Polygon;
import org.junit.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.geolatte.geom.json.GeoJsonStrings.polygon;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class PolygonSerializationTest extends GeoJsonTest {

    @Test
    public void testSerializeEmpty() throws JacksonException {
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        Polygon<?> p = Geometries.mkEmptyPolygon(WGS84);
        String rec = mapper.writeValueAsString(p);
        assertEquals(emptyPolygon, rec);
    }

    @Test
    public void testSerializeSimple() throws JacksonException {
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        Polygon<?> p = polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1)));
        String rec = mapper.writeValueAsString(p);
        assertEquals(polygon, rec);
    }

    @Test
    public void testSerializeWithCrs() throws JacksonException {
        Polygon<?> p = polygon(Crss.lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1)));
        String rec = mapper.writeValueAsString(p);
        assertEquals(polygonWithCrs, rec);
    }

}
