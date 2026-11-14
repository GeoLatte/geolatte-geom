package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;

import static org.junit.Assert.*;

public class SummationTest {

    @Test
    public void emptySumIsZero() {
        Summation sum = new Summation();
        assertEquals(0, sum.signum());
        assertEquals(0.0, sum.round(), 0);
        assertThrows(ArithmeticException.class, () -> Summation.roundQuotient(new Summation().add(1), sum));
        assertEquals(0.0, Summation.roundQuotient(sum, new Summation().add(3)), 0);
    }

    @Test
    public void cancellation() {
        Summation sum = new Summation().add(1e300).add(1).add(-1e300);
        assertEquals(1.0, sum.round(), 0);
        assertEquals(1, sum.signum());
        // 1 + 2^-53 is a tie: rounded to even
        assertEquals(1.0, new Summation().add(1).add(0x1p-53).round(), 0);
        assertEquals(1 + 0x1p-52, new Summation().add(1).add(0x1p-53).add(0x1p-200).round(), 0);
        assertEquals(1 + 0x1p-51, new Summation().add(1).add(0x1p-52).add(0x1p-53).round(), 0);
        // a product that is not representable: (1 + 2^-30)^2 - 1 - 2^-29 = 2^-60
        double a = 1 + 0x1p-30;
        assertEquals(0x1p-60, new Summation().addProduct(a, a).add(-1).add(-0x1p-29).round(), 0);
    }

    @Test
    public void scaleAndQuotients() {
        Summation sum = new Summation().add(3);
        assertEquals(1.5, sum.round(-1), 0);
        assertEquals(Double.MIN_VALUE, new Summation().add(1).round(-1074), 0);
        assertEquals(0.0, new Summation().add(1).round(-1076), 0);            // below half the smallest subnormal
        assertEquals(Double.POSITIVE_INFINITY, new Summation().add(1).round(1100), 0);
        assertEquals(1.0 / 3, Summation.roundQuotient(new Summation().add(1), new Summation().add(3)), 0);
        assertEquals(-2.0 / 3, Summation.roundQuotient(new Summation().add(2), new Summation().add(-3)), 0);
    }

    @Test
    public void rejectsNonFiniteValues() {
        assertThrows(IllegalArgumentException.class, () -> new Summation().add(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new Summation().addProduct(1, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new Summation().addProduct(1, 2, Double.NaN));
    }

    @Test
    public void randomSumsMatchBigDecimal() {
        Random random = new Random(3);
        for (int k = 0; k < 3000; k++) {
            Summation sum = new Summation();
            Summation other = new Summation();
            BigDecimal exact = BigDecimal.ZERO, exactOther = BigDecimal.ZERO;
            int terms = 1 + random.nextInt(40);
            for (int t = 0; t < terms; t++) {
                double a = randomDouble(random), b = randomDouble(random), c = randomDouble(random);
                switch (random.nextInt(4)) {
                    case 0:
                        sum.add(a);
                        exact = exact.add(new BigDecimal(a));
                        break;
                    case 1:
                        sum.addProduct(a, b);
                        exact = exact.add(new BigDecimal(a).multiply(new BigDecimal(b)));
                        break;
                    case 2:
                        sum.addProduct(a, b, c);
                        exact = exact.add(new BigDecimal(a).multiply(new BigDecimal(b)).multiply(new BigDecimal(c)));
                        break;
                    default:
                        other.addProduct(a, b);
                        exactOther = exactOther.add(new BigDecimal(a).multiply(new BigDecimal(b)));
                }
            }
            if (random.nextBoolean()) {
                // cancel (most of) the sum
                double r = exact.doubleValue();
                if (Double.isFinite(r)) {
                    sum.add(-r);
                    exact = exact.subtract(new BigDecimal(r));
                }
            }
            assertEquals(exact.signum(), sum.signum());
            assertEquals(roundExactly(exact, BigDecimal.ONE), sum.round(), 0);
            if (exactOther.signum() != 0) {
                assertEquals(roundExactly(exact, exactOther), Summation.roundQuotient(sum, other), 0);
                Summation combined = new Summation().add(1, sum).add(-1, other);
                assertEquals(roundExactly(exact.subtract(exactOther), BigDecimal.ONE), combined.round(), 0);
            }
        }
    }

    // a double with a random sign, significand and exponent; products of three of them stay finite
    private static double randomDouble(Random random) {
        double v = (1 + random.nextDouble()) * Math.scalb(1.0, random.nextInt(600) - 300);
        if (random.nextInt(8) == 0) v = Math.scalb(v, random.nextBoolean() ? 700 : -700);
        if (random.nextInt(10) == 0) v = Math.rint(v);   // also zero, and integers
        return random.nextBoolean() ? v : -v;
    }

    // num / den, rounded to the nearest double (ties to even), independently of the implementation under test
    static double roundExactly(BigDecimal num, BigDecimal den) {
        if (num.signum() == 0) return 0;
        double c = num.divide(den, MathContext.DECIMAL128).doubleValue();
        if (Double.isInfinite(c)) return c;
        if (c == 0) c = num.signum() * den.signum() > 0 ? Double.MIN_VALUE : -Double.MIN_VALUE;
        for (int iteration = 0; iteration < 10; iteration++) {
            double down = Math.nextDown(c), up = Math.nextUp(c);
            int lo = compare(num, den, midpoint(c, down));
            int hi = compare(num, den, midpoint(c, up));
            if (lo < 0) {
                c = down;
            } else if (hi > 0) {
                c = up;
            } else if (lo == 0) {
                return even(c) ? c : down;
            } else if (hi == 0) {
                return even(c) ? c : up;
            } else {
                return c;
            }
        }
        throw new AssertionError("no convergence");
    }

    private static BigDecimal midpoint(double a, double b) {
        return new BigDecimal(a).add(new BigDecimal(b)).divide(BigDecimal.valueOf(2));
    }

    private static int compare(BigDecimal num, BigDecimal den, BigDecimal m) {
        return num.subtract(m.multiply(den)).signum() * den.signum();
    }

    private static boolean even(double v) {
        return (Double.doubleToRawLongBits(v) & 1) == 0;
    }
}
