package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;

public class ExactSumTest {

    private final Random random = new Random(7L);

    @Test
    public void productsAreExact() {
        for (int k = 0; k < 50_000; k++) {
            double a = rnd(), b = rnd(), c = rnd(), d = rnd();
            ExactSum s = new ExactSum().addProduct(a, b).subProduct(c, d).add(a).addProduct(a, b, c, d).subProduct(d, c, b, a);
            BigDecimal expected = big(a).multiply(big(b)).subtract(big(c).multiply(big(d))).add(big(a));
            assertTrue(s.isExact());
            assertEquals(0, expected.compareTo(value(s)));
            assertEquals(expected.signum(), s.signum());
        }
    }

    @Test
    public void productOfSumsIsExact() {
        for (int k = 0; k < 5_000; k++) {
            ExactSum x = new ExactSum().addProduct(rnd(), rnd()).add(rnd());
            ExactSum y = new ExactSum().addProduct(rnd(), rnd()).subProduct(rnd(), rnd());
            ExactSum p = new ExactSum().addProduct(1, x, y).addProduct(-1, y, y).add(-1, x);
            BigDecimal expected = value(x).multiply(value(y)).subtract(value(y).multiply(value(y))).subtract(value(x));
            assertTrue(p.isExact());
            assertEquals(0, expected.compareTo(value(p)));
        }
    }

    @Test
    public void zeroFactorsAddNoTerms() {
        ExactSum s = new ExactSum().addProduct(0, 5).addProduct(3, 0, 1, 1).add(0);
        assertEquals(0, s.size());
        assertEquals(0, s.signum());
    }

    @Test
    public void underflowIsDetected() {
        assertFalse(new ExactSum().addProduct(1e-200, 1e-200).isExact());
        // product itself fine, but the rounding error would underflow
        assertFalse(new ExactSum().addProduct(1e-150, 1e-150).isExact());
        assertFalse(new ExactSum().addProduct(1e-80, 1e-80, 1e-80, 1e-80).isExact());
        assertTrue(new ExactSum().addProduct(1e-40, 1e-40, 1e-40, 1e-40).isExact());
    }

    @Test
    public void overflowIsDetected() {
        assertFalse(new ExactSum().addProduct(1e200, 1e200).isExact());
        assertFalse(new ExactSum().add(Double.MAX_VALUE).isExact());
        assertFalse(new ExactSum().addProduct(1e80, 1e80, 1e80, 1e80).isExact());
        ExactSum inexact = new ExactSum().addProduct(1e200, 1e200);
        assertFalse(new ExactSum().add(1, inexact).isExact());
        assertFalse(new ExactSum().addProduct(1, inexact, new ExactSum().add(1)).isExact());
    }

    @Test
    public void tripleProductsAndScaledSumsAreExact() {
        for (int i = 0; i < 10_000; i++) {
            double a = rnd(), b = rnd(), c = rnd(), f = rnd();
            ExactSum triple = new ExactSum().addProduct(a, b, c);
            assertTrue(triple.size() <= 4);
            assertEquals(0, big(a).multiply(big(b)).multiply(big(c)).compareTo(value(triple)));
            ExactSum scaled = new ExactSum().addProduct(f, triple);
            assertTrue(scaled.size() <= 8);
            assertEquals(0, big(f).multiply(value(triple)).compareTo(value(scaled)));
        }
        assertFalse(new ExactSum().addProduct(1e-100, 1e-100, 1e-100).isExact());
        assertFalse(new ExactSum().addProduct(1e300, 1e300, 1).isExact());
        assertFalse(new ExactSum().addProduct(1, new ExactSum().addProduct(1e200, 1e200)).isExact());
    }

    @Test(expected = ArithmeticException.class)
    public void signumOfInexactSumThrows() {
        new ExactSum().addProduct(1e-200, 1e-200).signum();
    }

    private double rnd() {
        double v = Math.scalb(1 + random.nextDouble(), random.nextInt(100) - 50);
        return random.nextBoolean() ? v : -v;
    }

    private static BigDecimal value(ExactSum s) {
        BigDecimal sum = BigDecimal.ZERO;
        for (double t : s.terms()) sum = sum.add(big(t));
        return sum;
    }

    private static BigDecimal big(double v) {
        return new BigDecimal(v);
    }
}
