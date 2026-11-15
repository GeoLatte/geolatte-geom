package org.geolatte.geom.cga.exact;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Exact predicates on the cells of a fixed grid with a decimal cell size g, the pixels of snap rounding to that grid.
 * The counterpart of {@link RoundingCell}, for the double grid.
 *
 * <p>The grid points are the points (i g, j g) for integers i and j. The cell of a grid point is the product of the
 * half-open intervals [(i - 1/2) g, (i + 1/2) g) and [(j - 1/2) g, (j + 1/2) g): a coordinate v rounds to
 * floor(v / g + 1/2) g, so halves round up, as in JTS's {@code PrecisionModel}. The cells tile the plane.</p>
 *
 * <p>A grid point is in general not a double (for g = 0.001, say), so it is represented by its
 * <em>representative</em>: the double point nearest to it. That lies inside the cell as long as the cell size is
 * larger than the spacing of the doubles there; {@link #checkRange(double)} checks this for a range of coordinates.
 * The predicates take cells by their representatives, and are only defined where that holds.</p>
 *
 * <p>The predicates are exact: they are decided with floating point filters with rigorous error bounds where
 * possible, and otherwise with {@link BigDecimal} arithmetic.</p>
 */
public final class GridCell {

    private static final double U = 0x1p-53;
    // error bound of a floating point orientation or dot product of differences, relative to the sum of the
    // magnitudes of its two products, with a margin (as in RoundingCell)
    private static final double PRODUCT_ERROR = 8 * U;
    private static final double EXACT_INTEGERS = 0x1p53;
    private static final double[] POW10 = {
            1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9, 1e10, 1e11,
            1e12, 1e13, 1e14, 1e15, 1e16, 1e17, 1e18, 1e19, 1e20, 1e21, 1e22};
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private final BigDecimal size, halfSize, twoSize;
    private final double sizeD, half;
    // size = unscaled * 10^-scale; unscaled is 0 if it is not an exact double
    private final long unscaled;
    private final int scale;

    private GridCell(BigDecimal size) {
        this.size = size;
        this.halfSize = size.divide(TWO);
        this.twoSize = size.multiply(TWO);
        this.sizeD = size.doubleValue();
        this.half = sizeD / 2;
        long m = 0;
        try {
            m = size.unscaledValue().longValueExact();
            if (Math.abs(m) > EXACT_INTEGERS) m = 0;
        } catch (ArithmeticException e) {
            // too large: use BigDecimal
        }
        this.unscaled = m;
        this.scale = size.scale();
    }

    /**
     * @param size the cell size: positive, and a double when converted
     * @return the cells of the grid with the given cell size
     * @throws IllegalArgumentException if the size is not positive, or too large or too small for a double
     */
    public static GridCell of(BigDecimal size) {
        Objects.requireNonNull(size);
        if (size.signum() <= 0) throw new IllegalArgumentException("The cell size must be positive");
        double d = size.doubleValue();
        if (!(d > Double.MIN_NORMAL) || Double.isInfinite(d)) {
            throw new IllegalArgumentException("The cell size is out of range: " + size);
        }
        return new GridCell(size.stripTrailingZeros());
    }

    /**
     * @return the cell size
     */
    public BigDecimal size() {
        return size;
    }

    /**
     * Checks that the cells of all coordinates of magnitude up to {@code maxAbs} contain their representatives: the
     * cell size must be at least twice the spacing of the doubles there.
     *
     * @throws IllegalArgumentException if they do not
     */
    public void checkRange(double maxAbs) {
        if (!(2 * Math.ulp(Math.abs(maxAbs) + 2 * sizeD) <= sizeD)) {
            throw new IllegalArgumentException("The cell size " + size.toPlainString()
                    + " is too small for coordinates of magnitude " + maxAbs
                    + ": it must exceed the spacing of the doubles; use the double grid instead");
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // rounding
    // ---------------------------------------------------------------------------------------------------------

    /**
     * @return the representative of the cell that contains v: the double nearest to floor(v / g + 1/2) g
     * @throws IllegalArgumentException if v is NaN or infinite
     */
    public double round(double v) {
        if (!Double.isFinite(v)) throw new IllegalArgumentException("Coordinates must be finite");
        double q = v / sizeD;
        double f = q + 0.5;
        if (Math.abs(f) < 0x1p51) {
            double fl = Math.floor(f), frac = f - fl;
            double tolerance = 8 * U * (Math.abs(q) + 1);
            if (frac > tolerance && frac < 1 - tolerance) return representative((long) fl);
        }
        return representative(exactIndex(new BigDecimal(v)));
    }

    /**
     * Rounds the crossing point of the segments (a, b) and (c, d) to the grid.
     *
     * @return the representative of the cell that contains the exact crossing point
     * @throws IllegalArgumentException if the segments do not cross, or a coordinate is NaN or infinite
     */
    public double[] roundCrossing(double ax, double ay, double bx, double by,
                                  double cx, double cy, double dx, double dy) {
        // correctly rounded: within half an ulp of the exact crossing point
        double[] p = Constructions.crossingPoint(ax, ay, bx, by, cx, cy, dx, dy);
        double x = roundNear(p[0]), y = roundNear(p[1]);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            // the exact crossing point: a + t (b - a), t = tNum / den
            BigDecimal a0 = new BigDecimal(ax), a1 = new BigDecimal(ay);
            BigDecimal r0 = new BigDecimal(bx).subtract(a0), r1 = new BigDecimal(by).subtract(a1);
            BigDecimal c0 = new BigDecimal(cx), c1 = new BigDecimal(cy);
            BigDecimal s0 = new BigDecimal(dx).subtract(c0), s1 = new BigDecimal(dy).subtract(c1);
            BigDecimal den = r0.multiply(s1).subtract(r1.multiply(s0));
            BigDecimal tNum = c0.subtract(a0).multiply(s1).subtract(c1.subtract(a1).multiply(s0));
            if (Double.isNaN(x)) x = representative(exactIndex(a0.multiply(den).add(r0.multiply(tNum)), den));
            if (Double.isNaN(y)) y = representative(exactIndex(a1.multiply(den).add(r1.multiply(tNum)), den));
        }
        return new double[]{x, y};
    }

    // the representative for a value within half an ulp of v, or NaN if that is not decided by v
    private double roundNear(double v) {
        double q = v / sizeD;
        double f = q + 0.5;
        if (!(Math.abs(f) < 0x1p51)) return Double.NaN;
        double fl = Math.floor(f), frac = f - fl;
        double tolerance = 8 * U * (Math.abs(q) + 1) + Math.ulp(v) / sizeD;
        return frac > tolerance && frac < 1 - tolerance ? representative((long) fl) : Double.NaN;
    }

    // floor(v / g + 1/2), exactly
    private BigDecimal exactIndex(BigDecimal v) {
        return v.multiply(TWO).add(size).divide(twoSize, 0, RoundingMode.FLOOR);
    }

    // floor(num / (den g) + 1/2), exactly, for den != 0
    private BigDecimal exactIndex(BigDecimal num, BigDecimal den) {
        return num.multiply(TWO).add(size.multiply(den)).divide(twoSize.multiply(den), 0, RoundingMode.FLOOR);
    }

    // the double nearest k g
    private double representative(long k) {
        if (unscaled != 0 && Math.abs(k) < EXACT_INTEGERS) {
            double product = (double) k * unscaled;
            // the product is exact if it is less than 2^53 (k and unscaled are exact integers)
            if (Math.abs(product) < EXACT_INTEGERS) {
                if (scale >= 0 && scale < POW10.length) return product / POW10[scale] + 0.0;
                if (scale < 0 && -scale < POW10.length && Math.abs(product * POW10[-scale]) < EXACT_INTEGERS) {
                    return product * POW10[-scale] + 0.0;
                }
            }
        }
        return representative(BigDecimal.valueOf(k));
    }

    private double representative(BigDecimal k) {
        // BigDecimal.doubleValue is correctly rounded
        return k.multiply(size).doubleValue() + 0.0;
    }

    // the index of the cell of a representative
    private BigDecimal index(double p) {
        double f = p / sizeD + 0.5;
        if (Math.abs(f) < 0x1p51) {
            // f is close to k + 1/2
            double fl = Math.floor(f), frac = f - fl;
            if (frac > 0.25 && frac < 0.75) return BigDecimal.valueOf((long) fl);
        }
        return exactIndex(new BigDecimal(p));
    }

    private Clip.Interval interval(double p) {
        BigDecimal centre = index(p).multiply(size);
        return new Clip.Interval(centre.subtract(halfSize), true, centre.add(halfSize), false);
    }

    // ---------------------------------------------------------------------------------------------------------
    // segments and cells
    // ---------------------------------------------------------------------------------------------------------

    // a bound on the distance between a representative and the grid point of its cell, plus rounding errors
    private double margin(double p) {
        return 2 * Math.ulp(Math.abs(p) + half);
    }

    /**
     * Returns whether the closed segment from a to b has a point in the cell with representative p.
     *
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public boolean intersects(double ax, double ay, double bx, double by, double px, double py) {
        checkFinite(ax, ay, bx, by, px, py);
        if ((ax == px && ay == py) || (bx == px && by == py)) return true;
        // the cell lies within p +/- outer in each axis
        double mx = margin(px), my = margin(py);
        double outerX = half + 2 * mx, outerY = half + 2 * my;
        double innerX = half - 2 * mx, innerY = half - 2 * my;
        double minX = Math.min(ax, bx), maxX = Math.max(ax, bx), minY = Math.min(ay, by), maxY = Math.max(ay, by);
        // separated along the X or the Y axis
        if (maxX < px - outerX || minX > px + outerX || maxY < py - outerY || minY > py + outerY) return false;
        // separated along the normal of the segment
        double dx = bx - ax, dy = by - ay;
        double t1 = dx * (py - ay), t2 = dy * (px - ax);
        double d = t1 - t2;
        double error = PRODUCT_ERROR * (Math.abs(t1) + Math.abs(t2));
        double outerReach = (Math.abs(dx) * outerY + Math.abs(dy) * outerX) * (1 + PRODUCT_ERROR);
        if (Math.abs(d) - error > outerReach) return false;
        // no separating axis between the segment and a box inside the cell (separating axis theorem): they meet.
        // The box is p +/- (half - 3/4 margin): the axis tests below imply overlap with it despite rounding, and
        // innerX, innerY are smaller, so the normal test does too
        double boxX = half - mx, boxY = half - my;
        if (innerX > 0 && innerY > 0
                && maxX >= px - boxX && minX <= px + boxX && maxY >= py - boxY && minY <= py + boxY) {
            double innerReach = (Math.abs(dx) * innerY + Math.abs(dy) * innerX) * (1 - PRODUCT_ERROR);
            if (Math.abs(d) + error <= innerReach) return true;
        }
        return clip(ax, ay, bx, by, px, py) != null;
    }

    /**
     * Compares the positions along the segment from a to b at which it enters the cells with representatives p
     * and q. Both cells must meet the segment, and must differ.
     *
     * @return a negative number if the segment enters the cell of p first, a positive number if it enters the cell
     * of q first
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, if a cell does not meet the segment, or if
     *                                  p and q are equal
     */
    public int compareAlong(double ax, double ay, double bx, double by,
                            double px, double py, double qx, double qy) {
        checkFinite(ax, ay, bx, by, px, py);
        checkFinite(qx, qy, 0, 0, 0, 0);
        if (px == qx && py == qy) throw new IllegalArgumentException("The cells must differ");
        double dx = bx - ax, dy = by - ay;
        // the projections of p and q on the direction, and how far a cell can extend along it
        double sp = dx * (px - ax) + dy * (py - ay), sq = dx * (qx - ax) + dy * (qy - ay);
        double ep = PRODUCT_ERROR * (Math.abs(dx * (px - ax)) + Math.abs(dy * (py - ay)))
                + (Math.abs(dx) * (half + 2 * margin(px)) + Math.abs(dy) * (half + 2 * margin(py)))
                * (1 + PRODUCT_ERROR);
        double eq = PRODUCT_ERROR * (Math.abs(dx * (qx - ax)) + Math.abs(dy * (qy - ay)))
                + (Math.abs(dx) * (half + 2 * margin(qx)) + Math.abs(dy) * (half + 2 * margin(qy)))
                * (1 + PRODUCT_ERROR);
        if (Double.isFinite(sp) && Double.isFinite(sq) && Double.isFinite(ep + eq)) {
            if (sp + ep < sq - eq) return -1;
            if (sq + eq < sp - ep) return 1;
        }
        Clip cp = clip(ax, ay, bx, by, px, py), cq = clip(ax, ay, bx, by, qx, qy);
        if (cp == null || cq == null) throw new IllegalArgumentException("A cell does not meet the segment");
        return Clip.compareEntries(cp, cq);
    }

    // the part of the closed segment from a to b in the cell of p, or null if it is empty
    private Clip clip(double ax, double ay, double bx, double by, double px, double py) {
        return Clip.of(ax, ay, bx, by, interval(px), interval(py));
    }

    private static void checkFinite(double a, double b, double c, double d, double e, double f) {
        if (!Double.isFinite(a) || !Double.isFinite(b) || !Double.isFinite(c) || !Double.isFinite(d)
                || !Double.isFinite(e) || !Double.isFinite(f)) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GridCell && ((GridCell) o).size.compareTo(size) == 0;
    }

    @Override
    public int hashCode() {
        return size.hashCode();
    }

    @Override
    public String toString() {
        return "GridCell[" + size.toPlainString() + "]";
    }
}
