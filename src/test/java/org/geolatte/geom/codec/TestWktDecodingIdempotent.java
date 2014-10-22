package org.geolatte.geom.codec;


import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.PointSequenceBuilders;
import org.geolatte.geom.crs.CrsId;
import org.junit.Test;
import sun.misc.FloatingDecimal;

import static org.junit.Assert.*;

/**
 *
 */
public class TestWktDecodingIdempotent {

    @Test
    public void testFastNumberReader(){
        WktTokenizer tokenizer = new WktTokenizer("169038.177124  ", new PostgisWktVariant(), CrsId.valueOf(31370));
        double v = tokenizer.fastReadNumber();
        assertEquals("169038.177124", String.valueOf(v));

    }

    @Test
    public void testWktConversionShouldBeIdempotent() {
        Geometry original = new Point(PointSequenceBuilders.fixedSized(1, DimensionalFlag.d2D, new CrsId("EPSG", 31370))
                .add(68878.8400879, 169038.177124).toPointSequence());

        String originalWkt = "SRID=31370;POINT(68878.8400879 169038.177124)";
        assertEquals(originalWkt, Wkt.toWkt(original));

        // geom to wkb to geom zou stabiel moeten blijven
        assertEquals(Wkt.fromWkt(Wkt.toWkt(original)),original);
    }
}
