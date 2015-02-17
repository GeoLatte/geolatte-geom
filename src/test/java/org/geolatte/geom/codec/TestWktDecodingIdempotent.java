package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.ProjectedCoordinateReferenceSystem;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.point;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestWktDecodingIdempotent {

    ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);

    @Test
    public void testFastNumberReader() {
        WktTokenizer tokenizer = new WktTokenizer("169038.177124  ", new PostgisWktVariant(), crs);
        double v = tokenizer.fastReadNumber();
        assertEquals("169038.177124", String.valueOf(v));

    }

    @Test
    public void testWktConversionShouldBeIdempotent() {
        Geometry original = point(crs, c(68878.8400879, 169038.177124));

        String originalWkt = "SRID=31370;POINT(68878.8400879 169038.177124)";
        assertEquals(originalWkt, Wkt.toWkt(original));

        // geom to wkb to geom zou stabiel moeten blijven
        assertEquals(Wkt.fromWkt(Wkt.toWkt(original)), original);
    }
}
