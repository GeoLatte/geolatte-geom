package org.geolatte.geom.json;

import org.geolatte.geom.Polygon;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.geolatte.geom.json.GeoJsonStrings.polygon;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class PolygonDeserializerTest extends GeoJsonTest {

    @Test
    public void testEmtpy() throws IOException {
        Polygon<?> rec = mapper.readValue(emptyPolygon, Polygon.class);
        Polygon<?> exp = new Polygon<>(WGS84);
        assertEquals(exp, rec);
    }


    @Test
    public void testDeserializeSimple() throws IOException {
        Polygon<?> rec = mapper.readValue(polygon, Polygon.class);
        Polygon<?> expected = polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1)));
        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeWithCRS() throws IOException {
        Polygon<?> rec = mapper.readValue(polygonWithCrs, Polygon.class);
        Polygon<?> expected = polygon(Crss.lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1)));
        assertEquals(expected, rec);
    }


}
