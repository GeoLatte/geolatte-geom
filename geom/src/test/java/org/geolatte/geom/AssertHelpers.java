package org.geolatte.geom;

import org.geolatte.geom.codec.Wkt;

import static org.junit.Assert.assertTrue;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-28.
 */
public class AssertHelpers {

    private static final double DEFAULT_PRECISION = 0.05d;

    public static <P extends Position> void assertEquals(Geometry<P> g1, Geometry<P> g2) {
        GeometryEquality eq = new ApproximateGeometryEquality(DEFAULT_PRECISION);
        assertTrue(String.format("\nExpected:\t %s\nReceived:\t %s",
                g1 == null ? "<null>" : Wkt.toWkt(g1),
                g2 == null ? "<null>" : Wkt.toWkt(g2)), eq.equals(g1, g2));
    }

    public static <P extends Position> void assertEquals(Geometry<P> g1, Geometry<P> g2, double delta) {
        GeometryEquality eq = new ApproximateGeometryEquality(delta);
        assertTrue(String.format("\nExpected:\t %s\nReceived:\t %s",
                g1 == null ? "<null>" : Wkt.toWkt(g1),
                g2 == null ? "<null>" : Wkt.toWkt(g2)), eq.equals(g1, g2));
    }

    public static <P extends Position> void assertEquals(Envelope<P> g1, Envelope<P> g2, double delta) {
        WithinTolerancePositionEquality eq = new WithinTolerancePositionEquality(delta);
        assertTrue(String.format("\nExpected:\t %s\nReceived:\t %s",
                g1 == null ? "<null>" : g1,
                g2 == null ? "<null>" : g2),
                g1 != null
                        && g2 != null
                        && eq.equals(g1.lowerLeft(), g2.lowerLeft())
                        && eq.equals(g1.upperRight(), g2.upperRight()));
    }
}
