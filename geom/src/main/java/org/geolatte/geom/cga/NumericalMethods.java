package org.geolatte.geom.cga;

import org.geolatte.geom.LinearRing;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.cga.exact.Predicates;

import java.util.stream.IntStream;

/**
 * Numerical methods for computational geometry.
 *
 * <p>The predicates ({@code orientation}, {@code collinear}, {@code orient2d}, {@code isCounterClockwise}) are
 * exact: their result is the result for the coordinates as given, never falsified by rounding errors. See
 * {@link Predicates}.</p>
 *
 * Created by Karel Maesen, Geovise BVBA on 01/03/15.
 */
public class NumericalMethods {

    public static enum Orientation {
        Clockwise, Counterclockwise, Colinear
    }

    public final static int ClockWise = 1;
    public final static int CounterClockWise = 2;


    /**
     * Calculates the determinant of a 2x2 matrix
     *
     * @param a11
     * @param a12
     * @param a21
     * @param a22
     * @return the determinant
     */
    public static double determinant(double a11, double a12, double a21, double a22) {
        TwoSum ts = new TwoSum(a11 * a22, -a12 * a21);
        return ts.estimate + ts.error; // this is different from just the estimate, if the estimate is zero
    }


    public static double crossProduct(double x0, double y0, double x1, double y1) {
        return determinant(x0, x1, y0, y1);
    }

    /**
     * Calculates the determinant of a 3x3 matrix
     *
     * @param a11
     * @param a12
     * @param a13
     * @param a21
     * @param a22
     * @param a23
     * @param a31
     * @param a32
     * @param a33
     * @return
     */
    public static double determinant(double a11, double a12, double a13, double a21, double a22, double a23, double
            a31, double a32, double a33) {

        TwoSum tl1 = new TwoSum(a11 * a22 * a33, a12 * a23 * a31);
        TwoSum tl2 = new TwoSum(tl1.estimate, a13 * a21 * a32);


        TwoSum tr1 = new TwoSum(a13 * a22 * a31, a12 * a21 * a33);
        TwoSum tr2 = new TwoSum(tr1.estimate, a11 * a23 * a32);

        TwoSum det = new TwoSum(tl2.estimate, -tr2.estimate);

        return det.estimate + det.error + tl1.error + tl2.error - tr1.error - tr2.error;
    }

    /**
     * Determines the orientation of the triangle determined by p0, p1, p2 in 2D.
     * 
     * <p>Counterclockwise is equivalent to p2 is to the left of the line p0 - p1.</p>
     *
     * <p>The result is exact, see {@link Predicates#orientation(Position, Position, Position)}.</p>
     *
     * @param p0
     * @param p1
     * @param p2
     * @return the orientation of the triangle
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static Orientation orientation(Position p0, Position p1, Position p2) {
        int sign = Predicates.orientation(p0, p1, p2);
        if (sign == 0) {
            return Orientation.Colinear;
        }
        return sign > 0? Orientation.Counterclockwise : Orientation.Clockwise;
    }

    /**
     * Determine the (signed) area of a linearring, using the the
     * <a href="https://en.wikipedia.org/wiki/Shoelace_formula">Shoelace algorithm</a>.
     *
     * @param ring
     * @return
     */
    public static double area(LinearRing<?> ring) {
        return IntStream.range(1, ring.getPositions().size()).parallel().boxed().
                map(idx -> shoelaceStep(idx, ring.getPositions())).
                reduce((a, b) -> a + b).
                filter(orientation -> orientation != 0).
                orElseThrow(() -> new IllegalArgumentException("Ring is collinear in 2D")) ;
    }

    private static double shoelaceStep(Integer idx, PositionSequence<?> positions) {
        return  determinant(positions.getPositionN(idx - 1), positions.getPositionN(idx));
    }

    /**
     * Determines whether the specified {@code LinearRing} is counter-clockwise.
     *
     * <p> Orientation is determined by the (exact) sign of the signed area of the given ring.
     * <p>The specified Ring is assumed to be valid.
     *
     * @param ring a {@code LinearRing}
     * @return true if the positions of the ring describe it counter-clockwise
     * @throws IllegalArgumentException if the signed area is zero, or a coordinate is NaN or infinite
     */
    public static boolean isCounterClockwise(LinearRing<?> ring) {
        double res = orient2d(ring);
        if (res == 0) {
            throw new IllegalArgumentException("Positions are collinear in 2D");
        }
        return res > 0;
    }

    /**
     * Determine the orientation of a {@code LinearRing}
     *
     * <p>The orientation is the exact sign of the signed area of the ring,
     * see {@link Predicates#ringOrientation(PositionSequence)}.</p>
     *
     * @param ring the linear ring
     * @return -1 if the specified ring is clockwise, +1 if counterclockwise, 0 if the signed area is zero (e.g.
     * if all vertices are collinear)
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static double orient2d(LinearRing<?> ring) {
        return Predicates.ringOrientation(ring.getPositions());
    }

    /**
     * Determines whether the specified positions are collinear in 2D. The result is exact.
     *
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static boolean collinear(Position p0, Position p1, Position p2) {
        return Predicates.orientation(p0, p1, p2) == 0;
    }

    public static double determinant(Position p0, Position p1) {
        double[] c0 = p0.toArray(null);
        double[] c1 = p1.toArray(null);
        return determinant(c0[0], c0[1], c1[0], c1[1]);
    }


    public static class TwoSum {
        final double estimate;
        final double error;

        public TwoSum(double a, double b) {
            double s = a + b;
            double a1 = s - b;
            double b1 = s - a1;
            double deltaA = a - a1;
            double deltaB = b - b1;
            estimate = s;
            error = deltaA + deltaB;
        }
    }

}
