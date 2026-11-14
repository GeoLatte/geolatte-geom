package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.LengthSum;

/**
 * The length of a geometry, in the plane of the first two coordinates.
 *
 * <p>The length of a line string is the sum of the lengths of its segments. The length of a polygon is its
 * perimeter: the sum of the lengths of its rings ({@code ST_Perimeter}). The length of a collection is the sum of
 * the lengths of its elements; points have no length. This is the definition of JTS {@code Geometry.getLength()}.</p>
 */
public final class Length {

    private Length() {
    }

    /**
     * Computes the length with {@link LengthSum}: the result is correctly rounded (the double nearest to the exact
     * length, ties to even), except when the exact length is irrational and lies within about 10^-58 (relatively)
     * of the midpoint between two doubles; it is then one of the two doubles next to the exact length.
     *
     * <p>The lengths are first summed in double-word arithmetic, which decides the result unless the exact length
     * lies within about 2^-100 of a midpoint (or is one, as for a tie between exact lengths). The lengths are then
     * computed again with {@code BigDecimal} arithmetic, exactly when they are rational ({@link LengthSum#exact()}),
     * which is much slower.</p>
     *
     * @return the length of the geometry
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static double of(Geometry<?> geometry) {
        LengthSum sum = new LengthSum();
        add(geometry, sum);
        double result = sum.correctlyRounded();
        if (!Double.isNaN(result)) return result;
        LengthSum exact = LengthSum.exact();
        add(geometry, exact);
        return exact.value();
    }

    /**
     * Computes the length in plain floating point arithmetic: the sum of {@code sqrt(dx² + dy²)} over the segments.
     * This is faster than {@link #of(Geometry)}, but the error grows with the number of segments, and a segment
     * length overflows for coordinate differences above about 1e154. A NaN coordinate gives a NaN length.
     *
     * @return the length of the geometry, approximately
     */
    public static double fast(Geometry<?> geometry) {
        if (geometry.isEmpty()) return 0;
        if (geometry instanceof LineString) return fast(geometry.getPositions());
        if (geometry instanceof Polygon) {
            Polygon<?> polygon = (Polygon<?>) geometry;
            double length = fast(polygon.getExteriorRing().getPositions());
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                length += fast(polygon.getInteriorRingN(i).getPositions());
            }
            return length;
        }
        double length = 0;
        if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) length += fast(collection.getGeometryN(i));
        }
        return length;
    }

    private static double fast(PositionSequence<?> positions) {
        int n = positions.size();
        if (n < 2) return 0;
        double[] c = new double[Math.max(2, positions.getCoordinateDimension())];
        positions.getCoordinates(0, c);
        double x0 = c[0], y0 = c[1];
        double length = 0;
        for (int i = 1; i < n; i++) {
            positions.getCoordinates(i, c);
            double dx = c[0] - x0, dy = c[1] - y0;
            length += Math.sqrt(dx * dx + dy * dy);
            x0 = c[0];
            y0 = c[1];
        }
        return length;
    }

    private static void add(Geometry<?> geometry, LengthSum sum) {
        if (geometry.isEmpty()) return;
        if (geometry instanceof LineString) {
            add(geometry.getPositions(), sum);
        } else if (geometry instanceof Polygon) {
            Polygon<?> polygon = (Polygon<?>) geometry;
            add(polygon.getExteriorRing().getPositions(), sum);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) add(polygon.getInteriorRingN(i).getPositions(), sum);
        } else if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) add(collection.getGeometryN(i), sum);
        }
    }

    private static void add(PositionSequence<?> positions, LengthSum sum) {
        int n = positions.size();
        double[] c = new double[Math.max(2, positions.getCoordinateDimension())];
        positions.getCoordinates(0, c);
        double x0 = c[0], y0 = c[1];
        if (n == 1) sum.add(x0, y0, x0, y0);   // checks the coordinates
        for (int i = 1; i < n; i++) {
            positions.getCoordinates(i, c);
            sum.add(x0, y0, c[0], c[1]);
            x0 = c[0];
            y0 = c[1];
        }
    }
}
