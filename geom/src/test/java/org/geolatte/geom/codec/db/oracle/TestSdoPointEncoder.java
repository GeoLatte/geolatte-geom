package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.LinearUnit;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.codec.db.oracle.SDOGeometryHelper.sdoGeometry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class TestSdoPointEncoder {

    final CoordinateReferenceSystem<G2D> wgs84 = CoordinateReferenceSystems.WGS84;
    final CoordinateReferenceSystem<G3D> wgs84z = CoordinateReferenceSystems.addVerticalSystem(wgs84, G3D.class,
            LinearUnit.METER);
    final CoordinateReferenceSystem<G3DM> wgs84zm = CoordinateReferenceSystems.addLinearSystem(wgs84z, G3DM.class,
            LinearUnit.METER);

    @Test
    public void test2DMPointNullCrs() {
        Point<C2DM> point = point(CoordinateReferenceSystems.PROJECTED_2DM_METER, cM(12, 14, 3));
        SDOGeometry expected = sdoGeometry(3301, -1, null, new int[]{1, 1, 1}, new Double[]{12d, 14d, 3d});
        assertEquals(expected, Encoders.encode(point));
    }


    @Test
    public void test3DPointWGS() {
        Point<G3D> point = point(wgs84z, g(12, 14, 3));
        SDOGeometry expected = sdoGeometry(3001, 4326, null, new int[]{1, 1, 1}, new Double[]{12d, 14d, 3d});
        assertEquals(expected, Encoders.encode(point));
    }

    @Test
    public void test3DMPointWGS() {
        Point<G3DM> point = point(wgs84zm, g(12, 14, 3, 6));
        SDOGeometry expected = sdoGeometry(4401, 4326, null, new int[]{1, 1, 1}, new Double[]{12d, 14d, 3d, 6d});
        assertEquals(expected, Encoders.encode(point));
    }

    @Test
    public void testPointEncoding3D(){
        Point<G3D> point = point(wgs84z, g(12, 14, 3));
        System.setProperty(Settings.USE_SDO_POINT, "true");
        SDOGeometry expected = sdoGeometry(3001, 4326, new SDOPoint(12d, 14d, 3d), null, null);
        assertEquals(expected, Encoders.encode(point));
    }

    @Test
    public void testPointEncoding2D(){
        Point<G2D> point = point(wgs84, g(12, 14));
        System.setProperty(Settings.USE_SDO_POINT, "true");
        SDOGeometry expected = sdoGeometry(2001, 4326, new SDOPoint(12d, 14d), null, null);
        assertEquals(expected, Encoders.encode(point));
        System.setProperty(Settings.USE_SDO_POINT, "false");
    }

    @Test
    public void testPointEncoding4DIgnoresUseSDOPointFeature(){
        Point<G3DM> point = point(wgs84zm, g(12, 14,3d, 6d));
        System.setProperty(Settings.USE_SDO_POINT, "true");
        SDOGeometry expected = sdoGeometry(4401, 4326, null, new int[]{1, 1, 1}, new Double[]{12d, 14d, 3d, 6d});
        assertEquals(expected, Encoders.encode(point));
        System.setProperty(Settings.USE_SDO_POINT, "false");
    }

    @Test
    public void testEmptyPointDecoding() {
        Point<G2D> emptyPoint = new Point<G2D>(wgs84);

        assertTrue(emptyPoint.isEmpty());
        SDOGeometry sdoGeometry = Encoders.encode(emptyPoint);
        Geometry<?> geom = Decoders.decode(sdoGeometry);
        assert (geom.isEmpty());
    }


        @Test
        public void testEmptyPointDecodingUsingSDOPoint() {
            System.setProperty(Settings.USE_SDO_POINT, "true");
            Point<G2D> emptyPoint = new Point<G2D>(wgs84);

            assertTrue(emptyPoint.isEmpty());
            SDOGeometry sdoGeometry = Encoders.encode(emptyPoint);
            Geometry<?> geom = Decoders.decode(sdoGeometry);
            assert (geom.isEmpty());
            System.setProperty(Settings.USE_SDO_POINT, "false");
        }
}
