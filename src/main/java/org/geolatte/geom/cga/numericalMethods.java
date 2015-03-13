package org.geolatte.geom.cga;

import org.geolatte.geom.Position;

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

    public static boolean collinear(Position p0, Position p1, Position p2) {
        double det = deltaDeterminant(p0, p1, p2);
        return det == 0;
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
