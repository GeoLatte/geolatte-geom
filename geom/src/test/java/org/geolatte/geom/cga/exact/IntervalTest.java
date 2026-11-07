package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;
import java.util.function.BinaryOperator;

import static org.junit.Assert.*;

public class IntervalTest {

    private final Random random = new Random(20261107L);

    @Test
    public void pointOperationsGiveNarrowestEnclosure() {
        for (int k = 0; k < 50_000; k++) {
            double a = randomDouble(), b = randomDouble();
            checkNarrowest(Interval.of(a).add(Interval.of(b)), big(a).add(big(b)));
            checkNarrowest(Interval.of(a).sub(Interval.of(b)), big(a).subtract(big(b)));
            checkNarrowest(Interval.difference(a, b), big(a).subtract(big(b)));
            checkNarrowest(Interval.of(a).mul(Interval.of(b)), big(a).multiply(big(b)));
            checkNarrowest(Interval.product(a, b), big(a).multiply(big(b)));
            checkNarrowest(Interval.of(a).sqr(), big(a).multiply(big(a)));
        }
    }

    @Test
    public void exactResultsGivePointIntervals() {
        assertEquals(Interval.of(3), Interval.of(1).add(Interval.of(2)));
        assertEquals(Interval.ZERO, Interval.difference(150000.25, 150000.25));
        assertEquals(Interval.of(0.75), Interval.product(1.5, 0.5));
        assertEquals(0, Interval.difference(1e17, 1e17).mul(Interval.of(7)).signum());
    }

    @Test
    public void intervalOperationsContainAllResults() {
        BinaryOperator<Interval> add = Interval::add, sub = Interval::sub, mul = Interval::mul;
        for (int k = 0; k < 20_000; k++) {
            Interval x = randomInterval(), y = randomInterval();
            for (double s : new double[]{0, 0.25, 0.5, 1}) {
                for (double t : new double[]{0, 0.3, 1}) {
                    double xs = x.lo() + s * (x.hi() - x.lo());
                    double yt = y.lo() + t * (y.hi() - y.lo());
                    if (!x.contains(xs) || !y.contains(yt)) continue;
                    checkContains(add.apply(x, y), big(xs).add(big(yt)));
                    checkContains(sub.apply(x, y), big(xs).subtract(big(yt)));
                    checkContains(mul.apply(x, y), big(xs).multiply(big(yt)));
                    checkContains(x.sqr(), big(xs).multiply(big(xs)));
                }
            }
        }
    }

    @Test
    public void overflowKeepsInclusion() {
        Interval s = Interval.of(Double.MAX_VALUE).add(Interval.of(Double.MAX_VALUE));
        assertEquals(Double.MAX_VALUE, s.lo(), 0);
        assertEquals(Double.POSITIVE_INFINITY, s.hi(), 0);
        assertEquals(1, s.signum());
        Interval p = Interval.product(-1e200, 1e200);
        assertEquals(Double.NEGATIVE_INFINITY, p.lo(), 0);
        assertEquals(-Double.MAX_VALUE, p.hi(), 0);
        assertEquals(-1, p.signum());
    }

    @Test
    public void underflowKeepsInclusion() {
        Interval p = Interval.product(1e-200, 1e-200);
        assertEquals(Interval.UNDECIDED, p.signum());
        assertTrue(p.lo() <= 0 && p.hi() > 0);
        checkContains(Interval.product(3e-160, 7e-161), big(3e-160).multiply(big(7e-161)));
    }

    @Test
    public void signum() {
        assertEquals(1, Interval.of(1e-300, 1).signum());
        assertEquals(-1, Interval.of(-1, -1e-300).signum());
        assertEquals(0, Interval.ZERO.signum());
        assertEquals(Interval.UNDECIDED, Interval.of(-1e-300, 0).signum());
        assertEquals(Interval.UNDECIDED, Interval.of(-1, 1).signum());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidBounds() {
        Interval.of(1, 0);
    }

    private void checkNarrowest(Interval i, BigDecimal exact) {
        checkContains(i, exact);
        if (i.lo() == i.hi()) {
            assertEquals(0, big(i.lo()).compareTo(exact));
        } else if (exact.abs().compareTo(big(0x1p-968)) >= 0) {
            // outside the underflow range, bounds are exactly rounded
            // the exact value lies strictly between two adjacent doubles
            assertEquals("Not narrowest: " + i, Math.nextUp(i.lo()), i.hi(), 0);
        }
    }

    private void checkContains(Interval i, BigDecimal exact) {
        if (Double.isInfinite(i.lo()) || Double.isInfinite(i.hi())) {
            return;
        }
        assertTrue(i + " does not contain " + exact,
                big(i.lo()).compareTo(exact) <= 0 && exact.compareTo(big(i.hi())) <= 0);
    }

    private Interval randomInterval() {
        double a = randomDouble();
        double b = a + random.nextDouble() * Math.abs(a);
        return Interval.of(Math.min(a, b), Math.max(a, b));
    }

    private double randomDouble() {
        double v = Math.scalb(1 + random.nextDouble(), random.nextInt(200) - 100);
        if (random.nextInt(10) == 0) v = Math.scalb(v, -1000); // subnormal range
        return random.nextBoolean() ? v : -v;
    }

    private static BigDecimal big(double v) {
        return new BigDecimal(v);
    }
}
