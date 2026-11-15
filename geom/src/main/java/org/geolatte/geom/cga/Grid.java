package org.geolatte.geom.cga;

import org.geolatte.geom.cga.exact.Constructions;
import org.geolatte.geom.cga.exact.GridCell;
import org.geolatte.geom.cga.exact.RoundingCell;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The grid to which the results of the overlay operations ({@link Overlay}) are snap rounded.
 *
 * <ul>
 *     <li>{@link #DOUBLE}: the double grid. The vertices of the result are vertices of the operands, or the doubles
 *     nearest to the crossing points of their segments. The cells are the rounding cells of the doubles
 *     ({@link RoundingCell}).</li>
 *     <li>{@link #fixed(BigDecimal)}: a fixed grid with a decimal cell size g, such as 0.001 or 10. Every vertex of the
 *     result is a grid point (i g, j g), represented by the double nearest to it. A coordinate v rounds to
 *     floor(v / g + 1/2) g, so halves round up, as in JTS's {@code PrecisionModel} ({@link GridCell}). The vertices
 *     of the operands move to their grid points.</li>
 * </ul>
 *
 * <p>A fixed grid needs cells that are larger than the spacing of the doubles at the coordinates of the operands;
 * the operations throw an {@code IllegalArgumentException} if they are not.</p>
 */
public abstract class Grid {

    /**
     * The double grid: snap rounding to the nearest doubles
     */
    public static final Grid DOUBLE = new DoubleGrid();

    Grid() {
    }

    /**
     * @param size the cell size, positive
     * @return the fixed grid with the given cell size
     * @throws IllegalArgumentException if the size is not positive, or out of the range of the doubles
     */
    public static Grid fixed(BigDecimal size) {
        return new FixedGrid(GridCell.of(size));
    }

    /**
     * @param size the cell size as a decimal number, such as "0.001"
     * @return the fixed grid with the given cell size
     * @throws NumberFormatException    if the size is not a decimal number
     * @throws IllegalArgumentException if the size is not positive, or out of the range of the doubles
     */
    public static Grid fixed(String size) {
        return fixed(new BigDecimal(size));
    }

    /**
     * @param size the cell size; it is read as the shortest decimal that rounds to it ({@link BigDecimal#valueOf}),
     *             so 0.001 is one thousandth
     * @return the fixed grid with the given cell size
     * @throws IllegalArgumentException if the size is not positive and finite
     */
    public static Grid fixed(double size) {
        if (!Double.isFinite(size)) throw new IllegalArgumentException("The cell size must be finite");
        return fixed(BigDecimal.valueOf(size));
    }

    /**
     * @param scale the number of cells per unit, positive, as in JTS's {@code PrecisionModel}: a scale of 1000 is a
     *              cell size of 0.001
     * @return the fixed grid with cell size 1 / scale
     * @throws IllegalArgumentException if the scale is not positive, or 1 / scale is not a finite decimal (such as
     *                                  for a scale of 3)
     */
    public static Grid ofScale(BigDecimal scale) {
        Objects.requireNonNull(scale);
        if (scale.signum() <= 0) throw new IllegalArgumentException("The scale must be positive");
        BigDecimal size;
        try {
            size = BigDecimal.ONE.divide(scale);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("The cell size 1 / " + scale.toPlainString()
                    + " is not a finite decimal; use Grid.fixed with a decimal cell size");
        }
        return fixed(size);
    }

    /**
     * @param scale the number of cells per unit; it is read as the shortest decimal that rounds to it
     * @return the fixed grid with cell size 1 / scale
     * @throws IllegalArgumentException if the scale is not positive and finite, or 1 / scale is not a finite decimal
     * @see #ofScale(BigDecimal)
     */
    public static Grid ofScale(double scale) {
        if (!Double.isFinite(scale)) throw new IllegalArgumentException("The scale must be finite");
        return ofScale(BigDecimal.valueOf(scale));
    }

    /**
     * @return whether this is a fixed grid
     */
    public abstract boolean isFixed();

    /**
     * @return the cell size of a fixed grid; 0 for the double grid, whose cells vary in size
     */
    public abstract BigDecimal cellSize();

    /**
     * @return the coordinate of the grid point whose cell contains v (for the double grid: v itself)
     * @throws IllegalArgumentException if v is NaN or infinite
     */
    public abstract double round(double v);

    // the grid point of the cell that contains the crossing point of the segments (a, b) and (c, d)
    abstract double[] roundCrossing(double ax, double ay, double bx, double by,
                                    double cx, double cy, double dx, double dy);

    // whether the closed segment from a to b meets the cell of grid point p
    abstract boolean intersects(double ax, double ay, double bx, double by, double px, double py);

    // the order in which the segment from a to b enters the cells of the grid points p and q
    abstract int compareAlong(double ax, double ay, double bx, double by,
                              double px, double py, double qx, double qy);

    // a half-width of a box around grid point p that contains its cell, also after rounding
    abstract double boxHalfWidth(double p);

    // checks that the cells of coordinates up to maxAbs contain their grid points
    abstract void checkRange(double maxAbs);

    private static final class DoubleGrid extends Grid {

        @Override
        public boolean isFixed() {
            return false;
        }

        @Override
        public BigDecimal cellSize() {
            return BigDecimal.ZERO;
        }

        @Override
        public double round(double v) {
            if (!Double.isFinite(v)) throw new IllegalArgumentException("Coordinates must be finite");
            return v + 0.0;
        }

        @Override
        double[] roundCrossing(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
            return Constructions.crossingPoint(ax, ay, bx, by, cx, cy, dx, dy);
        }

        @Override
        boolean intersects(double ax, double ay, double bx, double by, double px, double py) {
            return RoundingCell.intersects(ax, ay, bx, by, px, py);
        }

        @Override
        int compareAlong(double ax, double ay, double bx, double by, double px, double py, double qx, double qy) {
            return RoundingCell.compareAlong(ax, ay, bx, by, px, py, qx, qy);
        }

        @Override
        double boxHalfWidth(double p) {
            // the cell extends less than one gap to the neighbouring doubles
            return 4 * Math.ulp(p);
        }

        @Override
        void checkRange(double maxAbs) {
        }

        @Override
        public String toString() {
            return "Grid[double]";
        }
    }

    private static final class FixedGrid extends Grid {
        private final GridCell cell;
        private final double half;

        FixedGrid(GridCell cell) {
            this.cell = cell;
            this.half = cell.size().doubleValue() / 2;
        }

        @Override
        public boolean isFixed() {
            return true;
        }

        @Override
        public BigDecimal cellSize() {
            return cell.size();
        }

        @Override
        public double round(double v) {
            return cell.round(v);
        }

        @Override
        double[] roundCrossing(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
            return cell.roundCrossing(ax, ay, bx, by, cx, cy, dx, dy);
        }

        @Override
        boolean intersects(double ax, double ay, double bx, double by, double px, double py) {
            return cell.intersects(ax, ay, bx, by, px, py);
        }

        @Override
        int compareAlong(double ax, double ay, double bx, double by, double px, double py, double qx, double qy) {
            return cell.compareAlong(ax, ay, bx, by, px, py, qx, qy);
        }

        @Override
        double boxHalfWidth(double p) {
            return half + 8 * Math.ulp(Math.abs(p) + half);
        }

        @Override
        void checkRange(double maxAbs) {
            cell.checkRange(maxAbs);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof FixedGrid && ((FixedGrid) o).cell.equals(cell);
        }

        @Override
        public int hashCode() {
            return cell.hashCode();
        }

        @Override
        public String toString() {
            return "Grid[" + cell.size().toPlainString() + "]";
        }
    }
}
