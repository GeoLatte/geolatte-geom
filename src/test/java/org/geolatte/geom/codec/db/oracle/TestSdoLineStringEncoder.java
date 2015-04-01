package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.*;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.codec.db.oracle.SDOGeometryHelper.sdoGeometry;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addVerticalSystem;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class TestSdoLineStringEncoder {


    GeographicCoordinateReferenceSystem wgs84 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);
    CompoundCoordinateReferenceSystem<G2DM> wgs84M = addLinearSystem(wgs84, G2DM.class, LinearUnit.METER);
    CompoundCoordinateReferenceSystem<G3D> wgs84Z = addVerticalSystem(wgs84, G3D.class, LinearUnit.METER);
    CompoundCoordinateReferenceSystem<G3DM> wgs84ZM = addLinearSystem(wgs84Z, G3DM.class, LinearUnit.METER);

    @Test
    public void test2DLineString() {
        LineString<G2D> lineString = linestring(wgs84, g(3, 4), g(5, 6), g(7, 8));
        SDOGeometry expected = sdoGeometry(2002, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(expected, Encoders.encode(lineString));
    }

    @Test
    public void test3DLineString() {
        LineString<G3D> linestring = linestring(wgs84Z, g(3, 4, 5), g(6, 7, 8));
        SDOGeometry expected = sdoGeometry(3002, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(expected, Encoders.encode(linestring));
    }

    @Test
    public void test3DLineStringNullSRID() {
        LineString<C3D> linestring = linestring(CoordinateReferenceSystems.PROJECTED_3D_METER, c(3, 4, 5), c(6, 7, 8));
        SDOGeometry expected = sdoGeometry(3002, -1, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(expected, Encoders.encode(linestring));
    }


    @Test
    public void test2DMLineString() {
        LineString<G2DM> linestring = linestring(wgs84M, gM(3, 4, 5), gM(6, 7, 8));
        SDOGeometry expected = sdoGeometry(3302, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d});
        assertEquals(expected, Encoders.encode(linestring));
    }

    @Test
    public void test3DMLineString() {
        LineString<G3DM> linestring = linestring(wgs84ZM, g(3, 4, 5, 6), g(7, 8, 9d, 10d));
        SDOGeometry expected = sdoGeometry(4402, 4326, null, new int[]{1, 2, 1}, new Double[]{3d, 4d, 5d, 6d, 7d, 8d, 9d,
                10d});
        assertEquals(expected, Encoders.encode(linestring));
    }


}
