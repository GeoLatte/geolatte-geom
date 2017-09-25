package org.geolatte.geom.json;

import org.geolatte.geom.MultiLineString;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiLineStringDeserializationTest extends GeoJsonTest {

    @Test
    public void testDeserializeEmpty() throws IOException {
        MultiLineString<?> rec = mapper.readValue(emptyMultiLineString, MultiLineString.class);
        MultiLineString<?> expected = new MultiLineString<>(WGS84);
        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeSimple() throws IOException {
        MultiLineString<?> rec = mapper.readValue(multiLineString, MultiLineString.class);
        MultiLineString<?> expected = multilinestring(
                linestring(WGS84, g(1, 1), g(1, 2)),
                linestring(WGS84, g(3, 4), g(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeWithCRS() throws IOException {
        MultiLineString<?> rec = mapper.readValue(multiLineStringWithCrs, MultiLineString.class);
        MultiLineString<?> expected = multilinestring(
                linestring(Crss.lambert72, c(1, 1), c(1, 2)),
                linestring(Crss.lambert72, c(3, 4), c(5, 6))
        );
        assertEquals(expected, rec);
    }


}
