package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.testcases.CodecUnitTestBase;
import org.geolatte.geom.codec.testcases.Sfa110WktWkbTestCases;
import org.geolatte.geom.codec.testcases.WktWkbCodecTestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;

public class TestSfa110WktWkbCodec extends CodecUnitTestBase  {

    static final private Logger LOGGER = LoggerFactory.getLogger(TestSfa110WktWkbCodec.class);
    final private Sfa110WktWkbTestCases testCases = new Sfa110WktWkbTestCases();

    @Test
    public void test_3d_crs_encoded() {
        Geometry<C2D> geom = point(mercator, c(1, 2));
        Geometry<C3D> pgeom = point(mercatorZ, c(1, 2, 3));
        ByteBuffer wkb = getWkbEncoder().encode(pgeom);
        ByteBuffer expected = getWkbEncoder().encode(geom);
        assertEquals(expected, wkb);
        Geometry<C2D> dgeom = getWkbDecoder().decode(wkb, mercator);
        assertEquals(point(mercator, c(1,2)), dgeom);
    }

    @Test
    public void test_3d_crs_decoded() {
        Geometry<C2D> pgeom = point(mercator, c(1, 2));
        ByteBuffer wkb = getWkbEncoder().encode(pgeom);
        Geometry<C3D> geom = getWkbDecoder().decode(wkb, mercatorZ);
        assertEquals(point(mercatorZ, c(1,2,0)), geom);
    }

    @Test(expected = WkbDecodeException.class)
    public void test_failures() {
        //invalid WKB missing last point
        ByteBuffer wkb = ByteBuffer.from("01020000000200000052b81e85eb51f83f713d0ad7a3700340ec51b81e85eb1040");
        getWkbDecoder().decode(wkb);
    }

    @Ignore
    @Test
    public void test_specified_crs_is_invalid() {
       //WKB only supports 2D
    }

    @Override
    protected WktWkbCodecTestBase getTestCases() {
        return testCases;
    }

    @Override
    protected WktDecoder getWktDecoder() {
        return new Sfa110WktDecoder();
    }

    @Override
    protected WktEncoder getWktEncoder() {
        return new Sfa110WktEncoder();
    }

    @Override
    protected WkbDecoder getWkbDecoder() {
        return new Sfa110WkbDecoder();
    }

    @Override
    protected WkbEncoder getWkbEncoder() {
        return new Sfa110WkbEncoder();
    }
}
