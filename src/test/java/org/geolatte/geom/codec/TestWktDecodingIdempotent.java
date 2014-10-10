package org.geolatte.geom.codec;

import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.PointSequenceBuilders;
import org.geolatte.geom.crs.CrsId;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class TestWktDecodingIdempotent {

    @Test
    public void testWktConversionShouldBeIdempotent() {
        Geometry original = new Point(PointSequenceBuilders.fixedSized(1, DimensionalFlag.d2D, new CrsId("EPSG", 31370))
                .add(68878.8400879, 169038.177124).toPointSequence());

        String originalWkt = "SRID=31370;POINT(68878.8400879 169038.177124)";
        assertEquals(originalWkt, Wkt.toWkt(original));


        Geometry geomFromOriginalWkt = Wkt.fromWkt(originalWkt);

        assertEquals(original,geomFromOriginalWkt);
    }


}
