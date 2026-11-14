package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Random;

import static org.junit.Assert.*;

public class MinimumDistanceTest {

    @Test
    public void sqrtQuotientOfSquares() {
        assertEquals(0.0, MinimumDistance.sqrtQuotient(BigInteger.ZERO, BigInteger.ONE, 0), 0);
        assertEquals(3.0, MinimumDistance.sqrtQuotient(BigInteger.valueOf(9), BigInteger.ONE, 0), 0);
        assertEquals(1.5, MinimumDistance.sqrtQuotient(BigInteger.valueOf(9), BigInteger.valueOf(4), 0), 0);
        assertEquals(6.0, MinimumDistance.sqrtQuotient(BigInteger.valueOf(9), BigInteger.ONE, 2), 0);
        assertEquals(6.0, MinimumDistance.sqrtQuotient(BigInteger.valueOf(18), BigInteger.ONE, 1), 0);
        assertEquals(Double.MIN_VALUE, MinimumDistance.sqrtQuotient(BigInteger.ONE, BigInteger.ONE, -2148), 0);
        assertEquals(0x1p1023, MinimumDistance.sqrtQuotient(BigInteger.ONE, BigInteger.ONE, 2046), 0);
    }

    @Test
    public void sqrtQuotientTies() {
        // (2^53 + 1)^2 is exactly the square of a midpoint: ties to even go down, (2^53 + 3)^2 up
        BigInteger m = BigInteger.ONE.shiftLeft(53).add(BigInteger.ONE);
        assertEquals(0x1p53, MinimumDistance.sqrtQuotient(m.multiply(m), BigInteger.ONE, 0), 0);
        m = BigInteger.ONE.shiftLeft(53).add(BigInteger.valueOf(3));
        assertEquals(0x1p53 + 4, MinimumDistance.sqrtQuotient(m.multiply(m), BigInteger.ONE, 0), 0);
        // just above and just below the midpoint
        m = BigInteger.ONE.shiftLeft(53).add(BigInteger.ONE);
        BigInteger square = m.multiply(m);
        assertEquals(0x1p53 + 2, MinimumDistance.sqrtQuotient(square.add(BigInteger.ONE), BigInteger.ONE, 0), 0);
        assertEquals(0x1p53, MinimumDistance.sqrtQuotient(square.subtract(BigInteger.ONE), BigInteger.ONE, 0), 0);
        // a tie in the subnormal range: (3/2 * 2^-1074)^2 between 2^-1074 and 2^-1073 rounds to the even 2^-1073
        assertEquals(2 * Double.MIN_VALUE,
                MinimumDistance.sqrtQuotient(BigInteger.valueOf(9), BigInteger.valueOf(4), -2148), 0);
        assertEquals(2 * Double.MIN_VALUE,
                MinimumDistance.sqrtQuotient(BigInteger.valueOf(25), BigInteger.valueOf(4), -2148), 0);
    }

    @Test
    public void randomSqrtQuotients() {
        Random random = new Random(71);
        for (int k = 0; k < 20000; k++) {
            BigInteger num = new BigInteger(1 + random.nextInt(300), random).add(BigInteger.ONE);
            BigInteger den = new BigInteger(1 + random.nextInt(300), random).add(BigInteger.ONE);
            int exp = random.nextInt(2000) - 1000 - (random.nextInt(4) == 0 ? 1100 : 0);
            BigDecimal n = new BigDecimal(num), d = new BigDecimal(den);
            if (exp >= 0) n = n.multiply(new BigDecimal(BigInteger.ONE.shiftLeft(exp)));
            else d = d.multiply(new BigDecimal(BigInteger.ONE.shiftLeft(-exp)));
            assertEquals(num + " / " + den + " * 2^" + exp, roundSqrt(n, d),
                    MinimumDistance.sqrtQuotient(num, den, exp), 0);
        }
    }

    @Test
    public void distanceAtATie() {
        // the perpendicular distance of (1, 5 * 2^51 + 2) to the line through (0, 0) and (4, 3) * 2^60 is exactly
        // 2^53 + 1, the midpoint between 2^53 and 2^53 + 2: ties to even
        MinimumDistance distance = new MinimumDistance();
        distance.addPointSegment(1, 5 * 0x1p51 + 2, 0, 0, 4 * 0x1p60, 3 * 0x1p60);
        assertEquals(0x1p53, distance.distance(), 0);
        distance = new MinimumDistance();
        distance.addPointSegment(3, 5 * 0x1p51 + 6, 0, 0, 4 * 0x1p60, 3 * 0x1p60);
        assertEquals(0x1p53 + 4, distance.distance(), 0);
        // the same at a small scale
        distance = new MinimumDistance();
        distance.addPointSegment(0x1p-900, (5 * 0x1p51 + 2) * 0x1p-900, 0, 0, 0x1p-838, 3 * 0x1p-840);
        assertEquals(0x1p-847, distance.distance(), 0);
    }

    @Test
    public void emptyAndZero() {
        MinimumDistance distance = new MinimumDistance();
        assertTrue(distance.isEmpty());
        distance.addPointPoint(1, 1, 1, 1);
        assertEquals(0.0, distance.distance(), 0);
        distance = new MinimumDistance();
        distance.addPointSegment(1, 1, 0, 0, 2, 2);
        assertEquals(0.0, distance.distance(), 0);
    }

    /**
     * The oracle: sqrt(num / den) rounded to the nearest double (ties to even), with midpoint tests on the squares
     */
    public static double roundSqrt(BigDecimal num, BigDecimal den) {
        if (num.signum() == 0) return 0;
        MathContext context = new MathContext(40);
        double c = num.divide(den, context).sqrt(context).doubleValue();
        if (c == 0) c = Double.MIN_VALUE;
        for (int iteration = 0; iteration < 10; iteration++) {
            double down = Math.nextDown(c), up = Math.nextUp(c);
            // the exact value is not negative: no lower neighbour below 0
            int lo = c == 0 ? 1 : num.subtract(square(midpoint(c, down)).multiply(den)).signum();
            int hi = num.subtract(square(midpoint(c, up)).multiply(den)).signum();
            if (lo < 0) {
                c = down;
            } else if (hi > 0) {
                c = up;
            } else if (lo == 0) {
                return (Double.doubleToRawLongBits(c) & 1) == 0 ? c : down;
            } else if (hi == 0) {
                return (Double.doubleToRawLongBits(c) & 1) == 0 ? c : up;
            } else {
                return c;
            }
        }
        throw new AssertionError("no convergence");
    }

    private static BigDecimal midpoint(double a, double b) {
        return new BigDecimal(a).add(new BigDecimal(b)).divide(BigDecimal.valueOf(2));
    }

    private static BigDecimal square(BigDecimal v) {
        return v.multiply(v);
    }
}
