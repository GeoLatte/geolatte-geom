package org.geolatte.geom.cga;

import org.geolatte.geom.LinearRing;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;

import java.util.stream.IntStream;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/03/15.
 */
public class NumericalMethods {

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
     * Determines if the triangle determined by p0, p1, p2 is counterclockwise in 2D.
     * 
     * <p>This is equivalent to p2 is to the left of the line p0 - p1.</p>
     *
     * @param p0
     * @param p1
     * @param p2
     * @return true if counterclockwise
     */
    public static boolean isCounterClockwise(Position p0, Position p1, Position p2) {
        double det = deltaDeterminant(p0, p1, p2);
        if (det == 0) {
            throw new IllegalArgumentException("Positions are collinear in 2D");
        }
        return det > 0;
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
     * <p> Orientation is determined by calculating the signed area of the given ring
     * and using the sign to determine the orientation
     * <p>The specified Ring is assumed to be valid.
     *
     * @param ring a {@code LinearRing}
     * @return true if the positions of the ring describe it counter-clockwise
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
     * This uses Newell's method (see Graphics Gems III, V. 5)
     *
     * @param ring the linear ring
     * @return +1 if the specified ring is clockwise, -1 if counterclockwise, 0 if all vertices are collinear
     */
    public static double orient2d(LinearRing<?> ring) {
        return orient2d(ring.getPositions());
    }

    private static double orient2d(PositionSequence<?> positions) {
        double d = 0;
        for (int i = 0; i < positions.size() - 1; i++) {
            double[] p1 = positions.getPositionN(i).toArray(null);
            double[] p2 = positions.getPositionN(i+1).toArray(null);
            d += (p1[0] - p2[0]) * (p1[1] + p2[1]);
        }
        return Math.signum(d);
    }

    public static boolean collinear(Position p0, Position p1, Position p2) {
        double det = deltaDeterminant(p0, p1, p2);
        return det == 0;
    }

    public static double determinant(Position p0, Position p1) {
        double[] c0 = p0.toArray(null);
        double[] c1 = p1.toArray(null);
        return determinant(c0[0], c0[1], c1[0], c1[1]);
    }
    
    private static double deltaDeterminant(Position p0, Position p1, Position p2) {
        double[] c0 = p0.toArray(null);
        double[] c1 = p1.toArray(null);
        double[] c2 = p2.toArray(null);
        return determinant(1, 1, 1, c0[0], c1[0], c2[0], c0[1], c1[1], c2[1]);
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
