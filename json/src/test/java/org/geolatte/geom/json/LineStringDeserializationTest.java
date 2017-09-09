package org.geolatte.geom.json;

import org.geolatte.geom.LineString;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class LineStringDeserializationTest extends GeoJsonTest {

    @Test
    public void testDeserializeEmpty() throws IOException {
        LineString<?> rec = mapper.readValue(emptyLineString, LineString.class);
        LineString<?> expected = new LineString(WGS84);
        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeSimple() throws IOException {
        LineString<?> rec = mapper.readValue(simpleLineString, LineString.class);
        LineString<?> expected = linestring(WGS84, g(1, 2), g(3, 4));

        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeWithCRS() throws IOException {
        LineString<?> rec = mapper.readValue(lineStringWithCrs, LineString.class);
        LineString<?> expected = linestring(Crss.lambert72, c(1, 2), c(3, 4));

        assertEquals(expected, rec);
    }



}
