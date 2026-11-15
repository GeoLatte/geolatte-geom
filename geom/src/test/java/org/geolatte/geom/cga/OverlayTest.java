package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.geolatte.geom.cga.Overlay.Operation.*;
import static org.junit.Assert.*;

public class OverlayTest {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    static Geometry<C2D> g(String wkt) {
        return Wkt.fromWkt(wkt, crs);
    }

    private static void check(String a, String b, Overlay.Operation op, String expected) {
        check(g(a), g(b), op, g(expected));
    }

    static void check(Geometry<C2D> a, Geometry<C2D> b, Overlay.Operation op, Geometry<C2D> expected) {
        Geometry<C2D> actual = Overlay.overlay(a, b, op);
        String message = op + " of " + a + " and " + b + ": " + actual;
        assertTrue(message + " is not valid", Validity.isValid(actual));
        if (expected.isEmpty()) {
            assertTrue(message, actual.isEmpty());
        } else {
            assertTrue(message + " expected " + expected, Relate.equalsTopologically(expected, actual));
        }
        assertEquals(message, expected.getGeometryType(), actual.getGeometryType());
    }

    private static final String A = "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))";
    private static final String B = "POLYGON ((1 1, 3 1, 3 3, 1 3, 1 1))";

    @Test
    public void overlappingSquares() {
        check(A, B, INTERSECTION, "POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))");
        check(A, B, UNION, "POLYGON ((0 0, 2 0, 2 1, 3 1, 3 3, 1 3, 1 2, 0 2, 0 0))");
        check(A, B, DIFFERENCE, "POLYGON ((0 0, 2 0, 2 1, 1 1, 1 2, 0 2, 0 0))");
        check(B, A, DIFFERENCE, "POLYGON ((2 1, 3 1, 3 3, 1 3, 1 2, 2 2, 2 1))");
        check(A, B, SYM_DIFFERENCE, "MULTIPOLYGON (((0 0, 2 0, 2 1, 1 1, 1 2, 0 2, 0 0)), "
                + "((2 1, 3 1, 3 3, 1 3, 1 2, 2 2, 2 1)))");
    }

    @Test
    public void squaresSharingASideOrACorner() {
        String right = "POLYGON ((2 0, 4 0, 4 2, 2 2, 2 0))";
        check(A, right, INTERSECTION, "LINESTRING (2 0, 2 2)");
        check(A, right, UNION, "POLYGON ((0 0, 4 0, 4 2, 0 2, 0 0))");
        check(A, right, DIFFERENCE, A);
        check(A, right, SYM_DIFFERENCE, "POLYGON ((0 0, 4 0, 4 2, 0 2, 0 0))");
        String corner = "POLYGON ((2 2, 4 2, 4 4, 2 4, 2 2))";
        check(A, corner, INTERSECTION, "POINT (2 2)");
        check(A, corner, UNION, "MULTIPOLYGON (((0 0, 2 0, 2 2, 0 2, 0 0)), ((2 2, 4 2, 4 4, 2 4, 2 2)))");
        // a part of a side
        check(A, "POLYGON ((2 1, 3 1, 3 5, 2 5, 2 1))", INTERSECTION, "LINESTRING (2 1, 2 2)");
    }

    @Test
    public void holes() {
        String withHole = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 2 8, 8 8, 8 2, 2 2))";
        check(withHole, "POLYGON ((1 1, 9 1, 9 9, 1 9, 1 1))", UNION, "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))");
        check(withHole, "POLYGON ((3 3, 7 3, 7 7, 3 7, 3 3))", UNION,
                "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 2 8, 8 8, 8 2, 2 2)), ((3 3, 7 3, 7 7, 3 7, 3 3)))");
        check(withHole, "POLYGON ((3 3, 7 3, 7 7, 3 7, 3 3))", INTERSECTION, "POLYGON EMPTY");
        // a new hole, touching the shell
        check("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))", "POLYGON ((0 5, 5 3, 5 7, 0 5))", DIFFERENCE,
                "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 5, 0 0), (0 5, 5 7, 5 3, 0 5))");
        // an island in the hole of an island in a hole
        String islands = "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0), (1 1, 1 9, 9 9, 9 1, 1 1)), "
                + "((2 2, 8 2, 8 8, 2 8, 2 2), (3 3, 3 7, 7 7, 7 3, 3 3)))";
        check(islands, "POLYGON ((4 4, 6 4, 6 6, 4 6, 4 4))", UNION, "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0), "
                + "(1 1, 1 9, 9 9, 9 1, 1 1)), ((2 2, 8 2, 8 8, 2 8, 2 2), (3 3, 3 7, 7 7, 7 3, 3 3)), "
                + "((4 4, 6 4, 6 6, 4 6, 4 4)))");
    }

    @Test
    public void linesAndPolygons() {
        String line = "LINESTRING (-1 1, 3 1)";
        check(line, A, INTERSECTION, "LINESTRING (0 1, 2 1)");
        check(line, A, DIFFERENCE, "MULTILINESTRING ((-1 1, 0 1), (2 1, 3 1))");
        check(A, line, DIFFERENCE, A);
        check(line, A, UNION, "GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), LINESTRING (-1 1, 0 1), "
                + "LINESTRING (2 1, 3 1))");
        check(line, A, SYM_DIFFERENCE, "GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), "
                + "LINESTRING (-1 1, 0 1), LINESTRING (2 1, 3 1))");
        // along the boundary
        check("LINESTRING (0 0, 4 0)", A, INTERSECTION, "LINESTRING (0 0, 2 0)");
        check("LINESTRING (0 0, 4 0)", A, DIFFERENCE, "LINESTRING (2 0, 4 0)");
        // touching at a point
        check("LINESTRING (2 2, 4 4)", A, INTERSECTION, "POINT (2 2)");
        check("LINESTRING (2 2, 4 4)", A, DIFFERENCE, "LINESTRING (2 2, 4 4)");
    }

    @Test
    public void lines() {
        check("LINESTRING (0 0, 2 2)", "LINESTRING (0 2, 2 0)", INTERSECTION, "POINT (1 1)");
        check("LINESTRING (0 0, 2 2)", "LINESTRING (0 2, 2 0)", UNION,
                "MULTILINESTRING ((0 0, 1 1), (1 1, 2 2), (0 2, 1 1), (1 1, 2 0))");
        // minus a point: the closure is the whole line, merged through the node
        check("LINESTRING (0 0, 2 2)", "LINESTRING (0 2, 2 0)", DIFFERENCE, "LINESTRING (0 0, 1 1, 2 2)");
        check("LINESTRING (0 0, 4 0)", "LINESTRING (2 0, 6 0)", INTERSECTION, "LINESTRING (2 0, 4 0)");
        check("LINESTRING (0 0, 4 0)", "LINESTRING (2 0, 6 0)", SYM_DIFFERENCE, "MULTILINESTRING ((0 0, 2 0), (4 0, 6 0))");
        check("LINESTRING (0 0, 4 0)", "LINESTRING (4 0, 4 4)", INTERSECTION, "POINT (4 0)");
        check("LINESTRING (0 0, 4 0)", "LINESTRING (5 0, 6 0)", INTERSECTION, "LINESTRING EMPTY");
    }

    @Test
    public void points() {
        String points = "MULTIPOINT ((1 1), (5 5), (2 0))";
        check(points, A, INTERSECTION, "MULTIPOINT ((1 1), (2 0))");
        check(points, A, DIFFERENCE, "POINT (5 5)");
        check(points, A, UNION, "GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), POINT (5 5))");
        check(points, "LINESTRING (0 0, 6 6)", INTERSECTION, "MULTIPOINT ((1 1), (5 5))");
        check(points, "MULTIPOINT ((1 1), (7 7))", SYM_DIFFERENCE, "MULTIPOINT ((5 5), (2 0), (7 7))");
        check(points, "MULTIPOINT ((1 1), (7 7))", UNION, "MULTIPOINT ((1 1), (5 5), (2 0), (7 7))");
        check(points, "MULTIPOINT ((8 8), (7 7))", INTERSECTION, "POINT EMPTY");
    }

    @Test
    public void unaryUnion() {
        Geometry<C2D> squares = g("GEOMETRYCOLLECTION (POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0)), "
                + "POLYGON ((1 0, 2 0, 2 1, 1 1, 1 0)), POLYGON ((0 1, 1 1, 1 2, 0 2, 0 1)), "
                + "POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1)), LINESTRING (0.5 0.5, 3 3), POINT (1 1), POINT (5 5))");
        Geometry<C2D> u = Overlay.unaryUnion(squares);
        assertTrue(Validity.isValid(u));
        assertTrue(u.toString(), Relate.equalsTopologically(g("GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), "
                + "LINESTRING (2 2, 3 3), POINT (5 5))"), u));
        // overlapping polygons
        Geometry<C2D> overlapping = g("MULTIPOLYGON (((0 0, 2 0, 2 2, 0 2, 0 0)), ((1 1, 3 1, 3 3, 1 3, 1 1)))");
        assertTrue(Relate.equalsTopologically(g("POLYGON ((0 0, 2 0, 2 1, 3 1, 3 3, 1 3, 1 2, 0 2, 0 0))"),
                Overlay.unaryUnion(overlapping)));
        assertEquals(1, ((Polygon<?>) Overlay.unaryUnion(overlapping)).getExteriorRing().getNumPositions() > 0 ? 1 : 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void zAndMAreKeptAndInterpolated() {
        CoordinateReferenceSystem<C3DM> crs3dm = CoordinateReferenceSystems.PROJECTED_3DM_METER;
        // a new vertex: interpolated along the segment of the first operand
        Geometry<C3DM> a = Wkt.fromWkt("LINESTRING ZM (0 0 10 100, 4 0 14 104)", crs3dm);
        Geometry<C3DM> b = Wkt.fromWkt("LINESTRING ZM (2 -2 0 0, 2 2 4 4)", crs3dm);
        Point<C3DM> crossing = (Point<C3DM>) Overlay.intersection(a, b);
        assertEquals(new C3DM(2, 0, 12, 102), crossing.getPosition());
        assertEquals(crs3dm, crossing.getCoordinateReferenceSystem());
        assertEquals(new C3DM(2, 0, 2, 2), ((Point<C3DM>) Overlay.intersection(b, a)).getPosition());
        // vertices keep their values, those of the first operand where both have one
        Geometry<C3DM> p = Wkt.fromWkt("POLYGON ZM ((0 0 1 10, 4 0 1 10, 4 4 1 10, 0 4 1 10, 0 0 1 10))", crs3dm);
        Geometry<C3DM> q = Wkt.fromWkt("POLYGON ZM ((2 2 2 20, 6 2 2 20, 6 6 2 20, 2 6 2 20, 2 2 2 20), "
                + "(3 3 3 30, 3 5 3 30, 5 5 3 30, 5 3 3 30, 3 3 3 30))", crs3dm);
        Polygon<C3DM> inter = (Polygon<C3DM>) Overlay.intersection(p, q);
        // an L shape: (2 2) and (3 3) are vertices of q, the others are crossings on edges of p
        assertEquals(7, inter.getExteriorRing().getNumPositions());
        for (C3DM c : inter.getPositions()) {
            double expectedZ = c.getX() == 2 && c.getY() == 2 ? 2 : c.getX() == 3 && c.getY() == 3 ? 3 : 1;
            assertEquals(c.toString(), expectedZ, c.getZ(), 0);
            assertEquals(c.toString(), 10 * expectedZ, c.getM(), 0);
        }
        // a mixed result in a 2D crs: Z and M are dropped
        Geometry<C2D> flat = Overlay.intersection(g(A), g("LINESTRING (1 1, 5 1)"));
        assertEquals(crs, flat.getCoordinateReferenceSystem());
    }

    @Test
    public void emptyOperands() {
        check(A, "POLYGON EMPTY", INTERSECTION, "POLYGON EMPTY");
        check(A, "POLYGON EMPTY", UNION, A);
        check(A, "LINESTRING EMPTY", DIFFERENCE, A);
        check("POLYGON EMPTY", A, DIFFERENCE, "POLYGON EMPTY");
        check("LINESTRING EMPTY", A, INTERSECTION, "LINESTRING EMPTY");
        check("POINT EMPTY", "LINESTRING EMPTY", UNION, "LINESTRING EMPTY");
    }

    // ---------------------------------------------------------------------------------------------------------
    // fixed grids
    // ---------------------------------------------------------------------------------------------------------

    private static final Grid MM = Grid.fixed("0.001");
    private static final String UNIT = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";

    private static void check(String a, String b, Overlay.Operation op, Grid grid, String expected) {
        Geometry<C2D> actual = Overlay.overlay(g(a), g(b), op, grid);
        String message = op + " of " + a + " and " + b + " on " + grid + ": " + actual;
        assertTrue(message + " is not valid", Validity.isValid(actual));
        assertOnGrid(actual, grid);
        Geometry<C2D> e = g(expected);
        if (e.isEmpty()) {
            assertTrue(message, actual.isEmpty());
        } else {
            assertTrue(message + " expected " + e, Relate.equalsTopologically(e, actual));
        }
        assertEquals(message, e.getGeometryType(), actual.getGeometryType());
    }

    static void assertOnGrid(Geometry<?> geometry, Grid grid) {
        for (Position p : geometry.getPositions()) {
            assertEquals(p.toString(), grid.round(p.getCoordinate(0)), p.getCoordinate(0), 0);
            assertEquals(p.toString(), grid.round(p.getCoordinate(1)), p.getCoordinate(1), 0);
        }
    }

    @Test
    public void snappingToAFixedGridCanMakeOperandsMeet() {
        String right = "POLYGON ((1.0004 0, 2 0, 2 1, 1.0004 1, 1.0004 0))";
        check(UNIT, right, INTERSECTION, MM, "LINESTRING (1 0, 1 1)");
        check(UNIT, right, UNION, MM, "POLYGON ((0 0, 2 0, 2 1, 0 1, 0 0))");
        // on the double grid they stay apart
        check(UNIT, right, INTERSECTION, "POLYGON EMPTY");
        check(UNIT, right, INTERSECTION, Grid.DOUBLE, "POLYGON EMPTY");
    }

    @Test
    public void verticesAndCrossingsMoveToGridPoints() {
        // the crossing (0.00155, 0.00155) rounds to (0.002, 0.002)
        check("LINESTRING (0 0, 0.0031 0.0031)", "LINESTRING (0 0.0031, 0.0031 0)", INTERSECTION, MM,
                "POINT (0.002 0.002)");
        check("POLYGON ((0.0004 0.0004, 1.0006 0.0004, 1.0006 1.0004, 0.0004 1.0004, 0.0004 0.0004))",
                "POLYGON EMPTY", UNION, MM, "POLYGON ((0 0, 1.001 0, 1.001 1, 0 1, 0 0))");
        // halves round up
        check("LINESTRING (0.5 0.5, 3.5 0.5)", "LINESTRING EMPTY", UNION, Grid.fixed(1), "LINESTRING (1 1, 4 1)");
        check("LINESTRING (-0.5 -0.5, -3.5 -0.5)", "LINESTRING EMPTY", UNION, Grid.fixed(1),
                "LINESTRING (0 0, -3 0)");
    }

    @Test
    public void partsThinnerThanACellCollapse() {
        // a sliver of width 0.0003
        check(UNIT, "POLYGON ((0.0003 0, 1 0, 1 1, 0.0003 1, 0.0003 0))", DIFFERENCE, MM, "POLYGON EMPTY");
        // a polygon that collapses to a line, a line that collapses to a point
        check("POLYGON ((0 0, 10 0, 10 0.4, 0 0.4, 0 0))", "POLYGON EMPTY", UNION, Grid.fixed(1), "POLYGON EMPTY");
        check("LINESTRING (0.0001 0.0001, 0.0003 0.0002)", "LINESTRING EMPTY", UNION, MM, "LINESTRING EMPTY");
        // a polygon of which a part collapses
        check("POLYGON ((0 0, 2 0, 2 1, 1 1, 1 1.3, 0 1.3, 0 0))", "POLYGON EMPTY", UNION, Grid.fixed(1),
                "POLYGON ((0 0, 2 0, 2 1, 0 1, 0 0))");
    }

    @Test
    public void pointsOnAFixedGrid() {
        // two points inside the square round to the same grid point
        check("MULTIPOINT ((0.0001 0.0001), (0.0002 0.0002))", UNIT, INTERSECTION, MM, "POINT (0 0)");
        // a point outside the polygon rounds onto its boundary: absorbed by the union
        String inner = "POLYGON ((0.001 0, 1 0, 1 1, 0.001 1, 0.001 0))";
        check("POINT (0.0006 0.5)", inner, UNION, MM, inner);
        // the point is not in the polygon, so it is in the difference (rounded)
        check("POINT (0.0006 0.5)", inner, DIFFERENCE, MM, "POINT (0.001 0.5)");
        check("POINT (0.0006 0.5)", inner, INTERSECTION, MM, "POINT EMPTY");
        // a point rounding onto a line of the result
        check("GEOMETRYCOLLECTION (POINT (0.5 0.0004), LINESTRING (0 0, 1 0))", "POINT EMPTY", UNION, MM,
                "LINESTRING (0 0, 1 0)");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void measuresOnAFixedGrid() {
        CoordinateReferenceSystem<C3DM> crs3dm = CoordinateReferenceSystems.PROJECTED_3DM_METER;
        Geometry<C3DM> line = Wkt.fromWkt("LINESTRING ZM (0.0004 0 1 10, 1.0004 0 2 20)", crs3dm);
        LineString<C3DM> reduced = (LineString<C3DM>) Overlay.reducePrecision(line, MM);
        assertEquals(new C3DM(0, 0, 1, 10), reduced.getStartPosition());
        assertEquals(new C3DM(1, 0, 2, 20), reduced.getEndPosition());
        assertEquals(crs3dm, reduced.getCoordinateReferenceSystem());
    }

    @Test
    public void fixedGridRangeAndEmptyOperands() {
        assertThrows(IllegalArgumentException.class, () -> Overlay.intersection(g("POINT (1e13 0)"), g(A), MM));
        Overlay.intersection(g("POINT (1e9 0)"), g(A), MM);
        check(A, "POLYGON EMPTY", INTERSECTION, MM, "POLYGON EMPTY");
        check(A, "LINESTRING EMPTY", UNION, MM, A);
        assertTrue(Overlay.reducePrecision(g("POLYGON EMPTY"), MM).isEmpty());
    }
}
