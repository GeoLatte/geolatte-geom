package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class GridCellTest {

    private static final String[] SIZES = {"0.001", "0.1", "0.5", "3", "10", "0.0009765625", "0.25", "7", "0.03"};
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    // ---------------------------------------------------------------------------------------------------------
    // an independent oracle, on exact rationals
    // ---------------------------------------------------------------------------------------------------------

    // the index of the cell that contains num / den (den > 0): the largest k with (k - 1/2) g <= num / den
    private static BigInteger index(BigDecimal g, BigDecimal num, BigDecimal den) {
        // (2 num + g den) / (2 g den), floored
        BigDecimal n = num.multiply(TWO).add(g.multiply(den)), d = g.multiply(den).multiply(TWO);
        BigDecimal[] qr = n.divideAndRemainder(d);
        BigInteger k = qr[0].toBigIntegerExact();
        if (qr[1].signum() != 0 && (qr[1].signum() < 0) != (d.signum() < 0)) k = k.subtract(BigInteger.ONE);
        return k;
    }

    private static double representative(BigDecimal g, BigInteger k) {
        return new BigDecimal(k).multiply(g).doubleValue() + 0.0;
    }

    private static double oracleRound(BigDecimal g, double v) {
        return representative(g, index(g, new BigDecimal(v), BigDecimal.ONE));
    }

    private static BigInteger cellOf(BigDecimal g, double p) {
        return index(g, new BigDecimal(p), BigDecimal.ONE);
    }

    // a rational parameter n / d, d > 0
    private static final class T {
        final BigDecimal n, d;

        T(BigDecimal n, BigDecimal d) {
            if (d.signum() < 0) {
                n = n.negate();
                d = d.negate();
            }
            this.n = n;
            this.d = d;
        }

        int compareTo(T o) {
            return n.multiply(o.d).compareTo(o.n.multiply(d));
        }

        T mid(T o) {
            return new T(n.multiply(o.d).add(o.n.multiply(d)), d.multiply(o.d).multiply(TWO));
        }
    }

    // whether a + t (b - a) lies in the cell with indices (i, j)
    private static boolean inCell(BigDecimal g, double ax, double ay, double bx, double by, T t, BigInteger i,
                                  BigInteger j) {
        BigDecimal a0 = new BigDecimal(ax), a1 = new BigDecimal(ay);
        BigDecimal x = a0.multiply(t.d).add(new BigDecimal(bx).subtract(a0).multiply(t.n));
        BigDecimal y = a1.multiply(t.d).add(new BigDecimal(by).subtract(a1).multiply(t.n));
        return index(g, x, t.d).equals(i) && index(g, y, t.d).equals(j);
    }

    // the parameters where the segment crosses the bounds of the given cells, and its ends, sorted, with the
    // midpoints between them: the cell membership is constant between consecutive candidates
    private static List<T> candidates(BigDecimal g, double ax, double ay, double bx, double by, BigInteger[]... cells) {
        List<T> ts = new ArrayList<>();
        T zero = new T(BigDecimal.ZERO, BigDecimal.ONE), one = new T(BigDecimal.ONE, BigDecimal.ONE);
        ts.add(zero);
        ts.add(one);
        BigDecimal half = g.divide(TWO);
        for (BigInteger[] c : cells) {
            for (int axis = 0; axis < 2; axis++) {
                BigDecimal a = new BigDecimal(axis == 0 ? ax : ay), b = new BigDecimal(axis == 0 ? bx : by);
                BigDecimal d = b.subtract(a);
                if (d.signum() == 0) continue;
                BigDecimal centre = new BigDecimal(c[axis]).multiply(g);
                for (BigDecimal bound : new BigDecimal[]{centre.subtract(half), centre.add(half)}) {
                    T t = new T(bound.subtract(a), d);
                    if (t.compareTo(zero) >= 0 && t.compareTo(one) <= 0) ts.add(t);
                }
            }
        }
        ts.sort(T::compareTo);
        List<T> all = new ArrayList<>();
        for (int i = 0; i < ts.size(); i++) {
            all.add(ts.get(i));
            if (i + 1 < ts.size() && ts.get(i).compareTo(ts.get(i + 1)) < 0) all.add(ts.get(i).mid(ts.get(i + 1)));
        }
        return all;
    }

    private static boolean oracleIntersects(BigDecimal g, double ax, double ay, double bx, double by,
                                            double px, double py) {
        BigInteger[] c = {cellOf(g, px), cellOf(g, py)};
        for (T t : candidates(g, ax, ay, bx, by, c)) {
            if (inCell(g, ax, ay, bx, by, t, c[0], c[1])) return true;
        }
        return false;
    }

    // -1 if the segment enters the cell of p first, 1 if that of q, 0 if it meets neither
    private static int oracleCompare(BigDecimal g, double ax, double ay, double bx, double by,
                                     double px, double py, double qx, double qy) {
        BigInteger[] cp = {cellOf(g, px), cellOf(g, py)}, cq = {cellOf(g, qx), cellOf(g, qy)};
        for (T t : candidates(g, ax, ay, bx, by, cp, cq)) {
            if (inCell(g, ax, ay, bx, by, t, cp[0], cp[1])) return -1;
            if (inCell(g, ax, ay, bx, by, t, cq[0], cq[1])) return 1;
        }
        return 0;
    }

    // ---------------------------------------------------------------------------------------------------------
    // tests
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void halvesRoundUp() {
        GridCell half = GridCell.of(new BigDecimal("0.5"));
        assertEquals(0.5, half.round(0.25), 0);
        assertEquals(0.0, half.round(Math.nextDown(0.25)), 0);
        assertEquals(0.0, half.round(-0.25), 0);
        assertEquals(-0.5, half.round(Math.nextDown(-0.25)), 0);
        GridCell ten = GridCell.of(BigDecimal.TEN);
        assertEquals(20, ten.round(15), 0);
        assertEquals(10, ten.round(14.999999), 0);
        assertEquals(-10, ten.round(-15), 0);
        assertEquals(0, ten.round(-4.9), 0);
        assertEquals(0.0, Double.doubleToRawLongBits(ten.round(-4.9)), 0);   // no -0.0
        GridCell three = GridCell.of(BigDecimal.valueOf(3));
        assertEquals(3, three.round(1.5), 0);
        assertEquals(0, three.round(Math.nextDown(1.5)), 0);
    }

    @Test
    public void decimalGridPointsAreRepresentedByTheNearestDouble() {
        GridCell mm = GridCell.of(new BigDecimal("0.001"));
        assertEquals(0.001, mm.round(0.0012), 0);
        assertEquals(0.003, mm.round(0.0029), 0);
        assertEquals(152000.123, mm.round(152000.12345), 0);
        assertEquals(-0.007, mm.round(-0.0071), 0);
        // 0.0015 is not a double: the double nearest to it is above it, so it rounds up; 0.0045 is below it
        assertTrue(new BigDecimal(0.0015).compareTo(new BigDecimal("0.0015")) > 0);
        assertEquals(0.002, mm.round(0.0015), 0);
        assertTrue(new BigDecimal(0.0045).compareTo(new BigDecimal("0.0045")) < 0);
        assertEquals(0.004, mm.round(0.0045), 0);
        assertEquals(new BigDecimal("0.001"), mm.size());
        assertEquals(GridCell.of(new BigDecimal("0.0010")), mm);
    }

    @Test
    public void roundAgainstOracle() {
        Random random = new Random(1);
        for (String s : SIZES) {
            BigDecimal g = new BigDecimal(s);
            GridCell cell = GridCell.of(g);
            double gd = g.doubleValue();
            for (int i = 0; i < 20000; i++) {
                double v;
                switch (i % 4) {
                    case 0:
                        v = (random.nextDouble() - 0.5) * 1000 * gd;
                        break;
                    case 1: {
                        // near a half: the double nearest to (k + 1/2) g, and its neighbours
                        long k = random.nextInt(2_000_000) - 1_000_000;
                        v = BigDecimal.valueOf(k).add(new BigDecimal("0.5")).multiply(g).doubleValue();
                        int step = random.nextInt(3) - 1;
                        if (step < 0) v = Math.nextDown(v);
                        if (step > 0) v = Math.nextUp(v);
                        break;
                    }
                    case 2:
                        v = (random.nextDouble() - 0.5) * 1e6 * Math.max(gd, 1);
                        break;
                    default:
                        v = (random.nextDouble() - 0.5) * 1e12 * gd;
                }
                double r = cell.round(v);
                assertEquals(s + " " + v, oracleRound(g, v), r, 0);
                // the representative lies in its own cell
                assertEquals(s + " " + v, r, cell.round(r), 0);
            }
        }
    }

    @Test
    public void roundCrossingAgainstOracle() {
        Random random = new Random(2);
        for (String s : SIZES) {
            BigDecimal g = new BigDecimal(s);
            GridCell cell = GridCell.of(g);
            double gd = g.doubleValue();
            for (int i = 0; i < 5000; i++) {
                double ox = 1000 * gd * random.nextInt(1000), oy = 1000 * gd * random.nextInt(1000);
                double w = gd * (i % 2 == 0 ? 3 : 300);
                double ax = ox + random.nextDouble() * w, ay = oy + random.nextDouble() * w;
                double bx = ox + random.nextDouble() * w, by = oy + random.nextDouble() * w;
                double cx = ox + random.nextDouble() * w, cy = oy + random.nextDouble() * w;
                double dx = ox + random.nextDouble() * w, dy = oy + random.nextDouble() * w;
                if (Predicates.segmentIntersection(ax, ay, bx, by, cx, cy, dx, dy)
                        != org.geolatte.geom.SegmentIntersection.CROSSING) {
                    continue;
                }
                double[] p = cell.roundCrossing(ax, ay, bx, by, cx, cy, dx, dy);
                double[] expected = oracleCrossing(g, ax, ay, bx, by, cx, cy, dx, dy);
                assertArrayEquals(s, expected, p, 0);
            }
        }
    }

    @Test
    public void crossingExactlyOnAHalf() {
        GridCell half = GridCell.of(new BigDecimal("0.5"));
        // the diagonals of the square [0, 0.5]^2 cross at (0.25, 0.25), a corner of four cells: halves round up
        assertArrayEquals(new double[]{0.5, 0.5}, half.roundCrossing(0, 0, 0.5, 0.5, 0, 0.5, 0.5, 0), 0);
        GridCell three = GridCell.of(BigDecimal.valueOf(3));
        // the diagonals of [0, 3]^2 cross at (1.5, 1.5)
        assertArrayEquals(new double[]{3, 3}, three.roundCrossing(0, 0, 3, 3, 0, 3, 3, 0), 0);
        // shift one diagonal by an ulp: the crossing moves off the half
        double[] p = three.roundCrossing(0, 0, 3, 3, 0, Math.nextDown(3.0), 3, Math.nextDown(0.0));
        assertArrayEquals(oracleCrossing(BigDecimal.valueOf(3), 0, 0, 3, 3, 0, Math.nextDown(3.0), 3,
                Math.nextDown(0.0)), p, 0);
        assertArrayEquals(new double[]{0, 0}, p, 0);
    }

    private static double[] oracleCrossing(BigDecimal g, double ax, double ay, double bx, double by,
                                           double cx, double cy, double dx, double dy) {
        BigDecimal a0 = new BigDecimal(ax), a1 = new BigDecimal(ay);
        BigDecimal r0 = new BigDecimal(bx).subtract(a0), r1 = new BigDecimal(by).subtract(a1);
        BigDecimal c0 = new BigDecimal(cx), c1 = new BigDecimal(cy);
        BigDecimal s0 = new BigDecimal(dx).subtract(c0), s1 = new BigDecimal(dy).subtract(c1);
        BigDecimal den = r0.multiply(s1).subtract(r1.multiply(s0));
        BigDecimal tn = c0.subtract(a0).multiply(s1).subtract(c1.subtract(a1).multiply(s0));
        if (den.signum() < 0) {
            den = den.negate();
            tn = tn.negate();
        }
        BigDecimal x = a0.multiply(den).add(r0.multiply(tn)), y = a1.multiply(den).add(r1.multiply(tn));
        return new double[]{representative(g, index(g, x, den)), representative(g, index(g, y, den))};
    }

    @Test
    public void closedLowerAndOpenUpperSides() {
        GridCell unit = GridCell.of(BigDecimal.ONE);
        // the cell of (0, 0) is [-0.5, 0.5) x [-0.5, 0.5)
        assertTrue(unit.intersects(-0.5, -1, -0.5, 1, 0, 0));
        assertFalse(unit.intersects(0.5, -1, 0.5, 1, 0, 0));
        assertTrue(unit.intersects(-1, -0.5, 1, -0.5, 0, 0));
        assertFalse(unit.intersects(-1, 0.5, 1, 0.5, 0, 0));
        // through the corner (0.5, 0.5): it belongs to the cell of (1, 1) only
        assertFalse(unit.intersects(0, 1, 1, 0, 0, 0));
        assertTrue(unit.intersects(0, 1, 1, 0, 1, 1));
        assertTrue(unit.intersects(0, 1, 1, 0, 0, 1));
        assertTrue(unit.intersects(0, 1, 1, 0, 1, 0));
        // ending on the closed side, or on the open side
        assertTrue(unit.intersects(-2, 0, -0.5, 0, 0, 0));
        assertFalse(unit.intersects(2, 0, 0.5, 0, 0, 0));
        // entering the cell of (0, 0) at t = 0.25 (at x = -0.5, closed) before that of (1, 0) (at x = 0.5)
        assertTrue(unit.compareAlong(-1, 0, 1, 0, 0, 0, 1, 0) < 0);
        assertTrue(unit.compareAlong(1, 0, -1, 0, 0, 0, 1, 0) > 0);
        // along the line x = y through the corners: the cells of (0, 0) and (1, 1)
        assertTrue(unit.compareAlong(0, 0, 1, 1, 0, 0, 1, 1) < 0);
        assertTrue(unit.compareAlong(1, 1, 0, 0, 0, 0, 1, 1) > 0);
        // equal cells
        assertThrows(IllegalArgumentException.class, () -> unit.compareAlong(0, 0, 1, 1, 0, 0, 0, 0));
    }

    @Test
    public void intersectsAgainstOracle() {
        Random random = new Random(3);
        for (String s : SIZES) {
            BigDecimal g = new BigDecimal(s);
            GridCell cell = GridCell.of(g);
            double gd = g.doubleValue();
            double offset = s.equals("0.001") || s.equals("0.03") ? 152000 : 0;
            for (int i = 0; i < 6000; i++) {
                double ax = offset + random.nextDouble() * 6 * gd, ay = offset + random.nextDouble() * 6 * gd;
                double bx = offset + random.nextDouble() * 6 * gd, by = offset + random.nextDouble() * 6 * gd;
                if (i % 3 == 0) {
                    // through or along cell sides and corners
                    ax = cell.round(ax) + gd / 2;
                    by = cell.round(by) - gd / 2;
                }
                if (i % 5 == 0) bx = ax;
                double px = cell.round(offset + random.nextDouble() * 6 * gd);
                double py = cell.round(offset + random.nextDouble() * 6 * gd);
                assertEquals(s + " " + i, oracleIntersects(g, ax, ay, bx, by, px, py),
                        cell.intersects(ax, ay, bx, by, px, py));
            }
        }
    }

    @Test
    public void compareAlongAgainstOracle() {
        Random random = new Random(4);
        for (String s : SIZES) {
            BigDecimal g = new BigDecimal(s);
            GridCell cell = GridCell.of(g);
            double gd = g.doubleValue();
            int compared = 0;
            for (int i = 0; i < 20000 && compared < 3000; i++) {
                double ax = random.nextDouble() * 8 * gd, ay = random.nextDouble() * 8 * gd;
                double bx = random.nextDouble() * 8 * gd, by = random.nextDouble() * 8 * gd;
                if (i % 3 == 0) {
                    ax = cell.round(ax) + gd / 2;
                    ay = cell.round(ay) + gd / 2;
                }
                // cells near points of the segment
                double t = random.nextDouble(), u = random.nextDouble();
                double px = cell.round(ax + t * (bx - ax) + (random.nextDouble() - 0.5) * gd);
                double py = cell.round(ay + t * (by - ay) + (random.nextDouble() - 0.5) * gd);
                double qx = cell.round(ax + u * (bx - ax) + (random.nextDouble() - 0.5) * gd);
                double qy = cell.round(ay + u * (by - ay) + (random.nextDouble() - 0.5) * gd);
                if ((px == qx && py == qy) || !cell.intersects(ax, ay, bx, by, px, py)
                        || !cell.intersects(ax, ay, bx, by, qx, qy)) {
                    continue;
                }
                compared++;
                int expected = oracleCompare(g, ax, ay, bx, by, px, py, qx, qy);
                assertEquals(s + " " + i, expected, Integer.signum(cell.compareAlong(ax, ay, bx, by, px, py, qx, qy)));
            }
            assertTrue(s + " " + compared, compared > 500);
        }
    }

    @Test
    public void range() {
        GridCell mm = GridCell.of(new BigDecimal("0.001"));
        mm.checkRange(1e9);
        assertThrows(IllegalArgumentException.class, () -> mm.checkRange(1e13));
        assertThrows(IllegalArgumentException.class, () -> GridCell.of(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> GridCell.of(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> GridCell.of(new BigDecimal("1e-400")));
        assertThrows(IllegalArgumentException.class, () -> mm.round(Double.NaN));
    }
}
