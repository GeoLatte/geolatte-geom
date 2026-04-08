package org.geolatte.geom.json;

import org.geolatte.geom.Geometry;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72ZM;
import static org.geolatte.geom.json.GeoJsonStrings.multiLineString;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithCrs34D;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 17/09/17.
 */
public class GeometryDeserializationTest extends GeoJsonTest {

    @Test
    public void testDeserializeML() throws IOException {
        Geometry<?> rec = mapper.readValue(multiLineString, Geometry.class);
        Geometry<?> expected = multilinestring(
                linestring(WGS84, g(1, 1), g(1, 2)),
                linestring(WGS84, g(3, 4), g(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializePoint() throws IOException {
        Geometry<?> rec = mapper.readValue(pointTextWithCrs34D, Geometry.class);
        Geometry<?> expected = point(lambert72ZM, c(1,2,3,4));
        assertEquals(expected, rec);
    }
}
