package org.geolatte.geom.index;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class PackedRTreeTest {

    @Test
    public void emptyTreeReturnsNothing() {
        PackedRTree<String> tree = PackedRTree.<String>builder().build();
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertTrue(tree.query(-1e9, -1e9, 1e9, 1e9).isEmpty());
        assertTrue(tree.query(0, 0, 1, 1, item -> false));
    }

    @Test
    public void singleItem() {
        PackedRTree<String> tree = PackedRTree.<String>builder().add("a", 0, 0, 1, 1).build();
        assertEquals(1, tree.size());
        assertEquals(Collections.singletonList("a"), tree.query(0.5, 0.5, 2, 2));
        assertTrue(tree.query(2, 2, 3, 3).isEmpty());
    }

    @Test
    public void boxesThatOnlyTouchIntersect() {
        PackedRTree<String> tree = PackedRTree.<String>builder().add("a", 0, 0, 1, 1).build();
        assertEquals(1, tree.query(1, 0, 2, 1).size()); // shared edge
        assertEquals(1, tree.query(1, 1, 2, 2).size()); // shared corner
        assertEquals(1, tree.query(0.5, 0.5, 0.5, 0.5).size()); // degenerate (point) query box
        assertEquals(0, tree.query(1.0000001, 0, 2, 1).size());
    }

    @Test
    public void pointAndLineItemsAreSupported() {
        PackedRTree<String> tree = PackedRTree.<String>builder()
                .add("point", 5, 5, 5, 5)
                .add("horizontal", 0, 2, 10, 2)
                .build();
        assertEquals(Collections.singletonList("point"), tree.query(4, 4, 6, 6));
        assertEquals(Collections.singletonList("horizontal"), tree.query(3, 1, 4, 3));
    }

    @Test
    public void duplicateItemsAndBoxesAreAllReturned() {
        PackedRTree<String> tree = PackedRTree.<String>builder()
                .add("a", 0, 0, 1, 1)
                .add("a", 0, 0, 1, 1)
                .add("b", 0, 0, 1, 1)
                .build();
        List<String> hits = tree.query(0, 0, 1, 1);
        Collections.sort(hits);
        assertEquals(Arrays.asList("a", "a", "b"), hits);
    }

    @Test
    public void visitorCanStopTheQuery() {
        PackedRTree.Builder<Integer> builder = PackedRTree.builder();
        for (int i = 0; i < 1000; i++) builder.add(i, 0, 0, 1, 1);
        PackedRTree<Integer> tree = builder.build();

        int[] visited = {0};
        boolean completed = tree.query(0, 0, 1, 1, item -> ++visited[0] < 10);
        assertFalse(completed);
        assertEquals(10, visited[0]);

        visited[0] = 0;
        assertTrue(tree.query(0, 0, 1, 1, item -> { visited[0]++; return true; }));
        assertEquals(1000, visited[0]);
    }

    @Test
    public void builderCanBeReused() {
        PackedRTree.Builder<String> builder = PackedRTree.<String>builder().add("a", 0, 0, 1, 1);
        PackedRTree<String> first = builder.build();
        PackedRTree<String> second = builder.add("b", 5, 5, 6, 6).build();
        assertEquals(1, first.size());
        assertEquals(2, second.size());
        assertTrue(first.query(5, 5, 6, 6).isEmpty());
        assertEquals(Collections.singletonList("b"), second.query(5, 5, 6, 6));
    }

    @Test
    public void matchesBruteForceOnRandomBoxes() {
        Random rnd = new Random(1234L);
        for (int capacity : new int[]{2, 3, 4, 16, 50}) {
            for (int n : new int[]{1, 2, 15, 16, 17, 255, 256, 257, 5000}) {
                double[][] boxes = new double[n][];
                PackedRTree.Builder<Integer> builder = PackedRTree.builder(capacity);
                for (int i = 0; i < n; i++) {
                    boxes[i] = randomBox(rnd, 1000, 50);
                    builder.add(i, boxes[i][0], boxes[i][1], boxes[i][2], boxes[i][3]);
                }
                PackedRTree<Integer> tree = builder.build();
                assertEquals(n, tree.size());
                for (int q = 0; q < 200; q++) {
                    double[] query = randomBox(rnd, 1000, 200);
                    Set<Integer> expected = new HashSet<>();
                    for (int i = 0; i < n; i++) {
                        if (intersects(boxes[i], query)) expected.add(i);
                    }
                    List<Integer> actual = tree.query(query[0], query[1], query[2], query[3]);
                    assertEquals("duplicates returned", actual.size(), new HashSet<>(actual).size());
                    assertEquals("capacity " + capacity + ", n " + n, expected, new HashSet<>(actual));
                }
            }
        }
    }

    @Test
    public void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> PackedRTree.builder(1));
        PackedRTree.Builder<String> builder = PackedRTree.builder();
        assertThrows(NullPointerException.class, () -> builder.add(null, 0, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> builder.add("a", 1, 0, 0, 1)); // minX > maxX
        assertThrows(IllegalArgumentException.class, () -> builder.add("a", 0, 1, 1, 0)); // minY > maxY
        assertThrows(IllegalArgumentException.class, () -> builder.add("a", Double.NaN, 0, 1, 1));
        PackedRTree<String> tree = builder.add("a", 0, 0, 1, 1).build();
        assertThrows(IllegalArgumentException.class, () -> tree.query(1, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> tree.query(0, 0, Double.NaN, 1));
        assertThrows(NullPointerException.class, () -> tree.query(0, 0, 1, 1, null));
    }

    private static double[] randomBox(Random rnd, double extent, double maxSize) {
        double minX = rnd.nextDouble() * extent;
        double minY = rnd.nextDouble() * extent;
        // some boxes are degenerate (points or lines)
        double w = rnd.nextInt(10) == 0 ? 0 : rnd.nextDouble() * maxSize;
        double h = rnd.nextInt(10) == 0 ? 0 : rnd.nextDouble() * maxSize;
        return new double[]{minX, minY, minX + w, minY + h};
    }

    private static boolean intersects(double[] a, double[] b) {
        return a[2] >= b[0] && b[2] >= a[0] && a[3] >= b[1] && b[3] >= a[1];
    }
}
