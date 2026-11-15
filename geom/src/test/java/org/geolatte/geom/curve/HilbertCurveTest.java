package org.geolatte.geom.curve;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class HilbertCurveTest {

    @Test
    public void orderOne() {
        assertEquals(0, HilbertCurve.index(0, 0, 1));
        assertEquals(1, HilbertCurve.index(0, 1, 1));
        assertEquals(2, HilbertCurve.index(1, 1, 1));
        assertEquals(3, HilbertCurve.index(1, 0, 1));
    }

    @Test
    public void everyCellOnceAndAlwaysToANeighbour() {
        for (int order = 1; order <= 6; order++) {
            int size = 1 << order;
            int[] previous = null;
            boolean[] seen = new boolean[size * size];
            for (long d = 0; d < (long) size * size; d++) {
                int[] cell = HilbertCurve.cell(d, order);
                assertEquals(d, HilbertCurve.index(cell[0], cell[1], order));
                int key = cell[0] * size + cell[1];
                assertFalse(seen[key]);
                seen[key] = true;
                if (previous != null) {
                    assertEquals(1, Math.abs(cell[0] - previous[0]) + Math.abs(cell[1] - previous[1]));
                }
                previous = cell;
            }
            assertArrayEquals(new int[]{0, 0}, HilbertCurve.cell(0, order));
            assertArrayEquals(new int[]{size - 1, 0}, HilbertCurve.cell((long) size * size - 1, order));
        }
    }

    @Test
    public void highOrders() {
        Random random = new Random(7);
        for (int order : new int[]{16, 20, 30, HilbertCurve.MAX_ORDER}) {
            int max = order == 31 ? Integer.MAX_VALUE : (1 << order) - 1;
            for (int i = 0; i < 1000; i++) {
                int x = (int) (random.nextDouble() * max), y = (int) (random.nextDouble() * max);
                long d = HilbertCurve.index(x, y, order);
                assertTrue(d >= 0 && d < 1L << (2 * order));
                assertArrayEquals(new int[]{x, y}, HilbertCurve.cell(d, order));
            }
            long last = (1L << (2 * order)) - 1;
            assertArrayEquals(new int[]{max, 0}, HilbertCurve.cell(last, order));
            assertEquals(last, HilbertCurve.index(max, 0, order));
        }
    }

    @Test
    public void invalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> HilbertCurve.index(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> HilbertCurve.index(0, 0, 32));
        assertThrows(IllegalArgumentException.class, () -> HilbertCurve.index(2, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> HilbertCurve.index(-1, 0, 4));
        assertThrows(IllegalArgumentException.class, () -> HilbertCurve.cell(4, 1));
        assertThrows(IllegalArgumentException.class, () -> HilbertCurve.cell(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> HilbertCurve.order(new double[2], new double[3]));
        assertThrows(IllegalArgumentException.class, () -> HilbertCurve.order(new double[2], new double[2], 3));
    }

    @Test
    public void orderFollowsTheCurve() {
        // the cell centres of a 4 x 4 grid, in a scrambled order, come back in the order of the curve
        int order = 2, size = 4;
        List<int[]> cells = new ArrayList<>();
        for (long d = 0; d < size * size; d++) cells.add(HilbertCurve.cell(d, order));
        int[] scramble = {5, 12, 0, 9, 3, 15, 7, 1, 14, 10, 2, 8, 11, 4, 13, 6};
        double[] xs = new double[size * size], ys = new double[size * size];
        for (int i = 0; i < scramble.length; i++) {
            xs[i] = 10 + 2.5 * cells.get(scramble[i])[0];
            ys[i] = -3 + 2.5 * cells.get(scramble[i])[1];
        }
        int[] result = HilbertCurve.order(xs, ys);
        for (int i = 0; i < result.length; i++) assertEquals(i, scramble[result[i]]);
    }

    @Test
    public void orderIsAPermutation() {
        Random random = new Random(3);
        double[] xs = new double[1000], ys = new double[1000];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = 1e6 * random.nextGaussian();
            ys[i] = i % 10 == 0 ? Double.NaN : random.nextDouble();
        }
        xs[1] = Double.MAX_VALUE;
        xs[2] = -Double.MAX_VALUE;
        int[] result = HilbertCurve.order(xs, ys);
        int[] sorted = result.clone();
        Arrays.sort(sorted);
        for (int i = 0; i < sorted.length; i++) assertEquals(i, sorted[i]);
        // equal points keep their order
        assertArrayEquals(new int[]{0, 1, 2}, HilbertCurve.order(new double[]{1, 1, 1}, new double[]{2, 2, 2}));
        assertArrayEquals(new int[0], HilbertCurve.order(new double[0], new double[0]));
        // only the first two points: (0, 0) starts the curve, (5, 0) ends it
        assertArrayEquals(new int[]{1, 0}, HilbertCurve.order(new double[]{5, 0, 7}, new double[]{0, 0, 9}, 2));
    }

    @Test
    public void sortList() {
        List<double[]> points = new ArrayList<>();
        points.add(new double[]{1, 0});    // last along the curve
        points.add(new double[]{0, 1});
        points.add(new double[]{0, 0});    // first
        points.add(new double[]{1, 1});
        HilbertCurve.sort(points, p -> p[0], p -> p[1]);
        assertArrayEquals(new double[]{0, 0}, points.get(0), 0);
        assertArrayEquals(new double[]{0, 1}, points.get(1), 0);
        assertArrayEquals(new double[]{1, 1}, points.get(2), 0);
        assertArrayEquals(new double[]{1, 0}, points.get(3), 0);
    }
}
