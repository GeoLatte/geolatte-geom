package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.codec.db.oracle.SDOGeometryHelper.sdoGeometry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPointSdoDecoder {

    @Test
    public void testEmptyPointDecoding() {
        Point<G2D> emptyPoint = new Point<G2D>(WGS84);

        assertTrue(emptyPoint.isEmpty());
        SDOGeometry sdoGeometry = Encoders.encode(emptyPoint);
        Geometry<?> geom = Decoders.decode(sdoGeometry);
        assert (geom.isEmpty());
    }


    @Test
    public void testEmptyPointDecodingUsingSDOPoint() {
        System.setProperty(Settings.USE_SDO_POINT, "true");
        Point<G2D> emptyPoint = new Point<G2D>(WGS84);

        assertTrue(emptyPoint.isEmpty());
        SDOGeometry sdoGeometry = Encoders.encode(emptyPoint);
        Geometry<?> geom = Decoders.decode(sdoGeometry);
        assert (geom.isEmpty());
        System.setProperty(Settings.USE_SDO_POINT, "false");
    }

    @Test
    public void test2DMPointNullCrs() {
        Point<C2DM> expected = point(CoordinateReferenceSystems.PROJECTED_2DM_METER, cM(12, 14, 3));
        SDOGeometry sdo = sdoGeometry(3301, -1, null, new int[]{1, 1, 1}, new Double[]{12d, 14d, 3d});
        assertEquals(expected, Decoders.decode(sdo));
    }


    @Test
    public void test3DPointWGS() {
        Point<G3D> expected = point(WGS84_Z, g(12, 14, 3));
        SDOGeometry point = sdoGeometry(3001, 4326, null, new int[]{1, 1, 1}, new Double[]{12d, 14d, 3d});
        assertEquals(expected, Decoders.decode(point));
    }

    @Test
    public void test3DMPointWGS() {
        Point<G3DM> expected = point(WGS84_ZM, g(12, 14, 3, 6));
        SDOGeometry point  = sdoGeometry(4401, 4326, null, new int[]{1, 1, 1}, new Double[]{12d, 14d, 3d, 6d});
        assertEquals(expected, Decoders.decode(point));
    }

    @Test
    public void testPointEncoding3D() {
        Point<G3D> expected = point(WGS84_Z, g(12, 14, 3));
        System.setProperty(Settings.USE_SDO_POINT, "true");
        SDOGeometry point = sdoGeometry(3001, 4326, new SDOPoint(12d, 14d, 3d), null, null);
        assertEquals(expected, Decoders.decode(point));
    }

    @Test
    public void testPointEncoding2D() {
        Point<G2D> expected = point(WGS84, g(12, 14));
        System.setProperty(Settings.USE_SDO_POINT, "true");
        SDOGeometry point = sdoGeometry(2001, 4326, new SDOPoint(12d, 14d), null, null);
        assertEquals(expected, Decoders.decode(point));
        System.setProperty(Settings.USE_SDO_POINT, "false");
    }

    @Test
    public void testPointEncoding4DIgnoresUseSDOPointFeature() {
        Point<G3DM> expected = point(WGS84_ZM, g(12, 14, 3d, 6d));
        System.setProperty(Settings.USE_SDO_POINT, "true");
        SDOGeometry point = sdoGeometry(4401, 4326, null, new int[]{1, 1, 1}, new Double[]{12d, 14d, 3d, 6d});
        assertEquals(expected, Decoders.decode(point));
        System.setProperty(Settings.USE_SDO_POINT, "false");
    }
    
}
