package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.Predicates;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.*;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_2D_METER;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_3D_METER;
import static org.junit.Assert.*;

public class ConvexHullTest {

    private static final CoordinateReferenceSystem<C2D> CRS = PROJECTED_2D_METER;

    private final Random random = new Random(23L);

    @Test
    public void squareWithInteriorAndBoundaryPoints() {
        List<C2D> input = Arrays.asList(c(1, 1), c(0, 0), c(2, 0), c(1, 0), c(2, 2), c(0, 2), c(2, 1), c(0.5, 1.5));
        assertEquals(Arrays.asList(c(0, 0), c(2, 0), c(2, 2), c(0, 2)), ConvexHull.vertices(input));
    }

    @Test
    public void degenerateInput() {
        assertEquals(Collections.emptyList(), ConvexHull.vertices(Collections.<C2D>emptyList()));
        assertEquals(Collections.singletonList(c(1, 2)), ConvexHull.vertices(Arrays.asList(c(1, 2), c(1, 2), c(1, 2))));
        // collinear: the two extreme positions
        assertEquals(Arrays.asList(c(0, 0), c(3, 3)),
                ConvexHull.vertices(Arrays.asList(c(1, 1), c(3, 3), c(0, 0), c(2, 2))));
        // vertical
        assertEquals(Arrays.asList(c(1, -5), c(1, 7)),
                ConvexHull.vertices(Arrays.asList(c(1, 0), c(1, 7), c(1, -5))));
        // 0.0 and -0.0 are the same coordinate
        assertEquals(1, ConvexHull.vertices(Arrays.asList(c(0.0, 0.0), c(-0.0, -0.0))).size());
    }

    @Test
    public void verticesAreInputPositions() {
        C3D a = c(0, 0, 10), b = c(4, 0, 20), d = c(0, 4, 30), inside = c(1, 1, 40);
        C3D duplicateOfA = c(0, 0, 50);
        List<C3D> hull = ConvexHull.vertices(Arrays.asList(inside, a, duplicateOfA, b, d));
        assertEquals(3, hull.size());
        assertSame(a, hull.get(0));    // the first of the positions with equal X and Y
        assertSame(b, hull.get(1));
        assertSame(d, hull.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonFiniteCoordinatesAreRejected() {
        ConvexHull.vertices(Arrays.asList(c(0, 0), c(1, Double.NaN), c(2, 0)));
    }

    @Test
    public void hullOfGeometries() {
        assertTrue(ConvexHull.of(new Point<>(CRS)).isEmpty());
        assertEquals(GeometryType.GEOMETRYCOLLECTION, ConvexHull.of(new Point<>(CRS)).getGeometryType());
        assertEquals(point(CRS, c(1, 1)), ConvexHull.of(multipoint(CRS, c(1, 1), c(1, 1))));
        assertEquals(linestring(CRS, c(0, 0), c(2, 2)), ConvexHull.of(linestring(CRS, c(1, 1), c(0, 0), c(2, 2))));
        Polygon<C2D> withHole = polygon(ring(CRS, c(0, 0), c(0, 4), c(4, 4), c(4, 0), c(0, 0)),
                ring(CRS, c(1, 1), c(2, 1), c(2, 2), c(1, 1)));
        assertEquals(polygon(ring(CRS, c(0, 0), c(4, 0), c(4, 4), c(0, 4), c(0, 0))), ConvexHull.of(withHole));
        Geometry<C3D> hull3d = ConvexHull.of(multipoint(PROJECTED_3D_METER, c(0, 0, 1), c(3, 0, 2), c(0, 3, 3)));
        assertEquals(PROJECTED_3D_METER, hull3d.getCoordinateReferenceSystem());
        assertEquals(c(3, 0, 2), hull3d.getPositionN(1));
    }

    @Test
    public void randomPointSets() {
        for (int i = 0; i < 2000; i++) {
            List<C2D> points = new ArrayList<>();
            int n = 1 + random.nextInt(200);
            for (int j = 0; j < n; j++) points.add(c(150000 + 1000 * random.nextDouble(), 200000 + 1000 * random.nextDouble()));
            checkHull(points);
        }
    }

    /**
     * Points on a line, perturbed by at most a few ulps, in projected coordinates: the hull is very thin.
     */
    @Test
    public void nearlyCollinearProjectedPoints() {
        for (int i = 0; i < 2000; i++) {
            checkHull(nearlyCollinear(150000 + 1000 * random.nextDouble(), 200000 + 1000 * random.nextDouble(), 100));
        }
    }

    /**
     * Nearly collinear points whose extent is comparable to their coordinates: here naive floating point
     * orientation tests produce hulls that are not convex, or that miss points (cf. Kettner et al., "Classroom
     * examples of robustness problems in geometric computations", 2008).
     */
    @Test
    public void nearlyCollinearPointsNearOrigin() {
        int naiveFailures = 0;
        for (int i = 0; i < 2000; i++) {
            List<C2D> points = nearlyCollinear(random.nextDouble(), random.nextDouble(), 20);
            checkHull(points);
            if (!isValidHull(naiveHull(points), points)) naiveFailures++;
        }
        assertTrue("naive hull failures: " + naiveFailures, naiveFailures > 1000);
    }

    // points (x0, y0) + t (dx, dy), t in [0, length], with y moved by up to 2 ulps
    private List<C2D> nearlyCollinear(double x0, double y0, double length) {
        List<C2D> points = new ArrayList<>();
        double dx = random.nextDouble(), dy = random.nextDouble();
        int n = 3 + random.nextInt(50);
        for (int j = 0; j < n; j++) {
            double t = length * random.nextDouble();
            double x = x0 + t * dx, y = y0 + t * dy;
            for (int k = random.nextInt(3); k > 0; k--) y = random.nextBoolean() ? Math.nextUp(y) : Math.nextDown(y);
            points.add(c(x, y));
        }
        return points;
    }

    /**
     * Points on a grid, with many collinear and duplicate points
     */
    @Test
    public void gridPoints() {
        for (int i = 0; i < 2000; i++) {
            List<C2D> points = new ArrayList<>();
            int n = 1 + random.nextInt(100);
            for (int j = 0; j < n; j++) points.add(c(random.nextInt(6) * 0.1, random.nextInt(6) * 0.1));
            checkHull(points);
        }
    }

    /**
     * Large sets, where positions inside the octagon of extreme positions are removed before sorting. Includes
     * duplicated extremes and positions on the edges of the octagon.
     */
    @Test
    public void largePointSets() {
        for (int i = 0; i < 100; i++) {
            List<C2D> points = new ArrayList<>();
            int n = 100 + random.nextInt(5000);
            for (int j = 0; j < n; j++) {
                switch (i % 4) {
                    case 0:
                        points.add(c(150000 + 1000 * random.nextDouble(), 200000 + 1000 * random.nextDouble()));
                        break;
                    case 1: // on a circle, all hull vertices up to rounding
                        double t = 2 * Math.PI * random.nextDouble();
                        points.add(c(Math.cos(t), Math.sin(t)));
                        break;
                    case 2: // a diamond, with many positions on its edges
                        double u = random.nextInt(11) / 10.0;
                        int side = random.nextInt(4);
                        points.add(c((side < 2 ? 1 : -1) * u, (side % 2 == 0 ? 1 : -1) * (1 - u)));
                        break;
                    default: // a small grid: duplicated extremes
                        points.add(c(random.nextInt(4), random.nextInt(4)));
                }
            }
            checkHull(points);
        }
    }

    @Test
    public void resultIsIndependentOfInputOrder() {
        for (int i = 0; i < 500; i++) {
            List<C2D> points = new ArrayList<>();
            int n = 3 + random.nextInt(100);
            for (int j = 0; j < n; j++) points.add(c(random.nextInt(10), random.nextInt(10)));
            List<C2D> hull = ConvexHull.vertices(points);
            Collections.shuffle(points, random);
            assertEquals(hull, ConvexHull.vertices(points));
        }
    }

    @Test
    public void agreesWithJtsOnWellConditionedInput() {
        GeometryFactory factory = new GeometryFactory();
        for (int i = 0; i < 1000; i++) {
            List<C2D> points = new ArrayList<>();
            Coordinate[] coordinates = new Coordinate[3 + random.nextInt(100)];
            for (int j = 0; j < coordinates.length; j++) {
                C2D p = c(1000 * random.nextDouble(), 1000 * random.nextDouble());
                points.add(p);
                coordinates[j] = new Coordinate(p.getX(), p.getY());
            }
            Set<Coordinate> expected = new HashSet<>(Arrays.asList(
                    new org.locationtech.jts.algorithm.ConvexHull(coordinates, factory).getConvexHull().getCoordinates()));
            Set<Coordinate> actual = new HashSet<>();
            for (C2D p : ConvexHull.vertices(points)) actual.add(new Coordinate(p.getX(), p.getY()));
            assertEquals(expected, actual);
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    private static void checkHull(List<C2D> points) {
        List<C2D> hull = ConvexHull.vertices(points);
        assertTrue("Invalid hull " + hull, isValidHull(hull, points));
        // starts at the smallest (x, y)
        for (C2D p : points) {
            C2D first = hull.get(0);
            assertTrue(first.getX() < p.getX() || (first.getX() == p.getX() && first.getY() <= p.getY()));
        }
    }

    /**
     * A list of input points is the convex hull iff it is strictly convex (each consecutive triple is a strict left
     * turn) and all points lie inside or on its boundary. Checked with the exact orientation predicate.
     */
    private static boolean isValidHull(List<C2D> hull, List<C2D> points) {
        int n = hull.size();
        if (n == 0) return points.isEmpty();
        for (C2D p : points) {
            if (n == 1 && (p.getX() != hull.get(0).getX() || p.getY() != hull.get(0).getY())) return false;
            if (n == 2 && (Predicates.orientation(hull.get(0), hull.get(1), p) != 0 || !between(hull.get(0), hull.get(1), p))) {
                return false;
            }
        }
        if (n <= 2) {
            return n == 1 || hull.get(0).getX() != hull.get(1).getX() || hull.get(0).getY() != hull.get(1).getY();
        }
        for (int i = 0; i < n; i++) {
            C2D a = hull.get(i), b = hull.get((i + 1) % n), c = hull.get((i + 2) % n);
            if (Predicates.orientation(a, b, c) <= 0) return false;
            for (C2D p : points) {
                if (Predicates.orientation(a, b, p) < 0) return false;
            }
        }
        return true;
    }

    private static boolean between(C2D a, C2D b, C2D p) {
        return Math.min(a.getX(), b.getX()) <= p.getX() && p.getX() <= Math.max(a.getX(), b.getX())
                && Math.min(a.getY(), b.getY()) <= p.getY() && p.getY() <= Math.max(a.getY(), b.getY());
    }

    // the same algorithm with the plain floating point orientation test
    private static List<C2D> naiveHull(List<C2D> points) {
        TreeSet<C2D> unique = new TreeSet<>(Comparator.comparingDouble(C2D::getX).thenComparingDouble(C2D::getY));
        unique.addAll(points);
        List<C2D> sorted = new ArrayList<>(unique);
        if (sorted.size() <= 2) return sorted;
        List<C2D> hull = new ArrayList<>();
        for (int pass = 0; pass < 2; pass++) {
            int start = hull.size();
            for (int i = 0; i < sorted.size(); i++) {
                C2D p = sorted.get(pass == 0 ? i : sorted.size() - 1 - i);
                while (hull.size() >= start + 2 && naiveOrientation(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0) {
                    hull.remove(hull.size() - 1);
                }
                hull.add(p);
            }
            hull.remove(hull.size() - 1);
        }
        return hull;
    }

    private static double naiveOrientation(C2D a, C2D b, C2D c) {
        return (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
    }
}
