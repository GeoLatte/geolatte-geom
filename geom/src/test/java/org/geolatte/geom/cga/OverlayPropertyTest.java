package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

import java.util.*;

import static org.geolatte.geom.cga.Overlay.Operation.*;
import static org.junit.Assert.*;

public class OverlayPropertyTest {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    // -------------------------------------------------------------------------------------------------------
    // exact cases: unions of grid cells, where no coordinate is rounded
    // -------------------------------------------------------------------------------------------------------

    private static Set<Integer> randomCells(Random random, int n, double p) {
        Set<Integer> cells = new HashSet<>();
        for (int i = 0; i < n * n; i++) {
            if (random.nextDouble() < p) cells.add(i);
        }
        return cells;
    }

    private static Polygon<C2D> square(double x, double y, double size) {
        PositionSequence<C2D> ps = PositionSequenceBuilders.fixedSized(5, C2D.class)
                .add(x, y).add(x + size, y).add(x + size, y + size).add(x, y + size).add(x, y).toPositionSequence();
        return new Polygon<>(new LinearRing<>(ps, crs));
    }

    private static Geometry<C2D> cells(Set<Integer> cells, int n, double ox, double oy) {
        List<Geometry<C2D>> squares = new ArrayList<>();
        for (int c : cells) squares.add(square(ox + c % n, oy + c / n, 1));
        return squares.isEmpty() ? Geometries.mkEmptyGeometryCollection(crs) : Geometries.mkGeometryCollection(squares);
    }

    private static Set<Integer> op(Set<Integer> a, Set<Integer> b, Overlay.Operation op) {
        Set<Integer> all = new HashSet<>(a);
        all.addAll(b);
        all.removeIf(c -> !op.test(a.contains(c), b.contains(c)));
        return all;
    }

    @Test
    public void unionsOfGridCellsAreExact() {
        Random random = new Random(211);
        int n = 7;
        for (int k = 0; k < 150; k++) {
            double ox = random.nextBoolean() ? 0 : 152000, oy = random.nextBoolean() ? 0 : 212000;
            Set<Integer> ca = randomCells(random, n, 0.5), cb = randomCells(random, n, 0.5);
            Geometry<C2D> a = Overlay.unaryUnion(cells(ca, n, ox, oy)), b = Overlay.unaryUnion(cells(cb, n, ox, oy));
            assertTrue(a.toString(), Validity.isValid(a));
            assertEquals(ca.size(), Area.of(a), 0);
            for (Overlay.Operation op : Overlay.Operation.values()) {
                Geometry<C2D> r = Overlay.overlay(a, b, op);
                assertTrue(op + " " + r, Validity.isValid(r));
                assertEquals(op.toString(), op(ca, cb, op).size(), Area.of(r), 0);
                // the collections of cells, directly
                Geometry<C2D> direct = Overlay.overlay(cells(ca, n, ox, oy), cells(cb, n, ox, oy), op);
                assertTrue(direct.toString(), Validity.isValid(direct));
                assertTrue(op.toString(), Relate.equalsTopologically(r, direct) || (r.isEmpty() && direct.isEmpty()));
            }
            Geometry<C2D> inter = Overlay.intersection(a, b), union = Overlay.union(a, b);
            Geometry<C2D> diff = Overlay.difference(a, b);
            assertTopologicallyEqual(a, Overlay.union(diff, inter));
            assertTopologicallyEqual(Overlay.symDifference(a, b), Overlay.difference(union, inter));
            assertTopologicallyEqual(inter, Overlay.intersection(b, a));
            assertTopologicallyEqual(union, Overlay.union(b, a));
            assertTopologicallyEqual(Overlay.symDifference(a, b), Overlay.symDifference(b, a));
        }
    }

    private static void assertTopologicallyEqual(Geometry<?> expected, Geometry<?> actual) {
        if (expected.isEmpty() || actual.isEmpty()) {
            assertTrue(expected + " / " + actual, expected.isEmpty() && actual.isEmpty());
        } else {
            assertTrue(expected + " / " + actual, Relate.equalsTopologically(expected, actual));
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // random star polygons, against JTS OverlayNG
    // -------------------------------------------------------------------------------------------------------

    private static double[] ring(Random random, double cx, double cy, double rMin, double rMax, int n, boolean cw) {
        double[] angles = new double[n];
        for (int i = 0; i < n; i++) angles[i] = 2 * Math.PI * random.nextDouble();
        Arrays.sort(angles);
        double[] c = new double[2 * n + 2];
        for (int i = 0; i < n; i++) {
            double r = rMin + (rMax - rMin) * random.nextDouble();
            int j = cw ? n - 1 - i : i;
            c[2 * j] = cx + r * Math.cos(angles[i]);
            c[2 * j + 1] = cy + r * Math.sin(angles[i]);
        }
        c[2 * n] = c[0];
        c[2 * n + 1] = c[1];
        return c;
    }

    private static LinearRing<C2D> linearRing(double[] c) {
        PositionSequenceBuilder<C2D> b = PositionSequenceBuilders.fixedSized(c.length / 2, C2D.class);
        for (int i = 0; i < c.length; i += 2) b.add(c[i], c[i + 1]);
        return new LinearRing<>(b.toPositionSequence(), crs);
    }

    // a random valid star shaped polygon, with an optional hole
    static Polygon<C2D> star(Random random, double cx, double cy, double radius, int n, boolean hole) {
        while (true) {
            LinearRing<C2D> shell = linearRing(ring(random, cx, cy, 0.5 * radius, radius, n, false));
            Polygon<C2D> p = !hole ? new Polygon<>(shell)
                    : new Polygon<>(shell, linearRing(ring(random, cx, cy, 0.1 * radius, 0.45 * radius, n / 2 + 3, true)));
            if (Validity.isValid(p)) return p;
        }
    }

    private static int jtsCode(Overlay.Operation op) {
        switch (op) {
            case INTERSECTION:
                return OverlayNG.INTERSECTION;
            case UNION:
                return OverlayNG.UNION;
            case DIFFERENCE:
                return OverlayNG.DIFFERENCE;
            default:
                return OverlayNG.SYMDIFFERENCE;
        }
    }

    @Test
    public void starPolygonsAgreeWithJts() {
        Random random = new Random(223);
        int jtsInvalid = 0;
        for (int k = 0; k < 400; k++) {
            double ox = random.nextBoolean() ? 0 : 152000 + random.nextDouble();
            double oy = random.nextBoolean() ? 0 : 212000 + random.nextDouble();
            Polygon<C2D> a = star(random, ox, oy, 10, 5 + random.nextInt(40), random.nextBoolean());
            Polygon<C2D> b = star(random, ox + 8 * random.nextDouble(), oy + 8 * random.nextDouble(), 10,
                    5 + random.nextInt(40), random.nextBoolean());
            Geometry<C2D> ab = random.nextInt(4) == 0
                    ? Geometries.mkMultiPolygon(a, star(random, ox + 30, oy, 5, 12, false)) : a;
            double scale = Area.of(ab) + Area.of(b);
            for (Overlay.Operation op : Overlay.Operation.values()) {
                Geometry<C2D> ours = Overlay.overlay(ab, b, op);
                assertTrue(op + " " + ab + " " + b, Validity.isValid(ours));
                org.locationtech.jts.geom.Geometry jts = OverlayNGRobust.overlay(JTS.to(ab), JTS.to(b), jtsCode(op));
                assertEquals(op + " " + ab + " " + b, jts.getArea(), Area.of(ours), 1e-12 * scale);
                Geometry<C2D> theirs = JTS.from(jts, crs);
                if (!Validity.isValid(theirs)) {
                    jtsInvalid++;
                    continue;
                }
                if (!theirs.isEmpty() && !ours.isEmpty()) {
                    assertEquals(0, Area.of(Overlay.symDifference(ours, theirs)), 1e-12 * scale);
                }
            }
        }
        System.out.println("JTS OverlayNG results invalid: " + jtsInvalid);
    }

    // -------------------------------------------------------------------------------------------------------
    // fixed grids
    // -------------------------------------------------------------------------------------------------------

    @Test
    public void starPolygonsOnFixedGrids() {
        Random random = new Random(229);
        String[] sizes = {"0.001", "0.5", "7", "0.03"};
        for (int k = 0; k < 200; k++) {
            Grid grid = Grid.fixed(sizes[k % sizes.length]);
            double g = grid.cellSize().doubleValue();
            double ox = random.nextBoolean() ? 0 : 152000 + random.nextDouble();
            double oy = random.nextBoolean() ? 0 : 212000 + random.nextDouble();
            Polygon<C2D> a = star(random, ox, oy, 10, 5 + random.nextInt(40), random.nextBoolean());
            Polygon<C2D> b = star(random, ox + 8 * random.nextDouble(), oy + 8 * random.nextDouble(), 10,
                    5 + random.nextInt(40), random.nextBoolean());
            // each edge moves by less than a cell diagonal
            double tolerance = 2 * g * (Length.of(a) + Length.of(b));
            for (Overlay.Operation op : Overlay.Operation.values()) {
                Geometry<C2D> ours = Overlay.overlay(a, b, op, grid);
                String message = op + " on " + grid + ": " + a + " " + b;
                assertTrue(message, Validity.isValid(ours));
                OverlayTest.assertOnGrid(ours, grid);
                org.locationtech.jts.geom.Geometry jts = OverlayNG.overlay(JTS.to(a), JTS.to(b), jtsCode(op),
                        JTS.toPrecisionModel(grid));
                assertEquals(message, jts.getArea(), Area.of(ours), tolerance);
            }
            // reducing the precision again keeps the result on the grid and valid, but is not always idempotent:
            // an edge can pass through the cell of another vertex (Hobby's snap rounding, as in JTS)
            Geometry<C2D> reduced = Overlay.reducePrecision(a, grid);
            assertTrue(Validity.isValid(reduced));
            OverlayTest.assertOnGrid(reduced, grid);
            Geometry<C2D> again = Overlay.reducePrecision(reduced, grid);
            assertTrue(Validity.isValid(again));
            OverlayTest.assertOnGrid(again, grid);
            assertEquals(Area.of(reduced), Area.of(again), 2 * g * Length.of(reduced));
        }
    }

    @Test
    public void reducingGeometriesOnTheGridChangesNothing() {
        Random random = new Random(233);
        Grid unit = Grid.fixed(1);
        for (int k = 0; k < 50; k++) {
            int n = 8;
            Geometry<C2D> cells = cells(randomCells(random, n, 0.5), n, 152000, 212000);
            Geometry<C2D> union = Overlay.unaryUnion(cells);
            assertTopologicallyEqual(union, Overlay.reducePrecision(cells, unit));
            assertTopologicallyEqual(union, Overlay.reducePrecision(cells, Grid.fixed("0.001")));
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // near degenerate inputs
    // -------------------------------------------------------------------------------------------------------

    @Test
    public void nearlyCoincidentPolygons() {
        // B is A moved by a few ulps, or rotated by a tiny angle around a vertex: edges nearly overlap
        Random random = new Random(227);
        for (int k = 0; k < 300; k++) {
            double ox = 152000 + random.nextDouble(), oy = 212000 + random.nextDouble();
            Polygon<C2D> a = star(random, ox, oy, 1, 5 + random.nextInt(20), random.nextBoolean());
            Geometry<C2D> b;
            if (random.nextBoolean()) {
                double dx = Math.ulp(ox) * (random.nextInt(7) - 3), dy = Math.ulp(oy) * (random.nextInt(7) - 3);
                b = transform(a, (x, y) -> new double[]{x + dx, y + dy});
            } else {
                double angle = 1e-14 * (random.nextDouble() - 0.5);
                double cx = a.getPositionN(0).getX(), cy = a.getPositionN(0).getY();
                double cos = Math.cos(angle), sin = Math.sin(angle);
                b = transform(a, (x, y) -> new double[]{cx + cos * (x - cx) - sin * (y - cy),
                        cy + sin * (x - cx) + cos * (y - cy)});
            }
            if (!Validity.isValid(b)) continue;
            double scale = Area.of(a);
            for (Overlay.Operation op : Overlay.Operation.values()) {
                Geometry<C2D> r = Overlay.overlay(a, b, op);
                assertTrue(op + " " + a + " " + b + " " + r, Validity.isValid(r));
                double expected = op == UNION || op == INTERSECTION ? scale : 0;
                assertEquals(op.toString(), expected, Area.of(r), 1e-9 * scale);
            }
        }
    }

    @Test
    public void tinyPolygonsAtLargeCoordinates() {
        // all vertices within a few ulps: many crossings and collapses
        Random random = new Random(229);
        for (int k = 0; k < 500; k++) {
            double ox = 152000 + random.nextDouble(), oy = 212000 + random.nextDouble();
            double size = Math.ulp(ox) * (2 + random.nextInt(30));
            Geometry<C2D> a = randomPolygon(random, ox, oy, size), b = randomPolygon(random, ox, oy, size);
            if (a == null || b == null) continue;
            for (Overlay.Operation op : Overlay.Operation.values()) {
                Geometry<C2D> r = Overlay.overlay(a, b, op);
                assertTrue(op + " " + a + " " + b + " " + r, Validity.isValid(r));
            }
        }
    }

    // a random valid polygon (a triangle or a star) with vertices on a grid of ulps, or null if degenerate
    private static Geometry<C2D> randomPolygon(Random random, double ox, double oy, double size) {
        Polygon<C2D> p;
        if (random.nextBoolean()) {
            double u = Math.ulp(ox);
            double[] c = new double[8];
            for (int i = 0; i < 6; i += 2) {
                c[i] = ox + u * Math.floor(random.nextDouble() * size / u);
                c[i + 1] = oy + u * Math.floor(random.nextDouble() * size / u);
            }
            c[6] = c[0];
            c[7] = c[1];
            p = new Polygon<>(linearRing(c));
        } else {
            p = star(random, ox, oy, size, 3 + random.nextInt(8), false);
        }
        return Validity.isValid(p) ? p : null;
    }

    private interface XYTransform {
        double[] apply(double x, double y);
    }

    private static Geometry<C2D> transform(Polygon<C2D> p, XYTransform t) {
        List<LinearRing<C2D>> rings = new ArrayList<>();
        rings.add(transform(p.getExteriorRing(), t));
        for (int i = 0; i < p.getNumInteriorRing(); i++) rings.add(transform(p.getInteriorRingN(i), t));
        return Geometries.mkPolygon(rings);
    }

    private static LinearRing<C2D> transform(LinearRing<C2D> ring, XYTransform t) {
        int n = ring.getNumPositions();
        double[] c = new double[2 * n];
        for (int i = 0; i < n - 1; i++) {
            double[] q = t.apply(ring.getPositionN(i).getX(), ring.getPositionN(i).getY());
            c[2 * i] = q[0];
            c[2 * i + 1] = q[1];
        }
        c[2 * n - 2] = c[0];
        c[2 * n - 1] = c[1];
        return linearRing(c);
    }
}
