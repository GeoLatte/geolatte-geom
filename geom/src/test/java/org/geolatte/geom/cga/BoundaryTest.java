package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.BoundaryOp;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests {@link Boundary}.
 */
public class BoundaryTest {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;
    private static final CoordinateReferenceSystem<C3DM> crs3dm = CoordinateReferenceSystems.PROJECTED_3DM_METER;

    @Test
    public void boundaries() {
        assertBoundary("GEOMETRYCOLLECTION EMPTY", "POINT (1 1)");
        assertBoundary("GEOMETRYCOLLECTION EMPTY", "MULTIPOINT ((1 1), (2 2))");
        assertBoundary("GEOMETRYCOLLECTION EMPTY", "POINT EMPTY");
        assertBoundary("MULTIPOINT ((0 0), (2 0))", "LINESTRING (0 0, 1 1, 2 0)");
        assertBoundary("MULTIPOINT EMPTY", "LINESTRING (0 0, 1 1, 2 0, 0 0)");
        assertBoundary("MULTIPOINT EMPTY", "LINESTRING (0 0, 0 0)");
        assertBoundary("MULTIPOINT EMPTY", "LINESTRING EMPTY");
        // Mod-2 rule, in the order of first occurrence
        assertBoundary("MULTIPOINT ((2 0), (0 0))", "MULTILINESTRING ((2 0, 1 1), (1 1, 0 0))");
        assertBoundary("MULTIPOINT ((0 0), (1 1), (2 0), (1 2))", "MULTILINESTRING ((0 0, 1 1), (1 1, 2 0), (1 1, 1 2))");
        // a closed element has no boundary: (0 0) is in the boundary of one element
        assertBoundary("MULTIPOINT ((0 0), (2 2))", "MULTILINESTRING ((0 0, 1 0, 1 1, 0 0), (0 0, 2 2))");
        assertBoundary("MULTIPOINT EMPTY", "MULTILINESTRING ((0 0, 1 1), (1 1, 0 0))");
        assertBoundary("MULTIPOINT EMPTY", "MULTILINESTRING EMPTY");
        assertBoundary("LINESTRING (0 0, 4 0, 4 4, 0 4, 0 0)", "POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0))");
        assertBoundary("MULTILINESTRING ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 1 2, 2 2, 1 1))",
                "POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 1 2, 2 2, 1 1))");
        assertBoundary("MULTILINESTRING EMPTY", "POLYGON EMPTY");
        assertBoundary("MULTILINESTRING ((0 0, 1 0, 1 1, 0 0), (2 2, 3 2, 3 3, 2 2))",
                "MULTIPOLYGON (((0 0, 1 0, 1 1, 0 0)), ((2 2, 3 2, 3 3, 2 2)))");
        assertBoundary("MULTILINESTRING EMPTY", "MULTIPOLYGON EMPTY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void collection() {
        Boundary.of(g("GEOMETRYCOLLECTION (POINT (1 1), LINESTRING (0 0, 1 1))"));
    }

    /**
     * Endpoints are compared by X and Y only; the positions of the geometry are returned, with their Z and M values
     */
    @Test
    public void keepsPositions() {
        Geometry<C3DM> lines = Wkt.fromWkt("MULTILINESTRING ZM ((0 0 1 2, 1 1 3 4), (1 1 5 6, 2 0 7 8))", crs3dm);
        Geometry<C3DM> boundary = Boundary.of(lines);
        assertEquals(Wkt.fromWkt("MULTIPOINT ZM ((0 0 1 2), (2 0 7 8))", crs3dm), boundary);
        // -0.0 and 0.0 are the same point
        assertTrue(Boundary.of(g("MULTILINESTRING ((0 0, 1 1), (-0.0 -0.0, 1 2))")).getNumPositions() == 2);
    }

    /**
     * The same point sets as JTS
     */
    @Test
    public void sameAsJts() {
        Random random = new Random(5L);
        GeometryFactory factory = new GeometryFactory();
        for (int k = 0; k < 2000; k++) {
            int n = 1 + random.nextInt(4);
            org.locationtech.jts.geom.LineString[] lines = new org.locationtech.jts.geom.LineString[n];
            for (int i = 0; i < n; i++) {
                Coordinate[] c = new Coordinate[2 + random.nextInt(3)];
                for (int j = 0; j < c.length; j++) c[j] = new Coordinate(random.nextInt(3), random.nextInt(3));
                lines[i] = factory.createLineString(c);
            }
            org.locationtech.jts.geom.Geometry g = n == 1 ? lines[0] : factory.createMultiLineString(lines);
            Coordinate[] expected = new BoundaryOp(g).getBoundary().getCoordinates();
            Coordinate[] actual = JTS.to(Boundary.of(JTS.from(g))).getCoordinates();
            Arrays.sort(expected);
            Arrays.sort(actual);
            assertArrayEquals(g.toString(), expected, actual);
        }
    }

    private static void assertBoundary(String expected, String wkt) {
        assertEquals(wkt, g(expected), Boundary.of(g(wkt)));
    }

    private static Geometry<C2D> g(String wkt) {
        return Wkt.fromWkt(wkt, crs);
    }
}
