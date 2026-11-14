package org.geolatte.geom;

import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests {@link DefaultProjectedGeometryOperations}, and the predicates added to {@link JTSGeometryOperations}.
 */
public class TestDefaultProjectedGeometryOperations {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;
    private static final CoordinateReferenceSystem<C2D> l72 = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);

    private final ProjectedGeometryOperations ops = new DefaultProjectedGeometryOperations();
    private final ProjectedGeometryOperations jts = new JTSGeometryOperations();

    private static final String SQUARE = "POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0))";

    @Test
    public void isTheDefault() {
        assertTrue(ProjectedGeometryOperations.Default instanceof DefaultProjectedGeometryOperations);
        assertSame(ProjectedGeometryOperations.Default, GeometryOperations.projectedGeometryOperations());
    }

    /**
     * Both implementations agree on simple cases.
     */
    @Test
    public void predicates() {
        for (ProjectedGeometryOperations o : new ProjectedGeometryOperations[]{ops, jts}) {
            Geometry<C2D> square = g(SQUARE), inner = g("POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))");
            Geometry<C2D> touching = g("POLYGON ((0 1, 1 1, 1 2, 0 2, 0 1))");
            Geometry<C2D> overlapping = g("POLYGON ((2 2, 6 2, 6 6, 2 6, 2 2))");
            Geometry<C2D> far = g("POLYGON ((10 10, 11 10, 11 11, 10 11, 10 10))");
            Geometry<C2D> crossing = g("LINESTRING (-1 2, 5 2)");
            Geometry<C2D> edge = g("LINESTRING (0 0, 4 0)");

            assertTrue(o.intersects(square, inner) && !o.disjoint(square, inner));
            assertTrue(o.disjoint(square, far) && !o.intersects(square, far));
            assertTrue(o.within(inner, square) && !o.within(square, inner));
            assertTrue(o.contains(square, inner) && o.covers(square, inner) && o.coveredBy(inner, square));
            assertTrue(o.within(touching, square) && !o.contains(touching, square));
            assertFalse(o.touches(touching, square));
            assertTrue(o.touches(edge, inner) == false && o.touches(edge, square));
            assertTrue(o.coveredBy(edge, square) && !o.within(edge, square));
            assertTrue(o.covers(square, edge) && !o.contains(square, edge));
            assertTrue(o.overlaps(square, overlapping) && !o.overlaps(square, inner));
            assertTrue(o.crosses(crossing, square) && o.crosses(square, crossing));
            assertTrue(o.equalsTopologically(square, g("POLYGON ((4 4, 0 4, 0 0, 4 0, 4 4))")));
            assertTrue(o.equalsTopologically(square, g("POLYGON ((0 0, 2 0, 4 0, 4 4, 0 4, 0 0))")));
            assertFalse(o.equalsTopologically(square, inner));
            assertTrue(o.relates(square, overlapping, "212101212"));
            assertTrue(o.relates(square, inner, "T*****FF*"));
            assertFalse(o.relates(square, far, "T********"));
        }
    }

    @Test
    public void emptyOperands() {
        Geometry<C2D> empty = new Polygon<>(crs), square = g(SQUARE);
        for (ProjectedGeometryOperations o : new ProjectedGeometryOperations[]{ops, jts}) {
            assertFalse(o.intersects(empty, square));
            assertTrue(o.disjoint(empty, square));
            assertTrue(o.disjoint(empty, empty));
            assertFalse(o.within(empty, square));
            assertFalse(o.contains(square, empty));
            assertFalse(o.covers(square, empty));
            assertFalse(o.coveredBy(empty, square));
            assertFalse(o.touches(empty, square));
            assertFalse(o.crosses(empty, square));
            assertFalse(o.overlaps(empty, square));
            assertFalse(o.relates(empty, square, "FF*FF****"));
            assertTrue(o.equalsTopologically(empty, new Point<>(crs)));
            assertFalse(o.equalsTopologically(empty, square));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void differentCrs() {
        ops.intersects(g(SQUARE), Wkt.fromWkt("POINT (1 1)", l72));
    }

    @Test(expected = IllegalArgumentException.class)
    public void differentCrsForNewPredicates() {
        jts.covers(g(SQUARE), Wkt.fromWkt("POINT (1 1)", l72));
    }

    /**
     * Union semantics: adjacent polygons in a collection form one area.
     */
    @Test
    public void unionSemantics() {
        Geometry<C2D> adjacent = g("GEOMETRYCOLLECTION (POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0)), "
                + "POLYGON ((1 0, 2 0, 2 1, 1 1, 1 0)))");
        Geometry<C2D> sharedEdge = g("LINESTRING (1 0.25, 1 0.75)");
        assertTrue(ops.within(sharedEdge, adjacent));
        assertTrue(ops.contains(adjacent, sharedEdge));
        assertFalse(ops.touches(sharedEdge, adjacent));
        assertTrue(ops.equalsTopologically(adjacent, g("POLYGON ((0 0, 2 0, 2 1, 0 1, 0 0))")));
    }

    /**
     * The predicates are exact.
     */
    @Test
    public void exactness() {
        // (1.5, 0.5) lies on the line, its neighbour does not
        Geometry<C2D> line = g("LINESTRING (0 0, 3 1)");
        assertTrue(ops.intersects(g("POINT (1.5 0.5)"), line));
        assertTrue(ops.disjoint(g("POINT (1.5 0.5000000000000001)"), line));
        // three lines through (1/3, 1/3), which is not representable
        Geometry<C2D> a = g("MULTILINESTRING ((0 0, 1 1), (1 0, -1 1))");
        assertTrue(ops.touches(g("LINESTRING (1 -1, 0 1)"), a) == false);
        assertTrue(ops.relates(g("LINESTRING (1 -1, 0 1)"), a, "0F1FF0102"));
        assertTrue(ops.disjoint(g("LINESTRING (1 -1, 0 1)"), g("POLYGON ((1 1, 2 1, 2 2, 1 1))")));
        // a non-simple line where the JTS result is wrong: the interiors share a segment
        assertTrue(ops.relates(g("MULTILINESTRING ((1 4, 2 5, 0 1, 2 3), (3 5, 5 5, 1 4, 3 4))"),
                g("MULTILINESTRING ((6 2, 2 2, 1 1), (1 4, 5 5))"), "101F00102"));
    }

    @Test
    public void convexHull() {
        Geometry<C2D> hull = ops.convexHull(g("MULTIPOINT ((0 0), (4 0), (2 1), (4 4), (0 4), (1 3))"));
        assertTrue(hull instanceof Polygon);
        assertTrue(ops.equalsTopologically(hull, g(SQUARE)));
        assertEquals(g("LINESTRING (0 0, 3 3)"), ops.convexHull(g("MULTIPOINT ((1 1), (0 0), (3 3), (2 2))")));
        assertEquals(g("POINT (1 1)"), ops.convexHull(g("MULTIPOINT ((1 1), (1 1))")));
        assertTrue(ops.convexHull(new Polygon<>(crs)).isEmpty());
    }

    @Test
    public void simplicityValidityAndBoundary() {
        for (ProjectedGeometryOperations o : new ProjectedGeometryOperations[]{ops, jts}) {
            assertTrue(o.isSimple(g("LINESTRING (0 0, 1 1, 2 0)")));
            assertFalse(o.isSimple(g("LINESTRING (0 0, 2 2, 2 0, 0 2)")));
            assertTrue(o.isValid(g(SQUARE)));
            assertFalse(o.isValid(g("POLYGON ((0 0, 4 4, 4 0, 0 4, 0 0))")));
            assertFalse(o.isValid(g("POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (2 0, 4 2, 2 2, 2 0))")));
            assertTrue(o.isValid(g("POLYGON EMPTY")));
            assertTrue(o.equalsTopologically(g("MULTIPOINT ((0 0), (2 0))"), o.boundary(g("LINESTRING (0 0, 1 1, 2 0)"))));
            assertTrue(o.boundary(g("LINESTRING (0 0, 1 1, 2 0, 0 0)")).isEmpty());
            assertTrue(o.equalsTopologically(g("MULTIPOINT ((0 0), (3 0))"),
                    o.boundary(g("MULTILINESTRING ((0 0, 1 1), (1 1, 2 0), (2 0, 3 0))"))));
            assertTrue(o.equalsTopologically(g("LINESTRING (0 0, 4 0, 4 4, 0 4, 0 0)"), o.boundary(g(SQUARE))));
            assertTrue(o.boundary(g("POINT (1 1)")).isEmpty());
        }
        // a hole within an ulp of the shell: it crosses the shell
        double c = 0.1 + 0.2;
        assertFalse(ops.isValid(g("POLYGON ((0 0, 1 1, 0 1, 0 0), (" + c + " " + Math.nextDown(c)
                + ", 0.1 0.8, 0.05 0.9, " + c + " " + Math.nextDown(c) + "))")));
        assertTrue(ops.isValid(g("POLYGON ((0 0, 1 1, 0 1, 0 0), (" + c + " " + c
                + ", 0.1 0.8, 0.05 0.9, " + c + " " + c + "))")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void areaAndCentroid() {
        for (ProjectedGeometryOperations o : new ProjectedGeometryOperations[]{ops, jts}) {
            Polygon<C2D> withHole = (Polygon<C2D>) g("POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1))");
            assertEquals(15.0, o.area(withHole), 0);
            assertEquals(15.0, o.exactArea(withHole), 0);
            Point<C2D> centroid = o.centroid((Polygon<C2D>) g(SQUARE));
            assertEquals(2.0, centroid.getPosition().getX(), 0);
            assertEquals(2.0, centroid.getPosition().getY(), 0);
        }
        // a rectangle of about 1 mm at large coordinates: the exact area is the product of the (exact) differences
        Polygon<C2D> small = (Polygon<C2D>) Wkt.fromWkt(
                "POLYGON ((152345.678 212345.789, 152345.679 212345.789, 152345.679 212345.79, 152345.678 212345.79, "
                        + "152345.678 212345.789))", crs);
        double side = (152345.679 - 152345.678), height = (212345.79 - 212345.789);   // exact differences
        assertEquals(side * height, ops.exactArea(small), 0);
        assertEquals(org.geolatte.geom.cga.Area.fast(small), ops.area(small), 0);
        assertEquals(crs, ops.centroid(small).getCoordinateReferenceSystem());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void distanceAndLength() {
        for (ProjectedGeometryOperations o : new ProjectedGeometryOperations[]{ops, jts}) {
            assertEquals(5.0, o.distance(g("POINT (0 0)"), g("POINT (3 4)")), 0);
            assertEquals(0.0, o.distance(g(SQUARE), g("POINT (1 1)")), 0);
            LineString<C2D> line = (LineString<C2D>) g("LINESTRING (0 0, 3 4, 3 10)");
            assertEquals(11.0, o.length(line), 0);
            assertEquals(11.0, o.exactLength(line), 0);
        }
        // the perpendicular distance 1 / sqrt(5), correctly rounded
        assertEquals(0.4472135954999579, ops.distance(g("POINT (0 1)"), g("LINESTRING (-1 0, 1 1)")), 0);
        assertTrue(Double.isNaN(ops.distance(g("POINT EMPTY"), g(SQUARE))));
        assertTrue(Double.isNaN(ops.distance(g(SQUARE), g("POLYGON EMPTY"))));
        LineString<C2D> line = (LineString<C2D>) g("LINESTRING (152345.678 212345.789, 152346.1 212347.2, 152349 212340)");
        assertEquals(org.geolatte.geom.cga.Length.fast(line), ops.length(line), 0);
        assertEquals(org.geolatte.geom.cga.Length.of(line), ops.exactLength(line), 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void perimeterAndPointOnSurface() {
        for (ProjectedGeometryOperations o : new ProjectedGeometryOperations[]{ops, jts}) {
            Polygon<C2D> withHole = (Polygon<C2D>) g("POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 1 3, 3 3, 3 1, 1 1))");
            assertEquals(24.0, o.perimeter(withHole), 0);
            assertEquals(24.0, o.exactPerimeter(withHole), 0);
            assertTrue(Double.isNaN(o.perimeter((Polygon<C2D>) g("POLYGON EMPTY"))));
            assertTrue(Double.isNaN(o.exactPerimeter((Polygon<C2D>) g("POLYGON EMPTY"))));
            Point<C2D> point = o.pointOnSurface(withHole);
            assertEquals(crs, point.getCoordinateReferenceSystem());
            assertTrue(o.contains(withHole, point));
            assertTrue(o.pointOnSurface((Polygon<C2D>) g("POLYGON EMPTY")).isEmpty());
        }
        MultiPolygon<C2D> squares = (MultiPolygon<C2D>) g("MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), "
                + "((5 0, 15 0, 15 10, 5 10, 5 0)))");
        assertEquals(44.0, ops.perimeter(squares), 0);
        assertEquals(new C2D(10, 5), ops.pointOnSurface(squares).getPosition());
    }

    @Test(expected = IllegalArgumentException.class)
    public void differentCrsForDistance() {
        ops.distance(g(SQUARE), Wkt.fromWkt("POINT (1 1)", l72));
    }

    private static Geometry<C2D> g(String wkt) {
        return Wkt.fromWkt(wkt, crs);
    }
}
