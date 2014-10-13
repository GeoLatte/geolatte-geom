package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CrsId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestWkbDecodingIdempotent {

    @Test
    public void testWkbConversionShouldBeStable() {
        Geometry original = new Point(PointSequenceBuilders.fixedSized(1, DimensionalFlag.d2D, new CrsId("EPSG", 31370))
                .add(68878.8400879, 169038.177124).toPointSequence());

        String originalWkb = "01010000208A7A000084020071EDD0F040DBFCBF6A71A20441";

        assertEquals(originalWkb, Wkb.toWkb(original).toString());

        // geom to wkb to geom blijft stabliel
        assertEquals(Wkb.fromWkb(Wkb.toWkb(original)),original);
    }
}
