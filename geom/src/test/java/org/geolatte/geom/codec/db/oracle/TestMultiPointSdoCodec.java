package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.MultiPoint;
import static org.geolatte.geom.builder.DSL.*;

import org.junit.Test;

import static org.geolatte.geom.CrsMock.WGS84;
import static org.junit.Assert.assertEquals;

public class TestMultiPointSdoCodec {

    @Test
    public void test_multipoint_codec() {
        MultiPoint<G2D> mp = multipoint(WGS84, point(g(10, 5)), point(g(12.0, 6.0)));
        SDOGeometry sdoGeometry = Encoders.encode(mp);
        Geometry<?> geometry = Decoders.decode(sdoGeometry);
        assertEquals(mp, geometry);
    }

    @Test
    public void test_multipoint_with_empty_point_codec() {
        MultiPoint<G2D> mp = multipoint(WGS84);
        SDOGeometry sdoGeometry = Encoders.encode(mp);
        Geometry<?> geometry = Decoders.decode(sdoGeometry);
        assertEquals(mp, geometry);
    }

    @Test
    public void test_multipoint_with_point_cluster_decode() {
        SDOGeometry sdoGeometry = SDOGeometryHelper.sdoGeometry(2005, 4326, null, new int[]{1, 1, 3}, new Double[]{10., 5., 2., 3.});
        MultiPoint<G2D> mp = multipoint(WGS84, point(g(10.0, 5.0)), point(g(2.0, 3.0)));
        Geometry<?> geometry = Decoders.decode(sdoGeometry);
        assertEquals(mp, geometry);
    }

    @Test
    public void test_multipoint_with_point_cluster_encode() {
        SDOGeometry sdoGeometry = SDOGeometryHelper.sdoGeometry(2005, 4326, null, new int[]{1, 1, 2}, new Double[]{10., 5., 2., 3.});
        MultiPoint<G2D> mp = multipoint(WGS84, point(g(10.0, 5.0)), point(g(2.0, 3.0)));
        SDOGeometry encoded = Encoders.encode(mp);
        assertEquals(sdoGeometry, encoded);
    }



}
