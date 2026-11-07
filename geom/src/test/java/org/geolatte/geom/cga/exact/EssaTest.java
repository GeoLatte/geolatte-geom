package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class EssaTest {

    private final Random random = new Random(42L);

    @Test
    public void trivialSums() {
        assertEquals(0, Essa.sign());
        assertEquals(0, Essa.sign(0, 0));
        assertEquals(1, Essa.sign(1e-300));
        assertEquals(-1, Essa.sign(-Double.MIN_VALUE));
        assertEquals(0, Essa.sign(1, -1));
        assertEquals(1, Essa.sign(1, -1, 0x1p-1074));
    }

    @Test
    public void sumsThatFloatingPointGetsWrong() {
        // naive summation returns 0 in all these cases
        assertEquals(1, Essa.sign(1e20, 1, -1e20));
        assertEquals(-1, Essa.sign(1e20, -1, -1e20));
        assertEquals(1, Essa.sign(1.0, 0x1p-60, -1.0));
        assertEquals(-1, Essa.sign(0x1p1000, -0x1p-1000, -0x1p1000));
    }

    @Test
    public void randomSums() {
        for (int k = 0; k < 50_000; k++) {
            int l = 1 + random.nextInt(60);
            double[] s = new double[l];
            for (int i = 0; i < l; i++) {
                s[i] = randomSummand();
                if (i > 0 && k % 2 == 0 && random.nextBoolean()) {
                    s[i] = -s[random.nextInt(i)]; // provoke cancellation
                }
            }
            check(s);
        }
    }

    /**
     * The ill-conditioned test series from Sec. 4.5 of the book: 8c - c - c - ... - c (8 times) for many c, plus
     * a tiny summand that decides the sign.
     */
    @Test
    public void illConditionedSums() {
        for (int k = 0; k < 200; k++) {
            int groups = 1 + random.nextInt(500);
            double[] s = new double[groups * 9 + 1];
            int idx = 0;
            for (int g = 0; g < groups; g++) {
                double c = Math.scalb(1 + random.nextDouble(), random.nextInt(200) - 100);
                s[idx++] = 8 * c;
                for (int j = 0; j < 8; j++) s[idx++] = -c;
            }
            double tiny = Math.scalb(1.0, -random.nextInt(200) - 300);
            s[idx] = random.nextBoolean() ? tiny : -tiny;
            int expected = (int) Math.signum(s[idx]);
            shuffle(s);
            assertEquals(expected, Essa.sign(s));
        }
    }

    @Test
    public void subnormalSummands() {
        for (int k = 0; k < 20_000; k++) {
            int l = 1 + random.nextInt(10);
            double[] s = new double[l];
            for (int i = 0; i < l; i++) {
                s[i] = (random.nextBoolean() ? 1 : -1) * random.nextInt(1 << 20) * Double.MIN_VALUE
                        + (random.nextInt(3) == 0 ? (random.nextBoolean() ? Double.MIN_NORMAL : -Double.MIN_NORMAL) : 0);
            }
            check(s);
        }
    }

    @Test
    public void largeSummands() {
        double big = Math.nextDown(Essa.MAX_SUMMAND);
        assertEquals(1, Essa.sign(big, big, -big, 1e-300));
        assertEquals(-1, Essa.sign(big, -big, -big, big, -1e-300));
    }

    @Test
    public void inputIsNotModified() {
        double[] s = {3, -1, -1, -1, 1e-30, 7, -7};
        double[] copy = s.clone();
        Essa.sign(s);
        assertArrayEquals(copy, s, 0);
    }

    @Test
    public void usesOnlyFirstLengthElements() {
        assertEquals(1, Essa.sign(new double[]{1, -2}, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNaN() {
        Essa.sign(1, Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsTooLargeSummands() {
        Essa.sign(1, Essa.MAX_SUMMAND);
    }

    private void check(double[] s) {
        BigDecimal sum = BigDecimal.ZERO;
        for (double v : s) sum = sum.add(new BigDecimal(v));
        assertEquals(Arrays.toString(s), sum.signum(), Essa.sign(s));
    }

    private double randomSummand() {
        double v = Math.scalb(1 + random.nextDouble(), random.nextInt(400) - 200);
        if (random.nextInt(4) == 0) v = Math.scalb(1.0, random.nextInt(100) - 50); // powers of two are special
        return random.nextBoolean() ? v : -v;
    }

    private void shuffle(double[] s) {
        for (int i = s.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double t = s[i];
            s[i] = s[j];
            s[j] = t;
        }
    }
}
