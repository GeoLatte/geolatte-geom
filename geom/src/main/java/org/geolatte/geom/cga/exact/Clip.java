package org.geolatte.geom.cga.exact;

import java.math.BigDecimal;

/**
 * The part of a segment a + t (b - a), 0 &lt;= t &lt;= 1, in a cell (a product of two intervals, each side closed or
 * open), computed exactly: Liang–Barsky clipping on rationals. Used by {@link RoundingCell} and {@link GridCell}.
 *
 * <p>The part is the parameter interval from {@code lowerNum / lowerDen} to {@code upperNum / upperDen} (with
 * positive denominators), each end included or not.</p>
 */
final class Clip {

    /**
     * An interval of one axis of a cell, from lower to upper, each end closed or open
     */
    static final class Interval {
        final BigDecimal lower, upper;
        final boolean lowerClosed, upperClosed;

        Interval(BigDecimal lower, boolean lowerClosed, BigDecimal upper, boolean upperClosed) {
            this.lower = lower;
            this.lowerClosed = lowerClosed;
            this.upper = upper;
            this.upperClosed = upperClosed;
        }

        boolean contains(BigDecimal v) {
            int lo = v.compareTo(lower), hi = v.compareTo(upper);
            return (lowerClosed ? lo >= 0 : lo > 0) && (upperClosed ? hi <= 0 : hi < 0);
        }
    }

    BigDecimal lowerNum = BigDecimal.ZERO, lowerDen = BigDecimal.ONE;
    boolean lowerIncluded = true;
    BigDecimal upperNum = BigDecimal.ONE, upperDen = BigDecimal.ONE;
    boolean upperIncluded = true;

    private Clip() {
    }

    /**
     * @return the part of the closed segment from a to b in the cell x &times; y, or null if it is empty
     */
    static Clip of(double ax, double ay, double bx, double by, Interval x, Interval y) {
        Clip clip = new Clip();
        if (!clip.axis(ax, bx, x) || !clip.axis(ay, by, y)) return null;
        int c = compare(clip.lowerNum, clip.lowerDen, clip.upperNum, clip.upperDen);
        if (c > 0 || (c == 0 && !(clip.lowerIncluded && clip.upperIncluded))) return null;
        return clip;
    }

    /**
     * Compares the entries of the segment into two cells, given the parts of the segment in them, which must be
     * disjoint and not empty.
     *
     * @return a negative number if the segment enters the cell of p first, a positive number otherwise
     */
    static int compareEntries(Clip p, Clip q) {
        // the parts are disjoint, so their entries differ, or are equal with one of them included (a part of a
        // single point) and the other not
        int c = compare(p.lowerNum, p.lowerDen, q.lowerNum, q.lowerDen);
        if (c != 0) return c;
        return p.lowerIncluded ? -1 : 1;
    }

    // the sign of a/b - c/d, for positive b and d
    static int compare(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
        return a.multiply(d).compareTo(c.multiply(b));
    }

    // restricts the interval to the parameters where the coordinate a + t (b - a) lies in the interval
    private boolean axis(double a, double b, Interval interval) {
        BigDecimal start = new BigDecimal(a), d = new BigDecimal(b).subtract(start);
        if (d.signum() == 0) return interval.contains(start);
        // t = (bound - a) / d
        BigDecimal n0 = interval.lower.subtract(start), n1 = interval.upper.subtract(start);
        boolean c0 = interval.lowerClosed, c1 = interval.upperClosed;
        if (d.signum() < 0) {
            BigDecimal n = n0.negate();
            n0 = n1.negate();
            n1 = n;
            boolean c = c0;
            c0 = c1;
            c1 = c;
            d = d.negate();
        }
        raiseLower(n0, d, c0);
        reduceUpper(n1, d, c1);
        return true;
    }

    private void raiseLower(BigDecimal num, BigDecimal den, boolean included) {
        int c = compare(num, den, lowerNum, lowerDen);
        if (c > 0) {
            lowerNum = num;
            lowerDen = den;
            lowerIncluded = included;
        } else if (c == 0) {
            lowerIncluded &= included;
        }
    }

    private void reduceUpper(BigDecimal num, BigDecimal den, boolean included) {
        int c = compare(num, den, upperNum, upperDen);
        if (c < 0) {
            upperNum = num;
            upperDen = den;
            upperIncluded = included;
        } else if (c == 0) {
            upperIncluded &= included;
        }
    }
}
