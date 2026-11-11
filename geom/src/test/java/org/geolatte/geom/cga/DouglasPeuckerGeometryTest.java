package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_2D_METER;
import static org.junit.Assert.*;

/**
 * Tests the simplification of polygons and collections by {@link DouglasPeucker}, with and without topology
 * preservation.
 */
public class DouglasPeuckerGeometryTest {

    private final Random random = new Random(17L);

    // a square with an outward spike at the top, and a small hole in the spike
    private static final LinearRing<C2D> SPIKED_SHELL = ring(PROJECTED_2D_METER,
            c(0, 0), c(10, 0), c(10, 10), c(6, 10), c(5, 13), c(4, 10), c(0, 10), c(0, 0));
    private static final LinearRing<C2D> HOLE_IN_SPIKE = ring(PROJECTED_2D_METER,
            c(4.9, 10.9), c(5.1, 10.9), c(5.1, 11.1), c(4.9, 11.1), c(4.9, 10.9));

    // ---------------------------------------------------------------------------------------------------------
    // without topology preservation
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void collapsedHoleIsRemoved() {
        Polygon<C2D> polygon = polygon(SPIKED_SHELL, HOLE_IN_SPIKE);
        Polygon<C2D> simplified = DouglasPeucker.simplify(polygon, 0.5);
        assertEquals(polygon(SPIKED_SHELL), simplified);
    }

    @Test
    public void unchangedPolygonIsReturnedAsIs() {
        Polygon<C2D> polygon = polygon(SPIKED_SHELL, HOLE_IN_SPIKE);
        assertSame(polygon, DouglasPeucker.simplify(polygon, 0.01));
        assertSame(polygon, DouglasPeucker.simplify(polygon, 0.01, false));
        assertSame(polygon, DouglasPeucker.simplify(polygon, 0.01, true));
    }

    @Test
    public void plainSimplificationRemovesSpike() {
        // the spike is within 3.5 of the top edge, so it is removed (see randomPolygonsRemainValid for cases where
        // this makes the polygon invalid)
        Polygon<C2D> polygon = polygon(SPIKED_SHELL, HOLE_IN_SPIKE);
        Polygon<C2D> simplified = DouglasPeucker.simplify(polygon, 3.5);
        assertEquals(polygon(ring(PROJECTED_2D_METER, c(0, 0), c(10, 0), c(10, 10), c(0, 10), c(0, 0))), simplified);
    }

    @Test
    public void collapsedShellGivesEmptyPolygon() {
        Polygon<C2D> small = polygon(ring(PROJECTED_2D_METER, c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0)));
        Polygon<C2D> simplified = DouglasPeucker.simplify(small, 2);
        assertTrue(simplified.isEmpty());
        assertEquals(PROJECTED_2D_METER, simplified.getCoordinateReferenceSystem());

        Polygon<C2D> large = polygon(SPIKED_SHELL);
        MultiPolygon<C2D> both = multipolygon(small, large);
        assertEquals(multipolygon(DouglasPeucker.simplify(large, 2)), DouglasPeucker.simplify(both, 2));
        MultiPolygon<C2D> none = DouglasPeucker.simplify(multipolygon(small), 2);
        assertTrue(none.isEmpty());
        assertEquals(PROJECTED_2D_METER, none.getCoordinateReferenceSystem());
    }

    @Test
    public void genericSimplificationWithoutTopology() {
        LinearRing<C2D> smallRing = ring(PROJECTED_2D_METER, c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0));
        LinearRing<C2D> collapsed = DouglasPeucker.simplify(smallRing, 2, false);
        assertTrue(collapsed.isEmpty());

        LineString<C2D> line = linestring(PROJECTED_2D_METER, c(0, 0), c(2, 0.5), c(5, 3), c(8, 0.5), c(10, 0));
        LineString<C2D> simplifiedLine = DouglasPeucker.simplify(line, 1, false);
        assertEquals(DouglasPeucker.simplify(line, 1), simplifiedLine);

        Point<C2D> point = point(PROJECTED_2D_METER, c(3, 4));
        assertSame(point, DouglasPeucker.simplify(point, 1, false));

        GeometryCollection<C2D> collection = geometrycollection(point, line, polygon(smallRing), polygon(SPIKED_SHELL));
        GeometryCollection<C2D> simplified = DouglasPeucker.simplify(collection, 2, false);
        assertEquals(geometrycollection(point, DouglasPeucker.simplify(line, 2),
                DouglasPeucker.simplify(polygon(SPIKED_SHELL), 2)), simplified);
    }

    @Test
    public void invalidTolerance() {
        Polygon<C2D> polygon = polygon(SPIKED_SHELL);
        assertThrows(IllegalArgumentException.class, () -> DouglasPeucker.simplify(polygon, -1));
        assertThrows(IllegalArgumentException.class, () -> DouglasPeucker.simplify(polygon, Double.NaN, true));
        assertThrows(IllegalArgumentException.class,
                () -> DouglasPeucker.simplify(polygon, Double.POSITIVE_INFINITY, false));
    }

    // ---------------------------------------------------------------------------------------------------------
    // with topology preservation
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void holeDoesNotLeaveShell() {
        Polygon<C2D> polygon = polygon(SPIKED_SHELL, HOLE_IN_SPIKE);
        Polygon<C2D> simplified = DouglasPeucker.simplify(polygon, 3.5, true);
        // the spike can not be removed, but its sides are simplified
        assertEquals(ring(PROJECTED_2D_METER, c(0, 0), c(10, 0), c(10, 10), c(5, 13), c(0, 10), c(0, 0)),
                simplified.getExteriorRing());
        // the hole keeps 4 positions
        assertEquals(1, simplified.getNumInteriorRing());
        assertEquals(4, simplified.getInteriorRingN(0).getNumPositions());
        assertTrue(JTS.to(simplified).isValid());
    }

    @Test
    public void ringsKeepFourPositions() {
        LinearRing<C2D> square = ring(PROJECTED_2D_METER, c(0, 0), c(10, 0), c(10, 10), c(0, 10), c(0, 0));
        LinearRing<C2D> simplified = DouglasPeucker.simplify(square, 100, true);
        assertEquals(ring(PROJECTED_2D_METER, c(0, 0), c(10, 10), c(0, 10), c(0, 0)), simplified);
        MultiPolygon<C2D> polygons = DouglasPeucker.simplify(multipolygon(polygon(square)), 100, true);
        assertEquals(multipolygon(polygon(simplified)), polygons);
    }

    @Test
    public void closedLineDoesNotCollapse() {
        LineString<C2D> closed = linestring(PROJECTED_2D_METER, c(0, 0), c(10, 0), c(10, 10), c(0, 10), c(0, 0));
        LineString<C2D> simplified = DouglasPeucker.simplify(closed, 100, true);
        assertEquals(4, simplified.getNumPositions());
        assertEquals(2, DouglasPeucker.simplify(closed, 100).getNumPositions());  // plain: a point
    }

    @Test
    public void lineDoesNotJumpOverOtherLine() {
        LineString<C2D> a = linestring(PROJECTED_2D_METER, c(0, 0), c(5, 1), c(10, 0));
        LineString<C2D> b = linestring(PROJECTED_2D_METER, c(4.5, 0.5), c(5.5, 0.5));
        MultiLineString<C2D> lines = multilinestring(a, b);
        assertEquals(multilinestring(linestring(PROJECTED_2D_METER, c(0, 0), c(10, 0)), b),
                DouglasPeucker.simplify(lines, 2, false));
        assertSame(lines, DouglasPeucker.simplify(lines, 2, true));
        // without the other line, the peak is removed
        LineString<C2D> c = linestring(PROJECTED_2D_METER, c(4.5, -0.5), c(5.5, -0.5));
        assertEquals(multilinestring(linestring(PROJECTED_2D_METER, c(0, 0), c(10, 0)), c),
                DouglasPeucker.simplify(multilinestring(a, c), 2, true));
    }

    @Test
    public void newSegmentDoesNotCreateIntersection() {
        // b crosses the base line
        LineString<C2D> a = linestring(PROJECTED_2D_METER, c(0, 0), c(5, 1), c(10, 0));
        LineString<C2D> b = linestring(PROJECTED_2D_METER, c(2, 0.2), c(2, -1));
        assertSame(a, DouglasPeucker.simplify(multilinestring(a, b), 2, true).getGeometryN(0));
    }

    @Test
    public void contactsArePreserved() {
        // b ends on the vertex (5, 1) of a, and on the interior of a segment of a
        LineString<C2D> a = linestring(PROJECTED_2D_METER, c(0, 0), c(5, 1), c(10, 0));
        LineString<C2D> toVertex = linestring(PROJECTED_2D_METER, c(5, 1), c(5, 3));
        LineString<C2D> toSegment = linestring(PROJECTED_2D_METER, c(2.5, 0.5), c(2.5, 3));
        assertSame(a, DouglasPeucker.simplify(multilinestring(a, toVertex), 2, true).getGeometryN(0));
        assertSame(a, DouglasPeucker.simplify(multilinestring(a, toSegment), 2, true).getGeometryN(0));
        // a contact at the end of a section is no obstacle
        LineString<C2D> toEnd = linestring(PROJECTED_2D_METER, c(10, 0), c(10, 3));
        assertEquals(2, DouglasPeucker.simplify(multilinestring(a, toEnd), 2, true).getGeometryN(0).getNumPositions());
    }

    @Test
    public void pointsInCollectionsAreObstacles() {
        LineString<C2D> a = linestring(PROJECTED_2D_METER, c(0, 0), c(5, 1), c(10, 0));
        Point<C2D> inside = point(PROJECTED_2D_METER, c(5, 0.5));
        Point<C2D> outside = point(PROJECTED_2D_METER, c(5, 1.5));
        GeometryCollection<C2D> blocked = DouglasPeucker.simplify(geometrycollection(a, inside), 2, true);
        assertSame(a, blocked.getGeometryN(0));
        assertSame(inside, blocked.getGeometryN(1));
        GeometryCollection<C2D> free = DouglasPeucker.simplify(geometrycollection(a, outside), 2, true);
        assertEquals(2, free.getGeometryN(0).getNumPositions());
        // points are not changed
        MultiPoint<C2D> points = multipoint(inside, outside);
        assertSame(points, DouglasPeucker.simplify(points, 2, true));
    }

    @Test
    public void selfIntersectionIsNotCreated() {
        // a line that hooks back under its own bump: removing the bump at (5, 0.8) cuts through the last segment
        LineString<C2D> line = linestring(PROJECTED_2D_METER,
                c(0, 0), c(5, 0.8), c(10, 0), c(9.5, -0.5), c(5, -0.8), c(5, 0.5));
        assertTrue(JTS.to(line).isSimple());
        assertEquals(linestring(PROJECTED_2D_METER, c(0, 0), c(10, 0), c(5, -0.8), c(5, 0.5)),
                DouglasPeucker.simplify(line, 1));
        assertFalse(JTS.to(DouglasPeucker.simplify(line, 1)).isSimple());
        assertEquals(linestring(PROJECTED_2D_METER, c(0, 0), c(5, 0.8), c(10, 0), c(5, -0.8), c(5, 0.5)),
                DouglasPeucker.simplify(line, 1, true));
    }

    @Test
    public void randomPolygonsRemainValid() {
        int plainInvalid = 0;
        for (int trial = 0; trial < 200; trial++) {
            Polygon<C2D> polygon = randomPolygonWithHolesNearShell();
            assertTrue(JTS.to(polygon).isValid());
            double tolerance = 0.1 + random.nextDouble();
            Polygon<C2D> simplified = DouglasPeucker.simplify(polygon, tolerance, true);
            assertTrue("trial " + trial, JTS.to(simplified).isValid());
            assertEquals(polygon.getNumInteriorRing(), simplified.getNumInteriorRing());
            assertSubsequences(polygon, simplified);
            if (!JTS.to(DouglasPeucker.simplify(polygon, tolerance)).isValid()) plainInvalid++;
        }
        // the test cases are hard: plain simplification breaks many of them
        assertTrue("plain invalid: " + plainInvalid, plainInvalid > 20);
    }

    @Test
    public void randomLinesKeepTheirIntersections() {
        for (int trial = 0; trial < 100; trial++) {
            List<LineString<C2D>> lines = new ArrayList<>();
            for (int l = 0; l < 4; l++) {
                lines.add(randomWalk(50, random.nextDouble() * 10, random.nextDouble() * 10));
            }
            MultiLineString<C2D> input = Geometries.mkMultiLineString(lines);
            MultiLineString<C2D> simplified = DouglasPeucker.simplify(input, 0.5 + random.nextDouble(), true);
            assertSubsequences(input, simplified);
            for (int i = 0; i < lines.size(); i++) {
                for (int j = i + 1; j < lines.size(); j++) {
                    org.locationtech.jts.geom.Geometry before = JTS.to(lines.get(i)).intersection(JTS.to(lines.get(j)));
                    org.locationtech.jts.geom.Geometry after = JTS.to(simplified.getGeometryN(i))
                            .intersection(JTS.to(simplified.getGeometryN(j)));
                    assertTrue("trial " + trial, before.equalsExact(after) || before.equalsTopo(after));
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    // every ring or line of the simplified geometry consists of a subsequence of the positions of the original one,
    // with the same first and last positions
    private static void assertSubsequences(Geometry<C2D> original, Geometry<C2D> simplified) {
        List<LineString<C2D>> a = lines(original), b = lines(simplified);
        assertEquals(a.size(), b.size());
        for (int l = 0; l < a.size(); l++) {
            PositionSequence<C2D> p = a.get(l).getPositions(), q = b.get(l).getPositions();
            assertEquals(p.getPositionN(0), q.getPositionN(0));
            assertEquals(p.getPositionN(p.size() - 1), q.getPositionN(q.size() - 1));
            int j = 0;
            for (int i = 0; i < p.size() && j < q.size(); i++) {
                if (p.getPositionN(i).equals(q.getPositionN(j))) j++;
            }
            assertEquals(q.size(), j);
        }
    }

    private static List<LineString<C2D>> lines(Geometry<C2D> geometry) {
        List<LineString<C2D>> result = new ArrayList<>();
        if (geometry instanceof LineString) {
            result.add((LineString<C2D>) geometry);
        } else if (geometry instanceof Polygon) {
            Polygon<C2D> polygon = (Polygon<C2D>) geometry;
            result.add(polygon.getExteriorRing());
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) result.add(polygon.getInteriorRingN(i));
        } else if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<C2D, ?> collection = (AbstractGeometryCollection<C2D, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) result.addAll(lines(collection.getGeometryN(i)));
        }
        return result;
    }

    // a noisy circle of radius 10, with noisy circular holes of radius 1 just inside the boundary
    private Polygon<C2D> randomPolygonWithHolesNearShell() {
        List<LinearRing<C2D>> rings = new ArrayList<>();
        rings.add(noisyCircle(0, 0, 10, 0.3, 200));
        int holes = 1 + random.nextInt(6);
        double offset = random.nextDouble() * 2 * Math.PI;
        for (int h = 0; h < holes; h++) {
            double angle = offset + 2 * Math.PI * h / holes;
            double r = 8.65 + random.nextDouble() * 0.1;
            rings.add(noisyCircle(r * Math.cos(angle), r * Math.sin(angle), 1, 0.1, 16));
        }
        return Geometries.mkPolygon(rings);
    }

    private LinearRing<C2D> noisyCircle(double cx, double cy, double radius, double noise, int n) {
        PositionSequenceBuilder<C2D> builder = PositionSequenceBuilders.fixedSized(n + 1, C2D.class);
        C2D first = null;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double r = radius + (random.nextDouble() - 0.5) * noise;
            C2D p = new C2D(cx + r * Math.cos(angle), cy + r * Math.sin(angle));
            if (first == null) first = p;
            builder.add(p);
        }
        builder.add(first);
        return new LinearRing<>(builder.toPositionSequence(), PROJECTED_2D_METER);
    }

    private LineString<C2D> randomWalk(int n, double x, double y) {
        PositionSequenceBuilder<C2D> builder = PositionSequenceBuilders.fixedSized(n, C2D.class);
        for (int i = 0; i < n; i++) {
            builder.add(new C2D(x, y));
            x += random.nextGaussian() * 0.5;
            y += random.nextGaussian() * 0.5;
        }
        return new LineString<>(builder.toPositionSequence(), PROJECTED_2D_METER);
    }
}
