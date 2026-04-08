package org.geolatte.geom.json;

import org.geolatte.geom.MultiPolygon;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiPolygonDeserializationTest extends GeoJsonTest {

    @Test
    public void testEmtpy() throws IOException {
        MultiPolygon<?> rec = mapper.readValue(emptyMultiPolygon, MultiPolygon.class);
        MultiPolygon<?> exp = new MultiPolygon(WGS84);
        assertEquals(exp, rec);
    }


    @Test
    public void testDeserializeSimple() throws IOException {
        MultiPolygon<?> rec = mapper.readValue(multiPolygon, MultiPolygon.class);
        MultiPolygon<?> expected = multipolygon(
                polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1))),
                polygon(WGS84, ring(g(3, 3), g(3, 5), g(5, 5), g(5, 3), g(3, 3)))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeWithCRS() throws IOException {
        MultiPolygon<?> rec = mapper.readValue(multiPolygonWithCrs, MultiPolygon.class);
        MultiPolygon<?> expected =multipolygon(
                polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1))),
                polygon(lambert72, ring(c(3, 3), c(3, 5), c(5, 5), c(5, 3), c(3, 3)))
        );
        assertEquals(expected, rec);
    }

}
