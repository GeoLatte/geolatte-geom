package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.*;

public class SDOGTypeTest {

    @Test
    public void test_decode_coordinateDimension() {
        SDOGType sdogType = new SDOGType(3, 0, SdoGeometryType.UNKNOWN_GEOMETRY);
        assertEquals(3, sdogType.getDimension());
    }

    @Test
    public void test_decode_LRSDimension_when_no_measures() {
        SDOGType sdogType = new SDOGType(3, 0, SdoGeometryType.UNKNOWN_GEOMETRY);
        assertEquals(0, sdogType.getLRSDimension());
        assertFalse(sdogType.isLRSGeometry());
    }

    @Test
    public void test_decode_LRSDimension_when_measures(){
        SDOGType gtype = new SDOGType(3, 3, SdoGeometryType.LINE);
        assertEquals(3, gtype.getLRSDimension());
        assertTrue(gtype.isLRSGeometry());
    }

    @Test
    public void test_encode_coordinateDimension() {
        Geometry<G3D> point = point(WGS84_Z, g(2, 3, 4));
        SDOGType sdogType = new SDOGType(3, 0, SdoGeometryType.POINT);
        assertEquals(sdogType, SDOGType.gtypeFor(point));
    }

    @Test
    public void test_encode_LRSDimension_when_measures(){
        Geometry<G2DM> point = point(WGS84_M, gM(2, 3, 4));
        SDOGType gtype = new SDOGType(3, 3, SdoGeometryType.POINT);
        assertEquals(gtype, SDOGType.gtypeFor(point));
    }

    @Test
    public void test_encode_LRSDimension_when_measures_ZM(){
        Geometry<G3DM> point = point(WGS84_ZM, g(2, 3, 5, 4));
        SDOGType gtype = new SDOGType(4, 4, SdoGeometryType.POINT);
        assertEquals(gtype, SDOGType.gtypeFor(point));
    }

}
