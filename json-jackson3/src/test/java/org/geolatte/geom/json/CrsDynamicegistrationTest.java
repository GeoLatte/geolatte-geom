package org.geolatte.geom.json;

import org.geolatte.geom.G3D;
import org.geolatte.geom.Point;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.json.Crss.wgs3D;
import static org.geolatte.geom.json.GeoJsonStrings.pointText3D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Unit test to verify that an extended CRS is created only once, and then reused for all subsequent deserializations
 * Created by Karel Maesen, Geovise BVBA on 12/09/2018.
 */
public class CrsDynamicegistrationTest extends GeoJsonTest {

    @Test
    public void testDeserializePointText3D() throws IOException {

        Point<?> pnt1 = mapper.readValue(pointText3D, Point.class);
        Point<?> pnt2 = mapper.readValue(pointText3D, Point.class);

        assertSame(pnt1.getCoordinateReferenceSystem(), pnt2.getCoordinateReferenceSystem());
    }




}
