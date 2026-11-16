package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReverseTest {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    private static Geometry<C2D> g(String wkt) {
        return Wkt.fromWkt(wkt, crs);
    }

    private static void check(String wkt, String expected) {
        Geometry<C2D> geometry = g(wkt);
        Geometry<C2D> reversed = Reverse.of(geometry);
        assertEquals(g(expected), reversed);
        assertEquals(geometry.getClass(), reversed.getClass());
        assertEquals(geometry, Reverse.of(reversed));
    }

    @Test
    public void simpleGeometries() {
        check("POINT (1 2)", "POINT (1 2)");
        check("LINESTRING (0 0, 1 0, 1 3)", "LINESTRING (1 3, 1 0, 0 0)");
        LinearRing<C2D> ring = ((Polygon<C2D>) g("POLYGON ((0 0, 1 0, 1 1, 0 0))")).getExteriorRing();
        LinearRing<C2D> reversed = Reverse.of(ring);
        assertEquals(((Polygon<C2D>) g("POLYGON ((0 0, 1 1, 1 0, 0 0))")).getExteriorRing(), reversed);
    }

    @Test
    public void polygonKeepsItsExteriorRingFirst() {
        String polygon = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (1 1, 1 2, 2 2, 1 1), (5 5, 5 6, 6 6, 5 5))";
        check(polygon, "POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0), (1 1, 2 2, 1 2, 1 1), (5 5, 6 6, 5 6, 5 5))");
        Geometry<C2D> reversed = Reverse.of(g(polygon));
        assertTrue(Validity.isValid(reversed));
        assertEquals(Area.of(g(polygon)), Area.of(reversed), 0);
    }

    @Test
    public void collectionsReverseTheirElements() {
        check("MULTIPOINT ((1 1), (2 2))", "MULTIPOINT ((2 2), (1 1))");
        check("MULTILINESTRING ((0 0, 1 0), (5 5, 6 6, 7 5))", "MULTILINESTRING ((7 5, 6 6, 5 5), (1 0, 0 0))");
        check("MULTIPOLYGON (((0 0, 1 0, 1 1, 0 0)), ((5 5, 6 5, 6 6, 5 5)))",
                "MULTIPOLYGON (((5 5, 6 6, 6 5, 5 5)), ((0 0, 1 1, 1 0, 0 0)))");
        check("GEOMETRYCOLLECTION (POINT (1 2), LINESTRING (0 0, 1 0), POLYGON ((0 0, 1 0, 1 1, 0 0)))",
                "GEOMETRYCOLLECTION (POLYGON ((0 0, 1 1, 1 0, 0 0)), LINESTRING (1 0, 0 0), POINT (1 2))");
    }

    @Test
    public void emptyGeometries() {
        for (String wkt : new String[]{"POINT EMPTY", "LINESTRING EMPTY", "POLYGON EMPTY", "MULTIPOLYGON EMPTY",
                "GEOMETRYCOLLECTION EMPTY"}) {
            check(wkt, wkt);
        }
    }

    @Test
    public void keepsTheCoordinateReferenceSystemAndMeasures() {
        CoordinateReferenceSystem<C3DM> crs3dm = CoordinateReferenceSystems.PROJECTED_3DM_METER;
        Geometry<C3DM> line = Wkt.fromWkt("LINESTRING ZM (0 0 5 1, 10 0 6 2)", crs3dm);
        Geometry<C3DM> reversed = Reverse.of(line);
        assertEquals(Wkt.fromWkt("LINESTRING ZM (10 0 6 2, 0 0 5 1)", crs3dm), reversed);
        assertEquals(crs3dm, reversed.getCoordinateReferenceSystem());
    }
}
