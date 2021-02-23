package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.Unit;
import org.geolatte.geom.support.PostgisTestCases;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestPostgisWkb2EncoderDecoder extends TestPostgisWkbEncoderDecoder{

    @Test
    public void test_empty_point() {
        Geometry<G2D> g = new Point<G2D>(CoordinateReferenceSystems.mkGeographic(Unit.DEGREE));
        ByteBuffer buf = Wkb.newEncoder(Wkb.Dialect.POSTGIS_EWKB_2).encode(g);
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.EMPTY_POINT_USING_NAN);
        Geometry<?> geom = decode(byteBuffer);
        assertTrue(geom.isEmpty());
        testEncoding(byteBuffer, geom);
    }

    @Override
    protected WkbEncoder encoder() {
        return new PostgisWkb2Encoder();
    }

    @Override
    protected WkbDecoder decoder() {
        return new PostgisWkb2Decoder();
    }
}
