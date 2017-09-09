package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.C3D;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.*;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.codec.db.oracle.SDOGeometryHelper.sdoGeometry;
import static org.geolatte.geom.codec.db.oracle.SDOGeometryHelper.sdoPoint;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/03/15.
 */
public class TestSdoGeometryPointDecoder {

    GeographicCoordinateReferenceSystem wgs84 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);

    @Test
    public void testPointDecodingWithSDOPoint(){

        SDOGeometry sdo = sdoGeometry(2001, 4326, sdoPoint(12,14),null, null);
        assertEquals(point(wgs84, g(12, 14)), Decoders.decode(sdo));

    }

    @Test
    public void testPointDecodingWithSDOPointNullCRS(){

        SDOGeometry sdo = sdoGeometry(2001, 0, sdoPoint(12,14),null, null);
        assertEquals(point( CoordinateReferenceSystems.PROJECTED_2D_METER, c(12, 14)), Decoders.decode(sdo));

        sdo = sdoGeometry(2001, 0, null,new int[]{ 1, 1, 1} , new Double[]{0d, -0.3d});
        assertEquals(point( CoordinateReferenceSystems.PROJECTED_2D_METER, c(0d, -0.3d)), Decoders.decode(sdo));

    }

    @Test
    public void testPointDecodingWith3DSDOPointNullCRS(){
        SDOGeometry sdo = sdoGeometry(3001, 0, sdoPoint(12,14,3),null, null);
        assertEquals(point( CoordinateReferenceSystems.PROJECTED_3D_METER, c(12, 14, 3)), Decoders.decode(sdo));

        sdo = sdoGeometry(3001, 0, null,new int[]{ 1, 1, 1} , new Double[]{12d, 14d, 3d});
        assertEquals(point( CoordinateReferenceSystems.PROJECTED_3D_METER, c(12, 14, 3)), Decoders.decode(sdo));
    }

    @Test
    public void testPointDecodingWith2DMNullCRS(){
        SDOGeometry sdo = sdoGeometry(3301, 0, null,new int[]{ 1, 1, 1} , new Double[]{12d, 14d, 3d});
        assertEquals(point( CoordinateReferenceSystems.PROJECTED_2DM_METER, cM(12, 14, 3)), Decoders.decode(sdo));
    }

    @Test
    public void testPoint4DDecoding(){
        SDOGeometry sdo = sdoGeometry(4401, 0, null,new int[]{ 1, 1, 1} , new Double[]{12d, 14d, 3d, 4d});
        assertEquals(point( CoordinateReferenceSystems.PROJECTED_3DM_METER, c(12, 14, 3, 4)), Decoders.decode(sdo));
    }

    @Test
    public void testPointMonD3Decoding(){
        CoordinateReferenceSystem crs = CoordinateReferenceSystems.addVerticalSystem(wgs84, LinearUnit.METER);
        crs = CoordinateReferenceSystems.addLinearSystem(crs, LinearUnit.METER);
        SDOGeometry sdo = sdoGeometry(4301, 4326, null,new int[]{ 1, 1, 1} , new Double[]{12d, 14d, 4d, 3d});
        assertEquals(point( crs, g(12, 14, 3, 4)), Decoders.decode(sdo));

    }

    @Test
    public void testPointUnknown3DDecoding(){
        //maxvalue as epsg-code because this is very unlikely ever to be a valid EPSG code
        SDOGeometry sdo = sdoGeometry(4301, Integer.MAX_VALUE, null,new int[]{ 1, 1, 1} , new Double[]{12d, 14d, 4d, 3d});
        CoordinateReferenceSystem crs = CoordinateReferenceSystems.mkProjected(Integer.MAX_VALUE, LinearUnit.METER);
        crs = CoordinateReferenceSystems.addVerticalSystem(crs, LinearUnit.METER);
        crs = CoordinateReferenceSystems.addLinearSystem(crs, LinearUnit.METER);
        Geometry<?> geom = Decoders.decode(sdo);
        assertEquals(point( crs, c(12, 14, 3, 4)), geom);
        assertEquals(Integer.MAX_VALUE, geom.getSRID());
    }
}
