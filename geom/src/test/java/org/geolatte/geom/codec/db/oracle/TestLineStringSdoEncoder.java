package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.CrsMock;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.WGS84_Z;
import static org.geolatte.geom.CrsMock.WGS84_ZM;
import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.codec.db.oracle.SDOGeometryHelper.sdoGeometry;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
@SuppressWarnings("unchecked")
public class TestLineStringSdoEncoder {

    @Test
    public void test2DLineString() {
        SDOGeometry sdo = sdoGeometry(2002, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(sdo, Encoders.encode(linestring(WGS84, g(3, 4), g(5, 6), g(7, 8))));
    }

    @Test
    public void test3DLineString() {
        SDOGeometry sdo = sdoGeometry(3002, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(sdo,
                Encoders.encode(linestring(WGS84_Z, g(3, 4, 5), g(6, 7, 8))));
    }

    @Test
    public void test3DLineStringNullSRID() {
        SDOGeometry sdo = sdoGeometry(3002, -1, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(sdo,
                Encoders.encode(linestring(CoordinateReferenceSystems.PROJECTED_3D_METER, c(3, 4, 5), c(6, 7, 8))));
    }


    @Test
    public void test2DMLineString() {
        SDOGeometry sdo = sdoGeometry(3302, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(sdo,
                Encoders.encode(linestring(CrsMock.WGS84_M, gM(3, 4, 5), gM(6, 7, 8))));
    }

    @Test
    public void test3DMLineString() {
        SDOGeometry sdo = sdoGeometry(4402, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d, 9d,
                10d});
        assertEquals(sdo,
                Encoders.encode(linestring(WGS84_ZM, g(3, 4, 5, 6), g(7, 8, 9d, 10d))));
    }

    @Test
    public void testEmptyLineString(){
        SDOGeometry sdo = sdoGeometry(2002, 4326, null, new int[]{1, 2, 1}, new Double[]{});
        assertEquals(sdo, Encoders.encode( linestring(WGS84) ));
    }

}
