package org.geolatte.geom.cga.exact;

import java.math.BigDecimal;

/**
 * Exact predicates on <em>rounding cells</em>, the pixels of snap rounding to the double grid.
 *
 * <p>The rounding cell of a point (x, y) with double coordinates is the set of points (of the real plane) whose
 * coordinates both round to x and y (round to nearest, ties to even). It is a product of two intervals: in each
 * axis, from the midpoint with the previous double to the midpoint with the next one. Under ties to even, the
 * interval of an even double (last significand bit 0) contains both midpoints and is closed; that of an odd double
 * is open. So the cells tile the plane, and the correctly rounded value of a point is the double point whose cell
 * contains it. Around a power of 2 the gaps to the two neighbours differ, so the double is not the centre of its
 * cell, but it always lies inside it. The only double point in a cell is the cell's own point.</p>
 *
 * <p>The predicates are exact: they are decided with floating point filters with rigorous error bounds where
 * possible, and otherwise with {@link BigDecimal} arithmetic.</p>
 */
public final class RoundingCell {

    private static final double U = 0x1p-53;
    // error bound of a floating point orientation or dot product of differences, relative to the sum of the
    // magnitudes of its two products: 3 u plus higher order terms, with a margin
    private static final double PRODUCT_ERROR = 8 * U;
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal OVERFLOW = new BigDecimal(Double.MAX_VALUE).add(new BigDecimal(Math.ulp(Double.MAX_VALUE)));

    private RoundingCell() {
    }

    /**
     * Returns whether the closed segment from a to b has a point in the rounding cell of p.
     *
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static boolean intersects(double ax, double ay, double bx, double by, double px, double py) {
        checkFinite(ax, ay, bx, by, px, py);
        if ((ax == px && ay == py) || (bx == px && by == py)) return true;
        // the cell lies strictly between the neighbouring doubles
        if (Math.max(ax, bx) <= Math.nextDown(px) || Math.min(ax, bx) >= Math.nextUp(px)
                || Math.max(ay, by) <= Math.nextDown(py) || Math.min(ay, by) >= Math.nextUp(py)) {
            return false;
        }
        // the line through a and b misses the cell if p is too far from it
        double dx = bx - ax, dy = by - ay;
        double t1 = dx * (py - ay), t2 = dy * (px - ax);
        double d = t1 - t2;
        double error = PRODUCT_ERROR * (Math.abs(t1) + Math.abs(t2));
        double reach = (Math.abs(dx) * gap(py) + Math.abs(dy) * gap(px)) * (1 + PRODUCT_ERROR);
        if (Math.abs(d) - error > reach) return false;
        return clip(ax, ay, bx, by, px, py) != null;
    }

    /**
     * Compares the positions along the segment from a to b at which it enters the rounding cells of p and q. Both
     * cells must meet the segment, and must differ.
     *
     * @return a negative number if the segment enters the cell of p first, a positive number if it enters the cell
     * of q first
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, if a cell does not meet the segment, or if
     *                                  p and q are equal
     */
    public static int compareAlong(double ax, double ay, double bx, double by,
                                   double px, double py, double qx, double qy) {
        checkFinite(ax, ay, bx, by, px, py);
        checkFinite(qx, qy, 0, 0, 0, 0);
        if (px == qx && py == qy) throw new IllegalArgumentException("The cells must differ");
        double dx = bx - ax, dy = by - ay;
        // the projections of p and q on the direction, and how far a cell can extend along it
        double sp = dx * (px - ax) + dy * (py - ay), sq = dx * (qx - ax) + dy * (qy - ay);
        double ep = PRODUCT_ERROR * (Math.abs(dx * (px - ax)) + Math.abs(dy * (py - ay)))
                + (Math.abs(dx) * gap(px) + Math.abs(dy) * gap(py)) * (1 + PRODUCT_ERROR);
        double eq = PRODUCT_ERROR * (Math.abs(dx * (qx - ax)) + Math.abs(dy * (qy - ay)))
                + (Math.abs(dx) * gap(qx) + Math.abs(dy) * gap(qy)) * (1 + PRODUCT_ERROR);
        if (Double.isFinite(sp) && Double.isFinite(sq) && Double.isFinite(ep + eq)) {
            if (sp + ep < sq - eq) return -1;
            if (sq + eq < sp - ep) return 1;
        }
        Clip cp = clip(ax, ay, bx, by, px, py), cq = clip(ax, ay, bx, by, qx, qy);
        if (cp == null || cq == null) throw new IllegalArgumentException("A cell does not meet the segment");
        return Clip.compareEntries(cp, cq);
    }

    /**
     * @return the larger of the gaps between v and its neighbouring doubles: the cell of v extends less than this
     * on both sides
     */
    private static double gap(double v) {
        double up = Math.nextUp(v) - v, down = v - Math.nextDown(v);
        // at the largest doubles the gap to the (infinite) next is that of the previous one, doubled
        if (Double.isInfinite(up)) up = 2 * down;
        if (Double.isInfinite(down)) down = 2 * up;
        return Math.max(up, down);
    }

    private static void checkFinite(double a, double b, double c, double d, double e, double f) {
        if (!Double.isFinite(a) || !Double.isFinite(b) || !Double.isFinite(c) || !Double.isFinite(d)
                || !Double.isFinite(e) || !Double.isFinite(f)) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // exact clipping
    // ---------------------------------------------------------------------------------------------------------

    /**
     * The interval of v's cell: [lower, upper], closed if v is even, open if it is odd
     */
    static BigDecimal[] cell(double v) {
        v = v + 0.0;    // -0.0 is 0.0
        BigDecimal value = new BigDecimal(v);
        BigDecimal next = v == Double.MAX_VALUE ? OVERFLOW : new BigDecimal(Math.nextUp(v));
        BigDecimal previous = v == -Double.MAX_VALUE ? OVERFLOW.negate() : new BigDecimal(Math.nextDown(v));
        return new BigDecimal[]{value.add(previous).divide(TWO), value.add(next).divide(TWO)};
    }

    static boolean isClosed(double v) {
        return (Double.doubleToRawLongBits(v + 0.0) & 1) == 0;
    }

    private static Clip.Interval interval(double v) {
        BigDecimal[] cell = cell(v);
        boolean closed = isClosed(v);
        return new Clip.Interval(cell[0], closed, cell[1], closed);
    }

    // the part of the closed segment from a to b in the cell of p, or null if it is empty
    private static Clip clip(double ax, double ay, double bx, double by, double px, double py) {
        return Clip.of(ax, ay, bx, by, interval(px), interval(py));
    }
}
