package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.math.BigDecimal;
import java.util.*;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_2D_METER;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_3D_METER;
import static org.junit.Assert.*;

/**
 * Tests {@link DouglasPeucker} against an independent, straightforward implementation of Algorithm 24 of
 * Ratschek &amp; Rokne (recursive, comparing exact squared distances in {@code BigDecimal}).
 */
public class DouglasPeuckerTest {

    private final Random random = new Random(31L);

    @Test
    public void simpleLine() {
        // the peak at (5, 3) is kept, the bumps of 0.5 are removed
        PositionSequence<C2D> line = seq(0, 0, 2, 0.5, 5, 3, 8, 0.5, 10, 0);
        assertKept(line, 1, true, false, true, false, true);
        assertKept(line, 3, true, false, false, false, true);   // distance 3 is within tolerance 3
        assertKept(line, 0.4, true, true, true, true, true);
    }

    @Test
    public void zeroToleranceRemovesExactlyCollinearPositions() {
        PositionSequence<C2D> line = seq(0, 0, 1, 3, 2, 6, 3, 9);
        assertKept(line, 0, true, false, false, true);
        // (1, 3) is on the base line, but not on the line from (0, 0) to the moved position (2, 6 + ulp)
        PositionSequence<C2D> almost = seq(0, 0, 1, 3, 2, Math.nextUp(6.0), 3, 9);
        assertKept(almost, 0, true, true, true, true);
        // (0.1, 0.3) is on the line through (0, 0) and (0.3, 0.9) iff 0.1 * 0.9 == 0.3 * 0.3 for the doubles
        boolean[] keep = DouglasPeucker.keptPositions(seq(0, 0, 0.1, 0.3, 0.3, 0.9), 0);
        boolean collinear = new BigDecimal(0.1).multiply(new BigDecimal(0.9))
                .compareTo(new BigDecimal(0.3).multiply(new BigDecimal(0.3))) == 0;
        assertEquals(!collinear, keep[1]);
    }

    @Test
    public void ties() {
        // (3, 2) and (7, 2) are both at the maximal distance 2 from the base line. The first one is chosen; then
        // (5, 1.9) and (7, 2) are within 1.5 of (3, 2)-(10, 0). Choosing (7, 2) would have kept (7, 2) instead.
        PositionSequence<C2D> line = seq(0, 0, 3, 2, 5, 1.9, 7, 2, 10, 0);
        assertKept(line, 1.5, true, true, false, false, true);
    }

    @Test
    public void degenerateInput() {
        assertEquals(0, DouglasPeucker.keptPositions(seq(), 1).length);
        assertKept(seq(1, 1), 1, true);
        assertKept(seq(1, 1, 2, 2), 1, true, true);
        // a closed line: distances to the start position
        assertKept(seq(0, 0, 4, 0, 4, 4, 0, 4, 0, 0), 1, true, true, true, true, true);
        assertKept(seq(0, 0, 4, 0.5, 4, 4, 0.5, 4, 0, 0), 3, true, false, true, false, true);
        // duplicate positions
        assertKept(seq(0, 0, 0, 0, 5, 5, 5, 5, 10, 0), 0, true, false, true, false, true);
    }

    @Test
    public void invalidInput() {
        assertThrows(IllegalArgumentException.class, () -> DouglasPeucker.keptPositions(seq(0, 0, 1, 1, 2, 0), -1));
        assertThrows(IllegalArgumentException.class, () -> DouglasPeucker.keptPositions(seq(0, 0, 1, 1, 2, 0), Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> DouglasPeucker.keptPositions(seq(0, 0, 1, 1, 2, 0), Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> DouglasPeucker.keptPositions(seq(0, 0, 1, Double.NaN, 2, 0), 1));
    }

    @Test
    public void simplifyLineStrings() {
        LineString<C3D> line = linestring(PROJECTED_3D_METER, c(0, 0, 1), c(2, 0.5, 2), c(5, 3, 3), c(8, 0.5, 4), c(10, 0, 5));
        LineString<C3D> simplified = DouglasPeucker.simplify(line, 1);
        assertEquals(linestring(PROJECTED_3D_METER, c(0, 0, 1), c(5, 3, 3), c(10, 0, 5)), simplified);
        assertSame(line, DouglasPeucker.simplify(line, 0.1));  // nothing removed

        MultiLineString<C2D> lines = multilinestring(PROJECTED_2D_METER,
                linestring(c(0, 0), c(1, 0.1), c(2, 0)), linestring(c(0, 5), c(1, 7), c(2, 5)));
        MultiLineString<C2D> simplifiedLines = DouglasPeucker.simplify(lines, 1);
        assertEquals(2, simplifiedLines.getGeometryN(0).getNumPositions());
        assertEquals(3, simplifiedLines.getGeometryN(1).getNumPositions());
    }

    @Test
    public void randomLines() {
        for (int i = 0; i < 1000; i++) {
            PositionSequence<C2D> line = randomWalk(2 + random.nextInt(300), 150000, 200000, 1);
            double tolerance = random.nextDouble() * 3;
            assertArrayEquals(oracle(line, tolerance), DouglasPeucker.keptPositions(line, tolerance));
        }
    }

    /**
     * Lines on a grid: many exact ties between distances, and distances exactly equal to the tolerance.
     */
    @Test
    public void gridLines() {
        for (int i = 0; i < 3000; i++) {
            PositionSequenceBuilder<C2D> b = PositionSequenceBuilders.variableSized(C2D.class);
            int n = 2 + random.nextInt(30);
            double ox = 150000.25 + random.nextInt(1000), oy = 200000.5 + random.nextInt(1000);
            for (int j = 0; j < n; j++) b.add(c(ox + 0.25 * random.nextInt(9), oy + 0.25 * random.nextInt(9)));
            PositionSequence<C2D> line = b.toPositionSequence();
            double tolerance = 0.25 * random.nextInt(8);
            assertArrayEquals(oracle(line, tolerance), DouglasPeucker.keptPositions(line, tolerance));
        }
    }

    /**
     * Positions whose distances to the base line differ by a few ulps, and tolerances equal to such distances.
     */
    @Test
    public void nearTies() {
        for (int i = 0; i < 3000; i++) {
            double ax = 150000 + 1000 * random.nextDouble(), ay = 200000 + 1000 * random.nextDouble();
            double dx = 100 * random.nextDouble(), dy = 100 * random.nextDouble();
            double len = Math.hypot(dx, dy), nx = -dy / len, ny = dx / len;
            double dist = 10 * random.nextDouble();
            PositionSequenceBuilder<C2D> b = PositionSequenceBuilders.variableSized(C2D.class);
            b.add(c(ax, ay));
            int n = 1 + random.nextInt(8);
            for (int j = 0; j < n; j++) {
                double t = random.nextDouble();
                double side = random.nextBoolean() ? 1 : -1;
                b.add(c(ax + t * dx + side * dist * nx, ay + t * dy + side * dist * ny));
            }
            b.add(c(ax + dx, ay + dy));
            PositionSequence<C2D> line = b.toPositionSequence();
            for (double tolerance : new double[]{dist, Math.nextUp(dist), Math.nextDown(dist), dist / 2}) {
                assertArrayEquals(oracle(line, tolerance), DouglasPeucker.keptPositions(line, tolerance));
            }
        }
    }

    @Test
    public void extremeMagnitudes() {
        for (double scale : new double[]{1e-160, 1e-300, 1e150, 1e300}) {
            for (int i = 0; i < 200; i++) {
                PositionSequence<C2D> line = randomWalk(2 + random.nextInt(50), 0, 0, scale);
                double tolerance = random.nextDouble() * scale;
                assertArrayEquals(oracle(line, tolerance), DouglasPeucker.keptPositions(line, tolerance));
            }
        }
    }

    @Test
    public void agreesWithJtsOnWellConditionedInput() {
        for (int i = 0; i < 1000; i++) {
            PositionSequence<C2D> line = randomWalk(2 + random.nextInt(300), 0, 0, 1);
            double tolerance = random.nextDouble() * 3;
            Coordinate[] coordinates = new Coordinate[line.size()];
            for (int j = 0; j < line.size(); j++) {
                coordinates[j] = new Coordinate(line.getPositionN(j).getX(), line.getPositionN(j).getY());
            }
            Coordinate[] expected = DouglasPeuckerSimplifier.simplify(
                    new GeometryFactory().createLineString(coordinates), tolerance).getCoordinates();
            PositionSequence<C2D> actual = DouglasPeucker.simplify(line, tolerance);
            assertEquals(expected.length, actual.size());
            for (int j = 0; j < expected.length; j++) {
                assertEquals(expected[j].x, actual.getPositionN(j).getX(), 0);
                assertEquals(expected[j].y, actual.getPositionN(j).getY(), 0);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    private static PositionSequence<C2D> seq(double... xy) {
        PositionSequenceBuilder<C2D> b = PositionSequenceBuilders.variableSized(C2D.class);
        for (int i = 0; i < xy.length; i += 2) b.add(c(xy[i], xy[i + 1]));
        return b.toPositionSequence();
    }

    private static void assertKept(PositionSequence<C2D> line, double tolerance, boolean... expected) {
        assertArrayEquals(expected, DouglasPeucker.keptPositions(line, tolerance));
        assertArrayEquals(expected, oracle(line, tolerance));
    }

    private PositionSequence<C2D> randomWalk(int n, double x0, double y0, double step) {
        PositionSequenceBuilder<C2D> b = PositionSequenceBuilders.variableSized(C2D.class);
        double x = x0, y = y0;
        for (int i = 0; i < n; i++) {
            b.add(c(x, y));
            x += step * (random.nextDouble() * 2 - 0.5);
            y += step * (random.nextDouble() * 2 - 1);
        }
        return b.toPositionSequence();
    }

    // Algorithm 24, recursive, with exact squared distances as fractions
    private static boolean[] oracle(PositionSequence<C2D> line, double tolerance) {
        boolean[] keep = new boolean[line.size()];
        if (line.size() == 0) return keep;
        keep[0] = keep[line.size() - 1] = true;
        oracle(line, 0, line.size() - 1, new BigDecimal(tolerance).pow(2), keep);
        return keep;
    }

    private static void oracle(PositionSequence<C2D> line, int first, int last, BigDecimal tolerance2, boolean[] keep) {
        if (last - first < 2) return;
        C2D a = line.getPositionN(first), b = line.getPositionN(last);
        int farthest = -1;
        BigDecimal[] max = null;
        for (int m = first + 1; m < last; m++) {
            BigDecimal[] d = squaredDistance(a, b, line.getPositionN(m));
            if (max == null || d[0].multiply(max[1]).compareTo(max[0].multiply(d[1])) > 0) {
                max = d;
                farthest = m;
            }
        }
        if (max[0].compareTo(tolerance2.multiply(max[1])) <= 0) return;
        keep[farthest] = true;
        oracle(line, first, farthest, tolerance2, keep);
        oracle(line, farthest, last, tolerance2, keep);
    }

    // squared distance of p to segment ab, as fraction {numerator, denominator}
    private static BigDecimal[] squaredDistance(C2D a, C2D b, C2D p) {
        BigDecimal dx = big(b.getX()).subtract(big(a.getX())), dy = big(b.getY()).subtract(big(a.getY()));
        BigDecimal wx = big(p.getX()).subtract(big(a.getX())), wy = big(p.getY()).subtract(big(a.getY()));
        BigDecimal len2 = dx.multiply(dx).add(dy.multiply(dy));
        BigDecimal t = wx.multiply(dx).add(wy.multiply(dy));
        BigDecimal w2 = wx.multiply(wx).add(wy.multiply(wy));
        if (len2.signum() == 0 || t.signum() <= 0) return new BigDecimal[]{w2, BigDecimal.ONE};
        if (t.compareTo(len2) >= 0) {
            BigDecimal vx = big(p.getX()).subtract(big(b.getX())), vy = big(p.getY()).subtract(big(b.getY()));
            return new BigDecimal[]{vx.multiply(vx).add(vy.multiply(vy)), BigDecimal.ONE};
        }
        return new BigDecimal[]{w2.multiply(len2).subtract(t.multiply(t)), len2};
    }

    private static BigDecimal big(double v) {
        return new BigDecimal(v);
    }
}
