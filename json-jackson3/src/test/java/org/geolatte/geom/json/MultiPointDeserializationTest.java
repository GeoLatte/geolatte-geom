package org.geolatte.geom.json;

import org.geolatte.geom.MultiPoint;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiPointDeserializationTest extends GeoJsonTest {

    @Test
    public void testDeserializeEmpty() throws IOException {
       MultiPoint<?> rec = mapper.readValue(emptyMultiPoint,MultiPoint.class);
       MultiPoint<?> expected = new MultiPoint(WGS84);
        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeSimple() throws IOException {
       MultiPoint<?> rec = mapper.readValue(multiPoint,MultiPoint.class);
       MultiPoint<?> expected = multipoint(WGS84, g(1, 2), g(3, 4));

        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeWithCRS() throws IOException {
       MultiPoint<?> rec = mapper.readValue(multiPointWithCrs,MultiPoint.class);
       MultiPoint<?> expected = multipoint(Crss.lambert72, c(1, 2), c(3, 4));

        assertEquals(expected, rec);
    }
}
