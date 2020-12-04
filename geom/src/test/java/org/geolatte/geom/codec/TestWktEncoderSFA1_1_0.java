package org.geolatte.geom.codec;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;


public class TestWktEncoderSFA1_1_0 {

    public static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    WktEncoder encoder() {
        return Wkt.newEncoder(Wkt.Dialect.SFA_1_1_0);
    }

    @Test
    public void test_point() {
        Point<C2D> pnt = point(crs, c(1.52,2.43));
        assertEquals("POINT(1.52 2.43)", encoder().encode(pnt));
    }

}
