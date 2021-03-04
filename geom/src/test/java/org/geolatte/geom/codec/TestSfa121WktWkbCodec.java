package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.C2D;
import org.geolatte.geom.C3D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.testcases.CodecUnitTestBase;
import org.geolatte.geom.codec.testcases.Sfa121WktWkbTestCases;
import org.geolatte.geom.codec.testcases.WktWkbCodecTestBase;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_3D_METER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestSfa121WktWkbCodec extends CodecUnitTestBase {

    final private Sfa121WktWkbTestCases testCases = new Sfa121WktWkbTestCases();


    @Test
    public void test_tolerate_missing_ZM_marker(){
        Geometry<C3D> geom = linestring(PROJECTED_3D_METER, c(100, 200, 10), c(300, 400, 20));
        assertEquals(geom, getWktDecoder().decode("LINESTRING(  100 200 10 ,300 400   20)"));
    }

    @Test
    public void test_wkt_variable_spacing(){
        Geometry<C3D> geom = linestring(PROJECTED_3D_METER, c(100, 200, 10), c(300, 400, 20));
        assertEquals(geom, getWktDecoder().decode("LINESTRING Z (  100 200 10 ,300 400   20)"));
        assertEquals(geom, getWktDecoder().decode("LINESTRING Z (100 200 10,300 400 20)"));
        assertEquals(geom, getWktDecoder().decode("LINESTRINGZ   (100 200 10  ,300 400  20 )"));
        assertEquals(geom, getWktDecoder().decode("LINESTRING Z(100 200 10  ,300 400  20 )"));
    }

    @Test
    public void test_wkt_variable_case(){
        Geometry<C3D> geom = linestring(PROJECTED_3D_METER, c(100, 200, 10), c(300, 400, 20));
        assertEquals(geom, getWktDecoder().decode("LineStrING z (  100 200 10 ,300 400   20)"));
        assertEquals(geom, getWktDecoder().decode("linestring z (100 200 10,300 400 20)"));
        assertEquals(geom, getWktDecoder().decode("LineStringZ   (100 200 10  ,300 400  20 )"));
        assertEquals(geom, getWktDecoder().decode("LINESTring z(100 200 10  ,300 400  20 )"));
    }

    @Test(expected = WkbDecodeException.class)
    public void test_failures() {
        //invalid WKB missing last point
        ByteBuffer wkb = ByteBuffer.from("01020000000200000052b81e85eb51f83f713d0ad7a3700340ec51b81e85eb1040");
        getWkbDecoder().decode(wkb);
    }

    @Override
    protected WktWkbCodecTestBase getTestCases() {
        return testCases;
    }

    @Override
    protected WktDecoder getWktDecoder() {
        return new Sfa121WktDecoder();
    }

    @Override
    protected WktEncoder getWktEncoder() {
        return new Sfa121WktEncoder();
    }

    @Override
    protected WkbDecoder getWkbDecoder() {
        return new Sfa121WkbDecoder();
    }

    @Override
    protected WkbEncoder getWkbEncoder() {
        return new Sfa121WkbEncoder();
    }

}
