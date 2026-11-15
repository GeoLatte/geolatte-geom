package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class RoundingCellTest {

    private static boolean even(double v) {
        return (Double.doubleToRawLongBits(v + 0.0) & 1) == 0;
    }

    @Test
    public void endpointsAndFarCells() {
        assertTrue(RoundingCell.intersects(0, 0, 1, 1, 0, 0));
        assertTrue(RoundingCell.intersects(0, 0, 1, 1, 1, 1));
        assertTrue(RoundingCell.intersects(0, 0, 1, 1, 0.5, 0.5));
        assertFalse(RoundingCell.intersects(0, 0, 1, 1, 0.5, 0.6));
        assertFalse(RoundingCell.intersects(0, 0, 1, 1, 2, 2));
        // a degenerate segment: a point, which only lies in its own cell
        assertTrue(RoundingCell.intersects(3, 4, 3, 4, 3, 4));
        assertFalse(RoundingCell.intersects(3, 4, 3, 4, Math.nextUp(3.0), 4));
        assertThrows(IllegalArgumentException.class, () -> RoundingCell.intersects(0, 0, Double.NaN, 1, 0, 0));
    }

    @Test
    public void passingExactlyThroughACorner() {
        // the segment from (x, next(y)) to (next(x), y) passes through the corner shared by the cells of (x, y),
        // (next(x), y), (x, next(y)) and (next(x), next(y)); the corner belongs to the cell whose coordinates are
        // both even
        double[] values = {1.0, Math.nextUp(1.0), 3.0, Math.nextUp(3.0), 0x1p52 - 1, 0x1p52 - 2, 1e5, Math.nextUp(1e5),
                0.0, Double.MIN_VALUE, -2.5, Math.nextDown(-2.5)};
        for (double x : values) {
            for (double y : values) {
                double nx = Math.nextUp(x), ny = Math.nextUp(y);
                // the corner is the midpoint of the segment
                assertEquals(x + " " + y, even(x) && even(y), RoundingCell.intersects(x, ny, nx, y, x, y));
                assertEquals(x + " " + y, even(nx) && even(ny), RoundingCell.intersects(x, ny, nx, y, nx, ny));
                // the endpoints' own cells
                assertTrue(RoundingCell.intersects(x, ny, nx, y, x, ny));
                assertTrue(RoundingCell.intersects(x, ny, nx, y, nx, y));
            }
        }
    }

    @Test
    public void cellsAroundPowersOfTwo() {
        // the cell of 2 extends half as far below as above
        double two = 2.0, below = Math.nextDown(2.0), above = Math.nextUp(2.0);
        BigDecimal[] cell = RoundingCell.cell(two);
        assertEquals(new BigDecimal(two).subtract(new BigDecimal(two - below).divide(BigDecimal.valueOf(2))), cell[0]);
        assertEquals(new BigDecimal(two).add(new BigDecimal(above - two).divide(BigDecimal.valueOf(2))), cell[1]);
        // a vertical segment at x = 2 from y = below to y = above passes through the cells of all three
        for (double y : new double[]{below, two, above}) {
            assertTrue(RoundingCell.intersects(2, below, 2, above, 2, y));
        }
        // the largest double, and the cells of 0 and of the smallest subnormal
        assertTrue(RoundingCell.intersects(Double.MAX_VALUE, 0, -Double.MAX_VALUE, 0, 0, 0));
        assertTrue(RoundingCell.intersects(-Double.MIN_VALUE, -1, Double.MIN_VALUE, 1, 0, 0));
        assertFalse(RoundingCell.intersects(Double.MIN_VALUE, -1, 2 * Double.MIN_VALUE, 1, 0, 0));
    }

    @Test
    public void roundedCrossingPointsLieInTheCellsOfBothSegments() {
        Random random = new Random(83);
        for (int k = 0; k < 20000; k++) {
            double scale = Math.scalb(1.0, random.nextInt(60) - 30);
            double ox = random.nextInt(3) == 0 ? 0 : 152000 * random.nextDouble();
            double[] c = new double[8];
            for (int i = 0; i < 8; i++) c[i] = ox + scale * random.nextDouble();
            int code = Predicates.classifySegments(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]);
            if ((code & Predicates.KIND_MASK) != Predicates.CROSSING_CODE) continue;
            double[] x = Constructions.crossingPoint(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]);
            assertTrue(RoundingCell.intersects(c[0], c[1], c[2], c[3], x[0], x[1]));
            assertTrue(RoundingCell.intersects(c[4], c[5], c[6], c[7], x[0], x[1]));
        }
    }

    @Test
    public void roundedPointsOfASegmentLieInCellsThatItMeetsInOrder() {
        Random random = new Random(89);
        for (int k = 0; k < 3000; k++) {
            double scale = Math.scalb(1.0, random.nextInt(40) - 50);
            double ox = random.nextBoolean() ? 0 : 152000 + random.nextDouble();
            double oy = random.nextBoolean() ? 0 : 212000 + random.nextDouble();
            double ax = ox + scale * random.nextDouble(), ay = oy + scale * random.nextDouble();
            double bx = ox + scale * random.nextDouble(), by = oy + scale * random.nextDouble();
            // points a + t (b - a) for t = i / n, exactly, rounded: their cells meet the segment, in the order of t
            int n = 64;
            List<double[]> cells = new ArrayList<>();
            BigDecimal a0 = new BigDecimal(ax), a1 = new BigDecimal(ay);
            BigDecimal d0 = new BigDecimal(bx).subtract(a0), d1 = new BigDecimal(by).subtract(a1);
            for (int i = 0; i <= n; i++) {
                BigDecimal t = BigDecimal.valueOf(i).divide(BigDecimal.valueOf(n));
                double x = a0.add(d0.multiply(t)).doubleValue(), y = a1.add(d1.multiply(t)).doubleValue();
                assertTrue(RoundingCell.intersects(ax, ay, bx, by, x, y));
                double[] last = cells.isEmpty() ? null : cells.get(cells.size() - 1);
                if (last == null || last[0] != x || last[1] != y) cells.add(new double[]{x, y});
            }
            for (int i = 0; i + 1 < cells.size(); i++) {
                double[] p = cells.get(i), q = cells.get(i + 1);
                if (p[0] == q[0] && p[1] == q[1]) continue;
                assertTrue(RoundingCell.compareAlong(ax, ay, bx, by, p[0], p[1], q[0], q[1]) < 0);
                assertTrue(RoundingCell.compareAlong(ax, ay, bx, by, q[0], q[1], p[0], p[1]) > 0);
            }
        }
    }

    @Test
    public void cellsBesideASegment() {
        // the cell of a point at distance d from the segment, rounded: met iff the exact clipping says so; check
        // against the definition with the nearest point of the segment for points clearly off it
        Random random = new Random(97);
        for (int k = 0; k < 20000; k++) {
            double ax = 152000 + random.nextDouble(), ay = 212000 + random.nextDouble();
            double bx = ax + random.nextDouble(), by = ay + random.nextDouble();
            double t = random.nextDouble();
            double ulps = random.nextInt(5);
            double px = ax + t * (bx - ax) + ulps * Math.ulp(ax) * (random.nextBoolean() ? 1 : -1);
            double py = ay + t * (by - ay);
            boolean actual = RoundingCell.intersects(ax, ay, bx, by, px, py);
            assertEquals(oracle(ax, ay, bx, by, px, py), actual);
        }
    }

    // an independent oracle: the segment meets the cell iff a point of the segment rounds to p; the candidates are
    // the points where the segment crosses the lines x = const or y = const through the cell's bounds and through p,
    // and the endpoints; between consecutive candidates the rounding is constant
    private static boolean oracle(double ax, double ay, double bx, double by, double px, double py) {
        MathContext mc = MathContext.DECIMAL128;
        BigDecimal a0 = new BigDecimal(ax), a1 = new BigDecimal(ay);
        BigDecimal d0 = new BigDecimal(bx).subtract(a0), d1 = new BigDecimal(by).subtract(a1);
        List<BigDecimal> ts = new ArrayList<>();
        ts.add(BigDecimal.ZERO);
        ts.add(BigDecimal.ONE);
        BigDecimal[] cx = RoundingCell.cell(px), cy = RoundingCell.cell(py);
        for (BigDecimal v : new BigDecimal[]{cx[0], cx[1], new BigDecimal(px)}) {
            if (d0.signum() != 0) ts.add(v.subtract(a0).divide(d0, new MathContext(200)));
        }
        for (BigDecimal v : new BigDecimal[]{cy[0], cy[1], new BigDecimal(py)}) {
            if (d1.signum() != 0) ts.add(v.subtract(a1).divide(d1, new MathContext(200)));
        }
        ts.removeIf(t -> t.signum() < 0 || t.compareTo(BigDecimal.ONE) > 0);
        ts.sort(BigDecimal::compareTo);
        // test the candidates and the midpoints between them (well inside the pieces)
        for (int i = 0; i < ts.size(); i++) {
            if (roundsTo(a0, a1, d0, d1, ts.get(i), px, py)) return true;
            if (i + 1 < ts.size()) {
                BigDecimal mid = ts.get(i).add(ts.get(i + 1)).divide(BigDecimal.valueOf(2), mc);
                if (roundsTo(a0, a1, d0, d1, mid, px, py)) return true;
            }
        }
        return false;
    }

    private static boolean roundsTo(BigDecimal a0, BigDecimal a1, BigDecimal d0, BigDecimal d1, BigDecimal t,
                                    double px, double py) {
        return a0.add(d0.multiply(t)).doubleValue() == px && a1.add(d1.multiply(t)).doubleValue() == py;
    }
}
