package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.testcases.CodecUnitTestBase;
import org.geolatte.geom.codec.testcases.SFA110WkkWkbTestCases;
import org.geolatte.geom.codec.testcases.WktWkbCodecTestBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

public class TestSFA110WktWkbCodec  extends CodecUnitTestBase  {

    static final private Logger LOGGER = LoggerFactory.getLogger(TestSFA110WktWkbCodec.class);
    final private SFA110WkkWkbTestCases testCases = new SFA110WkkWkbTestCases();

    @Test
    public void test_codec() {

        for (Integer idx : testCases.getCases()) {
            ByteBuffer wkb = testCases.getWKB(idx);
            Geometry<?> geom = getWkbDecoder().decode(wkb);
            String wkt = testCases.getWKT(idx);
            assertEquals(format("WKB decoder gives incorrect result for case: %s (%d))", wkt, idx), testCases.getExpected(idx), geom);
//            assertEquals("WKB encoder gives incorrect result for case: " + testCase, wkb, getWkbEncoder().encode(geom, ByteOrder.NDR));
//            assertEquals("WKB encoder gives incorrect result for case: " + testCase, wkb, getWkbEncoder().encode(testCases.getExpected(testCase), ByteOrder.NDR));
        }
    }

    @Test
    public void test_3d_crs_passed() {
        //TODO check what happens when we have 3d, 2dm or 3dm CRS's
    }

    @Test
    public void test_failures() {
        //TODO check what happens on invalid input
    }

    @Test
    public void test_byte_order_switch() {
        //TODO check what happens when on a multipoint byeorder of constituent points is different from multipoint
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
        return new SFA110WkbDecoder();
    }

    @Override
    protected WkbEncoder getWkbEncoder() {
        return null;
    }
}
