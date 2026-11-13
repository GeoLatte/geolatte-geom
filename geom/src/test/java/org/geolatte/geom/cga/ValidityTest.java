package org.geolatte.geom.cga;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.operation.valid.IsSimpleOp;
import org.locationtech.jts.operation.valid.IsValidOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.geolatte.geom.cga.RelateTest.geometry;
import static org.geolatte.geom.cga.Validity.Problem.*;
import static org.junit.Assert.*;

/**
 * Tests {@link Validity} and {@link Simplicity}.
 */
public class ValidityTest {

    private final Random random = new Random(29L);
    private final GeometryFactory factory = new GeometryFactory();

    // ---------------------------------------------------------------------------------------------------------
    // validity: basic cases
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void validGeometries() {
        assertProblem(NONE, "POINT (1 2)");
        assertProblem(NONE, "POINT EMPTY");
        assertProblem(NONE, "MULTIPOINT ((1 2), (1 2))");
        assertProblem(NONE, "LINESTRING (0 0, 1 1, 0 1, 1 0)");
        assertProblem(NONE, "LINESTRING (0 0, 0 0, 1 1)");
        assertProblem(NONE, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))");
        assertProblem(NONE, "POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
        assertProblem(NONE, "POLYGON ((0 0, 10 0, 10 0, 10 10, 0 10, 0 0))");
        assertProblem(NONE, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 2 4, 4 4, 4 2, 2 2))");
        assertProblem(NONE, "POLYGON EMPTY");
        assertProblem(NONE, "MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), ((2 0, 3 0, 3 1, 2 1, 2 0)))");
        assertProblem(NONE, "GEOMETRYCOLLECTION (POINT (1 1), POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0)))");
        // overlapping members of a collection are allowed
        assertProblem(NONE, "GEOMETRYCOLLECTION (POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0)), "
                + "POLYGON ((5 5, 15 5, 15 15, 5 15, 5 5)))");
    }

    @Test
    public void touchingRings() {
        // a hole touching the shell in a vertex, and in the interior of a shell segment
        assertProblem(NONE, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (0 0, 4 2, 2 4, 0 0))");
        assertProblem(NONE, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (5 0, 6 2, 4 2, 5 0))");
        // two holes touching each other, one touching the shell
        assertProblem(NONE, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (5 0, 6 2, 4 2, 5 0), (5 2, 6 4, 4 4, 5 2))");
        // three holes in one point
        assertProblem(NONE, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (5 5, 7 4, 7 6, 5 5), (5 5, 3 6, 3 4, 5 5), "
                + "(5 5, 6 8, 4 8, 5 5))");
        // polygons touching in a point
        assertProblem(NONE, "MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), ((1 1, 2 1, 2 2, 1 2, 1 1)))");
        assertProblem(NONE, "MULTIPOLYGON (((0 0, 2 0, 2 2, 0 2, 0 0)), ((2 1, 3 0, 3 2, 2 1)))");
        // a polygon in the hole of another, touching it
        assertProblem(NONE, "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 8 2, 8 8, 2 8, 2 2)), "
                + "((2 2, 5 3, 3 5, 2 2)))");
        // two polygons touching in two points: they enclose part of the exterior
        assertProblem(NONE, "MULTIPOLYGON (((0 0, 4 0, 4 1, 0 1, 0 0)), ((0 1, 2 3, 4 1, 4 4, 0 4, 0 1)))");
    }

    @Test
    public void invalidCoordinates() {
        assertProblem(TOO_FEW_POINTS, "LINESTRING (1 1, 1 1)");
        assertProblem(TOO_FEW_POINTS, "POLYGON ((0 0, 1 1, 0 0, 0 0))");
        assertProblem(TOO_FEW_POINTS, "MULTILINESTRING ((0 0, 1 1), (2 2, 2 2))");
    }

    @Test
    public void selfIntersections() {
        // bow tie
        assertProblem(RING_SELF_INTERSECTION, "POLYGON ((0 0, 10 10, 10 0, 0 10, 0 0))");
        // a ring touching itself in a vertex (an inverted shell)
        assertProblem(RING_SELF_INTERSECTION, "POLYGON ((0 0, 10 0, 10 10, 5 0, 0 10, 0 0))");
        assertProblem(RING_SELF_INTERSECTION, "POLYGON ((0 0, 10 0, 5 0, 5 5, 0 0))");
        // a spike
        assertProblem(RING_SELF_INTERSECTION, "POLYGON ((0 0, 10 0, 10 10, 10 20, 10 10, 0 10, 0 0))");
        // collapsed
        assertProblem(RING_SELF_INTERSECTION, "POLYGON ((0 0, 10 0, 20 0, 0 0))");
        // crossing rings
        assertProblem(SELF_INTERSECTION, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (5 5, 15 5, 15 6, 5 6, 5 5))");
        // a hole sharing an edge with the shell
        assertProblem(SELF_INTERSECTION, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (0 0, 5 0, 5 5, 0 0))");
        // polygons sharing an edge
        assertProblem(SELF_INTERSECTION, "MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), ((1 0, 2 0, 2 1, 1 1, 1 0)))");
        // rings that cross in common vertices, and meet nowhere else
        assertProblem(SELF_INTERSECTION, "MULTIPOLYGON (((1 0, 2 1, 1 2, 0 1, 1 0)), ((1 0, 3 1, 1 2, 1 0)))");
        assertProblem(SELF_INTERSECTION, "POLYGON ((-1 -1, 5 -1, 5 4, -1 4, -1 -1), (1 0, 2 1, 1 2, 0 1, 1 0), "
                + "(1 0, 3 1, 1 2, 1 0))");
    }

    @Test
    public void nesting() {
        assertProblem(HOLE_OUTSIDE_SHELL, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (20 20, 21 20, 21 21, 20 20))");
        // outside, touching the shell
        assertProblem(HOLE_OUTSIDE_SHELL, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (10 5, 12 4, 12 6, 10 5))");
        // a hole containing the shell
        assertProblem(HOLE_OUTSIDE_SHELL, "POLYGON ((2 2, 4 2, 4 4, 2 4, 2 2), (0 0, 10 0, 10 10, 0 10, 0 0))");
        assertProblem(NESTED_HOLES, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (1 1, 9 1, 9 9, 1 9, 1 1), "
                + "(2 2, 3 2, 3 3, 2 2))");
        assertProblem(NESTED_SHELLS, "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0)), ((2 2, 3 2, 3 3, 2 2)))");
        assertProblem(NESTED_SHELLS, "MULTIPOLYGON (((2 2, 3 2, 3 3, 2 2)), ((0 0, 10 0, 10 10, 0 10, 0 0)))");
        // a polygon in a hole is fine, but not when it contains the hole
        assertProblem(NESTED_SHELLS, "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0), (4 4, 6 4, 6 6, 4 6, 4 4)), "
                + "((3 3, 7 3, 7 7, 3 7, 3 3)))");
        // nested, touching
        assertProblem(NESTED_SHELLS, "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0)), ((0 0, 3 2, 2 3, 0 0)))");
        // the hole of a polygon outside its shell, in the interior of another polygon
        assertProblem(HOLE_OUTSIDE_SHELL, "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0)), "
                + "((20 0, 30 0, 30 10, 20 10, 20 0), (2 2, 3 2, 3 3, 2 2)))");
    }

    /**
     * Rings all of whose vertices touch other rings: their nesting is decided by the direction of an edge
     */
    @Test
    public void ringsTouchingInAllVertices() {
        // a triangular hole whose vertices touch three other holes
        assertProblem(NONE, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (4 4, 6 4, 5 6, 4 4), (4 4, 3 4, 3 3, 4 4), "
                + "(6 4, 7 3, 7 4, 6 4), (5 6, 6 7, 4 7, 5 6))");
        // a triangle inscribed in a hole
        assertProblem(NESTED_HOLES, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 8 2, 5 8, 2 2), "
                + "(5 2, 6.5 5, 3.5 5, 5 2))");
        assertProblem(NESTED_HOLES, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (5 2, 6.5 5, 3.5 5, 5 2), "
                + "(2 2, 8 2, 5 8, 2 2))");
        // a polygon inscribed in a hole of another
        assertProblem(NONE, "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 8 2, 5 8, 2 2)), "
                + "((5 2, 6.5 5, 3.5 5, 5 2)))");
        // a polygon inscribed in another
        assertProblem(NESTED_SHELLS, "MULTIPOLYGON (((2 2, 8 2, 5 8, 2 2)), ((5 2, 6.5 5, 3.5 5, 5 2)))");
    }

    @Test
    public void disconnectedInterior() {
        // a hole touching the shell in two points
        assertProblem(DISCONNECTED_INTERIOR, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (5 0, 10 5, 5 5, 5 0))");
        // two holes touching each other in two points
        assertProblem(DISCONNECTED_INTERIOR, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 8 2, 5 5, 2 2), "
                + "(2 2, 5 1, 8 2, 5 1.5, 2 2))");
        // a chain of holes from shell to shell
        assertProblem(DISCONNECTED_INTERIOR, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (0 5, 3 4, 3 6, 0 5), "
                + "(3 5, 7 4, 7 6, 3 5), (7 5, 10 5, 8 6, 7 5))");
    }

    @Test
    public void linearRings() {
        assertTrue(Validity.isValid(ring("LINESTRING (0 0, 1 0, 1 1, 0 0)")));
        assertEquals(TOO_FEW_POINTS, Validity.of(ring("LINESTRING (0 0, 1 0, 1 0, 0 0)")).getProblem());
        assertEquals(RING_SELF_INTERSECTION, Validity.of(ring("LINESTRING (0 0, 1 1, 1 0, 0 1, 0 0)")).getProblem());
        assertTrue(Simplicity.isSimple(ring("LINESTRING (0 0, 1 0, 1 1, 0 0)")));
        assertFalse(Simplicity.isSimple(ring("LINESTRING (0 0, 1 1, 1 0, 0 1, 0 0)")));
    }

    @Test
    public void locationOfProblem() {
        Validity v = Validity.of(geometry("POLYGON ((0 0, 10 10, 10 0, 0 10, 0 0))"));
        assertEquals(5, v.getX(), 0);
        assertEquals(5, v.getY(), 0);
        v = Validity.of(geometry("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (5 0, 10 5, 5 5, 5 0))"));
        assertEquals(DISCONNECTED_INTERIOR, v.getProblem());
        assertTrue(v.getX() == 10 && v.getY() == 5 || v.getX() == 5 && v.getY() == 0);
        assertTrue(Double.isNaN(Validity.of(geometry("POINT (1 1)")).getX()));
    }

    // ---------------------------------------------------------------------------------------------------------
    // validity: exactness
    // ---------------------------------------------------------------------------------------------------------

    /**
     * A hole whose vertex lies within an ulp of the shell: on it (touching), just inside, or just outside
     * (crossing)
     */
    @Test
    public void nearlyTouchingHole() {
        double c = 0.1 + 0.2;
        double[] ys = {Math.nextDown(c), c, Math.nextUp(c)};
        for (double y : ys) {
            String wkt = "POLYGON ((0 0, 1 1, 0 1, 0 0), (" + c + " " + y + ", 0.1 0.8, 0.05 0.9, " + c + " " + y + "))";
            int o = org.geolatte.geom.cga.exact.Predicates.orientation(0, 0, 1, 1, c, y);
            Validity.Problem expected = o >= 0 ? NONE : SELF_INTERSECTION;
            assertEquals(wkt, expected, Validity.of(geometry(wkt)).getProblem());
        }
    }

    /**
     * Two holes that touch at a vertex, or miss each other by an ulp
     */
    @Test
    public void nearlyTouchingHoles() {
        double big = 1e14;   // the ulp of 5e14 is 1/16
        for (double dx : new double[]{-0.0625, 0, 0.0625}) {
            double x = 5 * big + dx;
            String wkt = "POLYGON ((0 0, " + 10 * big + " 0, " + 10 * big + " " + 10 * big + ", 0 " + 10 * big
                    + ", 0 0), (" + big + " " + big + ", " + 5 * big + " " + big + ", " + 3 * big + " " + 3 * big
                    + ", " + big + " " + big + "), (" + x + " " + big + ", " + 9 * big + " " + big + ", " + 7 * big
                    + " " + 3 * big + ", " + x + " " + big + "))";
            assertEquals(wkt, dx < 0 ? SELF_INTERSECTION : NONE, Validity.of(geometry(wkt)).getProblem());
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // validity: differential tests against JTS, on small integer grids where JTS is reliable
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void sameValidityAsJts() {
        int n = 6000;
        java.util.Map<Validity.Problem, Integer> problems = new java.util.EnumMap<>(Validity.Problem.class);
        for (int k = 0; k < n; k++) {
            org.locationtech.jts.geom.Geometry g = randomPolygonal();
            boolean expected = new IsValidOp(g).isValid();
            Validity actual = Validity.of(JTS.from(g));
            assertEquals(g + " " + new IsValidOp(g).getValidationError() + " / " + actual, expected, actual.isValid());
            problems.merge(actual.getProblem(), 1, Integer::sum);
        }
        // all kinds of problems occur
        for (Validity.Problem p : Validity.Problem.values()) {
            if (p == INVALID_COORDINATE || p == RING_NOT_CLOSED) continue;
            assertTrue(problems.toString(), problems.getOrDefault(p, 0) >= 10);
        }
    }

    @Test
    public void validityInvariantUnderExactTransformations() {
        for (int k = 0; k < 1000; k++) {
            org.locationtech.jts.geom.Geometry g = randomPolygonal();
            Validity.Problem expected = Validity.of(JTS.from(g)).getProblem();
            for (double scale : new double[]{0.5, 1024, 0x1p-40}) {
                Geometry<?> t = JTS.from(transform(g, 1 << 20, scale, random.nextBoolean()));
                assertEquals(g.toString(), expected == NONE, Validity.isValid(t));
            }
            assertEquals(g.toString(), expected == NONE, Validity.isValid(JTS.from(g.reverse())));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // simplicity
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void simpleGeometries() {
        assertSimple(true, "POINT (1 1)");
        assertSimple(true, "MULTIPOINT ((1 1), (2 2))");
        assertSimple(false, "MULTIPOINT ((1 1), (2 2), (1 1))");
        assertSimple(true, "LINESTRING (0 0, 1 1, 2 0)");
        assertSimple(true, "LINESTRING (0 0, 1 1, 2 0, 0 0)");
        assertSimple(true, "LINESTRING (0 0, 0 0)");
        assertSimple(true, "LINESTRING EMPTY");
        assertSimple(false, "LINESTRING (0 0, 2 2, 2 0, 0 2)");
        assertSimple(false, "LINESTRING (0 0, 2 0, 1 0)");
        assertSimple(false, "LINESTRING (0 0, 2 0, 2 2, 1 0)");
        assertSimple(false, "LINESTRING (0 0, 2 0, 2 2, 0 0, -1 -1)");
        assertSimple(false, "LINESTRING (0 0, 1 0, 0 0)");
        // repeated positions are ignored (JTS 1.19 and 1.20 consider repeated interior positions a self-intersection)
        assertSimple(true, "LINESTRING (2 3, 4 1, 4 1, 3 1)");
        assertSimple(true, "MULTILINESTRING ((0 0, 1 1), (1 1, 2 0))");
        assertSimple(true, "MULTILINESTRING ((0 0, 1 1), (1 1, 2 0), (1 1, 1 2))");
        assertSimple(false, "MULTILINESTRING ((0 0, 2 2), (1 1, 2 0))");
        assertSimple(false, "MULTILINESTRING ((0 0, 2 2), (0 2, 2 0))");
        assertSimple(false, "MULTILINESTRING ((0 0, 1 0, 1 1, 0 0), (0 0, -1 -1))");
        assertSimple(false, "MULTILINESTRING ((0 0, 2 0), (1 0, 3 0))");
        assertSimple(true, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (0 0, 4 2, 2 4, 0 0))");
        assertSimple(false, "POLYGON ((0 0, 10 10, 10 0, 0 10, 0 0))");
        assertSimple(true, "MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), ((1 0, 2 0, 2 1, 1 1, 1 0)))");
        assertSimple(true, "GEOMETRYCOLLECTION (LINESTRING (0 0, 2 2), LINESTRING (0 2, 2 0))");
        assertSimple(false, "GEOMETRYCOLLECTION (LINESTRING (0 0, 2 2, 2 0, 0 2))");
    }

    @Test
    public void nearlySelfTouchingLine() {
        // a line whose end vertex lies on, or an ulp next to, an earlier segment
        double c = 0.1 + 0.2;
        for (double y : new double[]{Math.nextDown(c), c, Math.nextUp(c)}) {
            String wkt = "LINESTRING (0 0, 1 1, 1 0, " + c + " " + y + ")";
            int o = org.geolatte.geom.cga.exact.Predicates.orientation(0, 0, 1, 1, c, y);
            assertSimple(o < 0, wkt);
        }
    }

    @Test
    public void sameSimplicityAsJts() {
        int simple = 0, n = 6000;
        for (int k = 0; k < n; k++) {
            org.locationtech.jts.geom.Geometry g = random.nextInt(3) == 0 ? randomPolygonal() : randomLinear();
            // JTS considers repeated interior positions a self-intersection
            org.locationtech.jts.geom.Geometry distinct;
            try {
                distinct = withoutRepeatedPositions(g);
            } catch (IllegalArgumentException e) {   // a ring that collapses
                continue;
            }
            boolean expected = new IsSimpleOp(distinct).isSimple();
            assertEquals(g.toString(), expected, Simplicity.isSimple(JTS.from(g)));
            if (expected) simple++;
        }
        assertTrue("simple cases: " + simple, simple > n / 10 && simple < n * 9 / 10);
    }

    // ---------------------------------------------------------------------------------------------------------

    private static void assertProblem(Validity.Problem expected, String wkt) {
        Validity v = Validity.of(geometry(wkt));
        assertEquals(wkt + ": " + v, expected, v.getProblem());
        assertEquals(expected == NONE, Validity.isValid(geometry(wkt)));
        assertEquals("JTS: " + wkt, expected == NONE, JTS.to(geometry(wkt)).isValid());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Geometry<?> ring(String wkt) {
        return new org.geolatte.geom.LinearRing((org.geolatte.geom.LineString) geometry(wkt));
    }

    private static void assertSimple(boolean expected, String wkt) {
        assertEquals(wkt, expected, Simplicity.isSimple(geometry(wkt)));
    }

    // polygons and multipolygons with rings on a small grid: random walks, rectangles and small triangles, which
    // often touch, cross and nest
    private org.locationtech.jts.geom.Geometry randomPolygonal() {
        return random.nextBoolean() ? randomRings() : randomHoles();
    }

    // polygons with a rectangular shell and several small holes, which often touch each other and the shell
    private org.locationtech.jts.geom.Geometry randomHoles() {
        int n = 1 + random.nextInt(2);
        org.locationtech.jts.geom.Polygon[] polygons = new org.locationtech.jts.geom.Polygon[n];
        for (int i = 0; i < n; i++) {
            if (i > 0 && random.nextBoolean()) {   // a small polygon, often inside the first one or in one of its holes
                polygons[i] = factory.createPolygon(random.nextBoolean() ? rectangle(0, 0, 8, 8) : triangle(0, 0, 8, 8));
                continue;
            }
            int x0 = random.nextInt(3), y0 = random.nextInt(3), x1 = x0 + 3 + random.nextInt(4), y1 = y0 + 3 + random.nextInt(4);
            LinearRing shell = factory.createLinearRing(new Coordinate[]{new Coordinate(x0, y0), new Coordinate(x1, y0),
                    new Coordinate(x1, y1), new Coordinate(x0, y1), new Coordinate(x0, y0)});
            LinearRing[] holes = new LinearRing[1 + random.nextInt(3)];
            for (int j = 0; j < holes.length; j++) {
                holes[j] = random.nextInt(3) == 0 ? rectangle(x0, y0, x1, y1) : triangle(x0, y0, x1, y1);
            }
            if (holes.length > 1 && random.nextInt(3) == 0) {   // a hole in the first hole
                org.locationtech.jts.geom.Envelope e = holes[0].getEnvelopeInternal();
                double m = 0.25;
                holes[1] = factory.createLinearRing(new Coordinate[]{new Coordinate(e.getMinX() + m, e.getMinY() + m),
                        new Coordinate(e.getMaxX() - m, e.getMinY() + m), new Coordinate(e.getMaxX() - m, e.getMaxY() - m),
                        new Coordinate(e.getMinX() + m, e.getMinY() + m)});
            }
            polygons[i] = factory.createPolygon(shell, holes);
        }
        return n == 1 ? polygons[0] : factory.createMultiPolygon(polygons);
    }

    // a rectangle in the given box
    private LinearRing rectangle(int x0, int y0, int x1, int y1) {
        int w = 1 + random.nextInt(3), h = 1 + random.nextInt(3);
        int x = x0 + random.nextInt(Math.max(1, x1 - x0 - w + 1)), y = y0 + random.nextInt(Math.max(1, y1 - y0 - h + 1));
        return factory.createLinearRing(new Coordinate[]{new Coordinate(x, y), new Coordinate(x + w, y),
                new Coordinate(x + w, y + h), new Coordinate(x, y + h), new Coordinate(x, y)});
    }

    // a small triangle with its vertices in the given box, on a grid of 1 or 1/2
    private LinearRing triangle(int x0, int y0, int x1, int y1) {
        double d = random.nextBoolean() ? 1 : 0.5;
        while (true) {
            double x = x0 + d * random.nextInt((int) ((x1 - x0) / d) + 1), y = y0 + d * random.nextInt((int) ((y1 - y0) / d) + 1);
            Coordinate[] c = {new Coordinate(x, y), new Coordinate(x + d * (random.nextInt(3) - 1), y + d * (random.nextInt(3) - 1)),
                    new Coordinate(x + d * (random.nextInt(3) - 1), y + d * (random.nextInt(3) - 1)), new Coordinate(x, y)};
            boolean inside = true;
            for (Coordinate p : c) inside &= p.x >= x0 && p.x <= x1 && p.y >= y0 && p.y <= y1;
            if (inside && org.locationtech.jts.algorithm.Orientation.index(c[0], c[1], c[2]) != 0) {
                return factory.createLinearRing(c);
            }
        }
    }

    // polygons with random rings, which often cross
    private org.locationtech.jts.geom.Geometry randomRings() {
        int n = 1 + random.nextInt(3);
        org.locationtech.jts.geom.Polygon[] polygons = new org.locationtech.jts.geom.Polygon[n];
        for (int i = 0; i < n; i++) {
            LinearRing shell = randomRing(8, true);
            int holes = random.nextInt(3);
            LinearRing[] h = new LinearRing[holes];
            for (int j = 0; j < holes; j++) h[j] = randomRing(8, false);
            polygons[i] = factory.createPolygon(shell, h);
        }
        return n == 1 ? polygons[0] : factory.createMultiPolygon(polygons);
    }

    private LinearRing randomRing(int size, boolean big) {
        switch (random.nextInt(big ? 3 : 4)) {
            case 0: {   // a rectangle
                int x0 = random.nextInt(size), y0 = random.nextInt(size);
                int x1 = x0 + 1 + random.nextInt(big ? size : 3), y1 = y0 + 1 + random.nextInt(big ? size : 3);
                return factory.createLinearRing(new Coordinate[]{new Coordinate(x0, y0), new Coordinate(x1, y0),
                        new Coordinate(x1, y1), new Coordinate(x0, y1), new Coordinate(x0, y0)});
            }
            case 1:
            case 3: {   // a small triangle
                int x = random.nextInt(size + 1), y = random.nextInt(size + 1);
                Coordinate a = new Coordinate(x, y);
                Coordinate b = new Coordinate(x + 1 + random.nextInt(2), y + random.nextInt(3) - 1);
                Coordinate c = new Coordinate(x + random.nextInt(3) - 1, y + 1 + random.nextInt(2));
                return factory.createLinearRing(new Coordinate[]{a, b, c, new Coordinate(a)});
            }
            default: {  // a random walk
                int m = 3 + random.nextInt(4);
                Coordinate[] c = new Coordinate[m + 1];
                for (int i = 0; i < m; i++) c[i] = new Coordinate(random.nextInt(size + 1), random.nextInt(size + 1));
                c[m] = new Coordinate(c[0]);
                return factory.createLinearRing(c);
            }
        }
    }

    private org.locationtech.jts.geom.Geometry randomLinear() {
        int n = 1 + random.nextInt(3);
        org.locationtech.jts.geom.LineString[] lines = new org.locationtech.jts.geom.LineString[n];
        for (int i = 0; i < n; i++) {
            int m = 2 + random.nextInt(4);
            List<Coordinate> c = new ArrayList<>();
            // no repeated consecutive positions, which JTS considers a self-intersection
            while (c.size() < m) {
                Coordinate p = new Coordinate(random.nextInt(5), random.nextInt(5));
                if (c.isEmpty() || !p.equals2D(c.get(c.size() - 1))) c.add(p);
            }
            if (random.nextInt(4) == 0 && !c.get(0).equals2D(c.get(c.size() - 1))) c.add(new Coordinate(c.get(0)));
            lines[i] = factory.createLineString(c.toArray(new Coordinate[0]));
        }
        if (random.nextInt(5) == 0) {
            Coordinate[] points = new Coordinate[1 + random.nextInt(4)];
            for (int i = 0; i < points.length; i++) points[i] = new Coordinate(random.nextInt(3), random.nextInt(3));
            return factory.createMultiPointFromCoords(points);
        }
        return n == 1 ? lines[0] : factory.createMultiLineString(lines);
    }

    private static org.locationtech.jts.geom.Geometry withoutRepeatedPositions(org.locationtech.jts.geom.Geometry g) {
        return new org.locationtech.jts.geom.util.GeometryTransformer() {
            @Override
            protected org.locationtech.jts.geom.CoordinateSequence transformCoordinates(
                    org.locationtech.jts.geom.CoordinateSequence coordinates, org.locationtech.jts.geom.Geometry parent) {
                return factory.getCoordinateSequenceFactory().create(
                        org.locationtech.jts.geom.CoordinateArrays.removeRepeatedPoints(coordinates.toCoordinateArray()));
            }
        }.transform(g);
    }

    // translates and scales (exactly), and optionally swaps X and Y
    private static org.locationtech.jts.geom.Geometry transform(org.locationtech.jts.geom.Geometry g, double offset,
                                                                double scale, boolean swap) {
        org.locationtech.jts.geom.Geometry copy = g.copy();
        copy.apply(new org.locationtech.jts.geom.CoordinateFilter() {
            @Override
            public void filter(Coordinate c) {
                double x = (c.x + offset) * scale, y = (c.y + offset) * scale;
                c.x = swap ? y : x;
                c.y = swap ? x : y;
            }
        });
        copy.geometryChanged();
        return copy;
    }
}
