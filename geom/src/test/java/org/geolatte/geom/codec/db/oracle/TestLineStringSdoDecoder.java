package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.CrsMock;
import org.geolatte.geom.G2D;
import org.geolatte.geom.G3D;
import org.geolatte.geom.LineString;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.WGS84_Z;
import static org.geolatte.geom.CrsMock.WGS84_ZM;
import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.codec.db.oracle.SDOGeometryHelper.sdoGeometry;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/03/15.
 */
public class TestLineStringSdoDecoder {

    @Test
    public void test2DLineString() {
        SDOGeometry sdo = sdoGeometry(2002, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(linestring(WGS84, g(3, 4), g(5, 6), g(7, 8)), Decoders.decode(sdo));
    }

    @Test
    public void test3DLineString() {
        SDOGeometry sdo = sdoGeometry(3002, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(linestring(WGS84_Z, g(3, 4, 5), g(6, 7, 8)), Decoders.decode(sdo));
    }

    @Test
    public void test3DLineStringNullSRID() {
        SDOGeometry sdo = sdoGeometry(3002, 0, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(linestring(CoordinateReferenceSystems.PROJECTED_3D_METER, c(3, 4, 5), c(6, 7, 8)), Decoders.decode(sdo));
    }

    @Test
    public void test2DMLineString() {
        SDOGeometry sdo = sdoGeometry(3302, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(linestring(CrsMock.WGS84_M, gM(3, 4, 5), gM(6, 7, 8)), Decoders.decode(sdo));
    }

    @Test
    public void test3DMLineString() {
        SDOGeometry sdo = sdoGeometry(4402, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d});
        assertEquals(linestring(WGS84_ZM, g(3, 4, 5, 6), g(7, 8, 9d, 10d)), Decoders.decode(sdo));
    }

    @Test
    public void test3DArcLineString() {
        SDOGeometry sdo = sdoGeometry(3002, 4326, null, new int[]{1, 2, 2}, new Double[]{1d, 1d, 1d, 2d, 2d, 2d, 2d, 1d, 3d});
        LineString<G3D> received = (LineString<G3D>) Decoders.decode(sdo);
        assertEquals(WGS84_Z, received.getCoordinateReferenceSystem());
        assertEquals(g(1, 1, 1), received.getStartPosition());
        assertEquals(g(2, 1, 3), received.getEndPosition());
    }

    @Test
    public void test2DCompoundLineString() {
        SDOGeometry sdo = sdoGeometry(2002, 4326, null, new int[]{1, 4, 2, 1, 2, 1, 3, 2, 2}, new Double[]{0d, 10d, 10d, 14d, 6d, 10d, 14d, 10d});
        LineString<G2D> received = (LineString<G2D>) Decoders.decode(sdo);
        assertEquals(g(0, 10), received.getStartPosition());
        assertEquals(g(14, 10), received.getEndPosition());
    }

    @Test
    public void testEmptyLineString(){
        SDOGeometry sdo = sdoGeometry(2002, 4326, null, new int[]{1, 2, 1}, new Double[]{});
        assertEquals(linestring(WGS84), Decoders.decode(sdo));
    }

}

