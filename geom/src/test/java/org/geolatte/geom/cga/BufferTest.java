package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.geolatte.geom.cga.BufferParameters.EndCap;
import static org.geolatte.geom.cga.BufferParameters.Join;
import static org.junit.Assert.*;

public class BufferTest {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    private static final BufferParameters ROUND = BufferParameters.DEFAULT;
    private static final BufferParameters MITRE = ROUND.withJoin(Join.MITRE);
    private static final BufferParameters BEVEL = ROUND.withJoin(Join.BEVEL);
    private static final BufferParameters FLAT = ROUND.withEndCap(EndCap.FLAT);
    private static final BufferParameters SQUARE = ROUND.withEndCap(EndCap.SQUARE);

    // the area of the regular 32-gon with circumradius 1, the default approximation of the unit circle
    private static final double CIRCLE = 16 * Math.sin(Math.PI / 16);

    private static final String SQUARE_10 = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))";

    static Geometry<C2D> g(String wkt) {
        return Wkt.fromWkt(wkt, crs);
    }

    private static Geometry<C2D> buffer(String wkt, double d, BufferParameters parameters) {
        Geometry<C2D> result = Buffer.buffer(g(wkt), d, parameters);
        assertTrue(result + " is not valid: " + Validity.of(result), Validity.isValid(result));
        return result;
    }

    private static void checkArea(String wkt, double d, BufferParameters parameters, double expected) {
        Geometry<C2D> result = buffer(wkt, d, parameters);
        assertEquals("buffer of " + wkt + " by " + d + " with " + parameters + ": " + result,
                expected, Area.of(result), 1e-9 * Math.max(1, expected));
    }

    private static void checkEmpty(String wkt, double d, BufferParameters parameters) {
        Geometry<C2D> result = buffer(wkt, d, parameters);
        assertTrue(result.toString(), result.isEmpty());
        assertEquals(GeometryType.POLYGON, result.getGeometryType());
    }

    @Test
    public void point() {
        Geometry<C2D> disk = buffer("POINT (3 4)", 1, ROUND);
        assertEquals(GeometryType.POLYGON, disk.getGeometryType());
        assertEquals(33, disk.getNumPositions());
        assertEquals(CIRCLE, Area.of(disk), 1e-12);
        assertEquals(17, buffer("POINT (3 4)", 1, ROUND.withQuadrantSegments(4)).getNumPositions());
        checkArea("POINT (3 4)", 1, SQUARE, 4);
        checkEmpty("POINT (3 4)", 1, FLAT);
        checkEmpty("POINT (3 4)", 0, ROUND);
        checkEmpty("POINT (3 4)", -1, ROUND);
        Geometry<C2D> two = buffer("MULTIPOINT ((0 0), (10 0))", 1, ROUND);
        assertEquals(GeometryType.MULTIPOLYGON, two.getGeometryType());
        checkArea("MULTIPOINT ((0 0), (1 0))", 1, SQUARE, 6);
    }

    @Test
    public void squareWithEachJoin() {
        checkArea(SQUARE_10, 1, MITRE, 144);
        assertEquals(5, buffer(SQUARE_10, 1, MITRE).getNumPositions());
        checkArea(SQUARE_10, 1, BEVEL, 142);
        checkArea(SQUARE_10, 1, ROUND, 140 + CIRCLE);
        // a mitre beyond the limit (sqrt 2 for a right angle) is cut off at the limit
        double limit = 1.2, cut = Math.sqrt(2) - limit;
        checkArea(SQUARE_10, 1, MITRE.withMitreLimit(limit), 144 - 4 * cut * cut);
        // the bevel lies beyond a limit of 0.5
        checkArea(SQUARE_10, 1, MITRE.withMitreLimit(0.5), 142);
    }

    @Test
    public void erosion() {
        for (BufferParameters p : new BufferParameters[]{ROUND, MITRE, BEVEL}) {
            checkArea(SQUARE_10, -1, p, 64);
            checkArea(SQUARE_10, -4.9, p, 0.2 * 0.2);
            checkEmpty(SQUARE_10, -5, p);
            checkEmpty(SQUARE_10, -6, p);
        }
        // an L shape: the reflex corner is rounded inside
        String l = "POLYGON ((0 0, 4 0, 4 2, 2 2, 2 4, 0 4, 0 0))";
        checkArea(l, -0.5, MITRE, 5);
        checkArea(l, -0.5, BEVEL, 5 + 0.125);
        checkArea(l, -0.5, ROUND, 5 + 0.25 - 0.25 * CIRCLE / 4);
        // lines have no interior
        checkEmpty("LINESTRING (0 0, 10 0)", -1, ROUND);
    }

    @Test
    public void lineWithEachCap() {
        String segment = "LINESTRING (0 0, 10 0)";
        checkArea(segment, 1, FLAT, 20);
        checkArea(segment, 1, SQUARE, 24);
        checkArea(segment, 1, ROUND, 20 + CIRCLE);
        assertEquals(5, buffer(segment, 1, FLAT).getNumPositions());
        // a right angle
        String corner = "LINESTRING (0 0, 10 0, 10 10)";
        checkArea(corner, 1, FLAT.withJoin(Join.MITRE), 40);
        checkArea(corner, 1, ROUND, 39 + 1.25 * CIRCLE);
        // a reversal in the line is joined
        checkArea("LINESTRING (0 0, 10 0, 5 0)", 1, FLAT, 20 + CIRCLE / 2);
        checkArea("LINESTRING (0 0, 10 0, 5 0)", 1, FLAT.withJoin(Join.MITRE).withMitreLimit(2), 24);
    }

    @Test
    public void closedLineHasNoCaps() {
        for (BufferParameters p : new BufferParameters[]{FLAT.withJoin(Join.MITRE), SQUARE.withJoin(Join.MITRE)}) {
            Geometry<C2D> result = buffer("LINESTRING (0 0, 10 0, 10 10, 0 10, 0 0)", 1, p);
            assertEquals(12 * 12 - 8 * 8, Area.of(result), 1e-9);
            assertEquals(1, ((Polygon<C2D>) result).getNumInteriorRing());
        }
    }

    @Test
    public void holes() {
        String withHole = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (4 4, 6 4, 6 6, 4 6, 4 4))";
        checkArea(withHole, 0.5, MITRE, 121 - 1);
        Geometry<C2D> closed = buffer(withHole, 1.5, MITRE);
        assertEquals(0, ((Polygon<C2D>) closed).getNumInteriorRing());
        assertEquals(169, Area.of(closed), 1e-9);
        // erosion widens the hole
        checkArea(withHole, -1, MITRE, 64 - 16);
        checkArea(withHole, 0, MITRE, 96);
    }

    @Test
    public void sidesMerge() {
        Geometry<C2D> u = buffer("LINESTRING (0 0, 0 10, 1 10, 1 0)", 1, ROUND);
        assertEquals(GeometryType.POLYGON, u.getGeometryType());
        assertEquals(0, ((Polygon<C2D>) u).getNumInteriorRing());
        Geometry<C2D> eight = buffer("LINESTRING (0 0, 10 10, 10 0, 0 10, 0 0)", 0.5, ROUND);
        assertEquals(GeometryType.POLYGON, eight.getGeometryType());
        assertEquals(2, ((Polygon<C2D>) eight).getNumInteriorRing());
    }

    @Test
    public void overlappingCollection() {
        String collection = "GEOMETRYCOLLECTION (POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0)), "
                + "POLYGON ((2 0, 6 0, 6 4, 2 4, 2 0)), LINESTRING (6 2, 8 2))";
        checkArea(collection, -1, MITRE, 8);
        checkArea(collection, 0, MITRE, 24);
        Geometry<C2D> grown = buffer(collection, 1, MITRE.withEndCap(EndCap.FLAT));
        assertEquals(48 + 2, Area.of(grown), 1e-9);
    }

    @Test
    public void zeroDistanceUnitesThePolygons() {
        assertTrue(Relate.equalsTopologically(g(SQUARE_10), buffer(SQUARE_10, 0, ROUND)));
        checkEmpty("LINESTRING (0 0, 10 0)", 0, ROUND);
    }

    @Test
    public void emptyAndInvalidInput() {
        checkEmpty("POLYGON EMPTY", 1, ROUND);
        checkEmpty("GEOMETRYCOLLECTION EMPTY", 1, ROUND);
        assertThrows(IllegalArgumentException.class, () -> Buffer.buffer(g(SQUARE_10), Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Buffer.buffer(g(SQUARE_10), Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> ROUND.withQuadrantSegments(0));
        assertThrows(IllegalArgumentException.class, () -> ROUND.withMitreLimit(0));
        assertThrows(IllegalArgumentException.class, () -> ROUND.withMitreLimit(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> ROUND.withSimplifyFactor(-0.01));
        assertThrows(IllegalArgumentException.class,
                () -> ROUND.withSimplifyFactor(Math.nextUp(BufferParameters.MAX_SIMPLIFY_FACTOR)));
        assertThrows(IllegalArgumentException.class, () -> ROUND.withSimplifyFactor(Double.NaN));
    }

    @Test
    public void simplifiedInputStaysInsideTheExactBuffer() {
        // a dense, slightly wavy circle of radius 100: with d = 50 most vertices fall within 1 % of d of a chord
        int n = 4000;
        StringBuilder wkt = new StringBuilder("POLYGON ((");
        for (int i = 0; i <= n; i++) {
            double a = 2 * Math.PI * (i % n) / n, r = 100 + 0.1 * Math.sin(40 * a);
            wkt.append(i == 0 ? "" : ", ").append(r * Math.cos(a)).append(' ').append(r * Math.sin(a));
        }
        String ring = wkt.append("))").toString();
        for (double d : new double[]{50, -20}) {
            Geometry<C2D> simplified = buffer(ring, d, ROUND);
            Geometry<C2D> exact = buffer(ring, d, ROUND.withSimplifyFactor(0));
            assertTrue(simplified.getNumPositions() < exact.getNumPositions() / 4);
            // the buffer (for d < 0 the band that is eroded) stays inside the exact one, so it extends beyond the
            // unsimplified result by at most the sagitta of that result's arc chords (angle below 3 pi / 8q)
            Geometry<C2D> inner = d > 0 ? simplified : exact, outer = d > 0 ? exact : simplified;
            double h = 0.01 * Math.abs(d), sagitta = Math.abs(d) * (1 - Math.cos(3 * Math.PI / 64));
            double perimeter = Length.of(exact);
            assertTrue(Area.of(Overlay.difference(inner, outer)) <= perimeter * sagitta);
            // and it misses at most a band of width 2h + the sagitta
            assertTrue(Area.of(Overlay.difference(outer, inner)) <= perimeter * (2 * h + sagitta));
        }
    }

    @Test
    public void coordinateReferenceSystemAndMeasures() {
        CoordinateReferenceSystem<C3D> crs3d = CoordinateReferenceSystems.PROJECTED_3D_METER;
        Geometry<C3D> line = Wkt.fromWkt("LINESTRING Z (0 0 5, 10 0 6)", crs3d);
        Geometry<C3D> result = Buffer.buffer(line, 1, FLAT);
        assertEquals(crs3d, result.getCoordinateReferenceSystem());
        assertEquals(20, Area.of(result), 1e-9);
        assertTrue(Double.isNaN(result.getPositionN(0).getZ()));
    }

    @Test
    public void fixedGrid() {
        Grid grid = Grid.fixed("0.01");
        Geometry<C2D> result = Buffer.buffer(g("LINESTRING (0 0, 3.333 1.111, 5 0)"), 0.777, ROUND, grid);
        assertTrue(Validity.isValid(result));
        PositionSequence<C2D> positions = ((Polygon<C2D>) result).getExteriorRing().getPositions();
        for (C2D p : positions) {
            assertEquals(grid.round(p.getX()), p.getX(), 0);
            assertEquals(grid.round(p.getY()), p.getY(), 0);
        }
        // a buffer thinner than a cell collapses
        checkEmptyOnGrid("LINESTRING (0 0, 10 0)", 0.001, grid);
        assertThrows(IllegalArgumentException.class,
                () -> Buffer.buffer(g("POINT (1e15 0)"), 1, ROUND, Grid.fixed("0.01")));
    }

    private static void checkEmptyOnGrid(String wkt, double d, Grid grid) {
        Geometry<C2D> result = Buffer.buffer(g(wkt), d, FLAT, grid);
        assertTrue(result.toString(), result.isEmpty());
    }
}
