package org.geolatte.geom.cga;

import org.geolatte.geom.SegmentIntersection;
import org.geolatte.geom.cga.exact.Constructions;
import org.geolatte.geom.cga.exact.Predicates;
import org.geolatte.geom.cga.exact.RoundingCell;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SnapRounderTest {

    @Test
    public void chainsWithoutContactsAreUnchanged() {
        List<double[][]> chains = Arrays.asList(
                chain(0, 0, 1, 0, 1, 1, 0, 0),
                chain(5, 5, 6, 7, 8, 5));
        List<SnapRounder.Chain> snapped = SnapRounder.snap(chains);
        for (int k = 0; k < chains.size(); k++) {
            assertArrayEquals(chains.get(k)[0], snapped.get(k).xs, 0);
            assertArrayEquals(chains.get(k)[1], snapped.get(k).ys, 0);
        }
    }

    @Test
    public void crossingSegmentsShareTheRoundedCrossing() {
        List<double[][]> chains = Arrays.asList(chain(0, 0, 3, 1), chain(0, 1, 3, 0));
        List<SnapRounder.Chain> snapped = SnapRounder.snap(chains);
        double[] x = Constructions.crossingPoint(0, 0, 3, 1, 0, 1, 3, 0);
        for (SnapRounder.Chain c : snapped) {
            assertEquals(3, c.size());
            assertEquals(x[0], c.xs[1], 0);
            assertEquals(x[1], c.ys[1], 0);
        }
        checkProperties(chains, snapped);
    }

    @Test
    public void aSegmentPassingThroughTheCellOfAVertexIsSnappedToIt() {
        // the vertex (1, 1 + u) (u = 2^-52) is not on the segment (0, 0) - (3, 3 + 4u), which passes through
        // (1, 1 + 4u/3), inside the cell of the vertex
        double u = 0x1p-52, y = 1 + u;
        List<double[][]> chains = Arrays.asList(chain(0, 0, 3, 3 + 4 * u), chain(1, y, 1, 5));
        assertNotEquals(0, Predicates.orientation(0, 0, 3, 3 + 4 * u, 1, y));
        List<SnapRounder.Chain> snapped = SnapRounder.snap(chains);
        assertArrayEquals(new double[]{0, 1, 3}, snapped.get(0).xs, 0);
        assertArrayEquals(new double[]{0, y, 3 + 4 * u}, snapped.get(0).ys, 0);
        checkProperties(chains, snapped);
        // the segment (0, 0) - (2, 2) passes only through the corner of that cell, which is open: not snapped
        chains = Arrays.asList(chain(0, 0, 2, 2), chain(1, y, 1, 5));
        assertEquals(2, SnapRounder.snap(chains).get(0).size());
    }

    @Test
    public void vertexOnASegmentAndOverlapsAreInserted() {
        // a T-junction, and a collinear overlap
        List<double[][]> chains = Arrays.asList(chain(0, 0, 4, 0), chain(2, 0, 2, 3), chain(3, 0, 6, 0));
        List<SnapRounder.Chain> snapped = SnapRounder.snap(chains);
        assertArrayEquals(new double[]{0, 2, 3, 4}, snapped.get(0).xs, 0);
        assertArrayEquals(new double[]{3, 4, 6}, snapped.get(2).xs, 0);
        checkProperties(chains, snapped);
    }

    @Test
    public void randomTinyLinesAtLargeCoordinates() {
        // many crossings and near passes within a few ulps
        Random random = new Random(101);
        for (int k = 0; k < 300; k++) {
            double ox = 152000 + random.nextDouble(), oy = 212000 + random.nextDouble();
            double size = Math.ulp(ox) * (1 + random.nextInt(64));
            List<double[][]> chains = new ArrayList<>();
            for (int c = 0; c < 2 + random.nextInt(4); c++) chains.add(randomChain(random, ox, oy, size, 2 + random.nextInt(6)));
            chains.removeIf(c -> c[0].length < 2);
            checkProperties(chains, SnapRounder.snap(chains));
        }
    }

    @Test
    public void randomLines() {
        Random random = new Random(103);
        for (int k = 0; k < 300; k++) {
            double ox = random.nextBoolean() ? 0 : 152000, oy = random.nextBoolean() ? 0 : 212000;
            double size = Math.scalb(1.0, random.nextInt(20) - 10);
            List<double[][]> chains = new ArrayList<>();
            for (int c = 0; c < 1 + random.nextInt(4); c++) chains.add(randomChain(random, ox, oy, size, 2 + random.nextInt(20)));
            checkProperties(chains, SnapRounder.snap(chains));
        }
    }

    @Test
    public void manySegmentsThroughOnePoint() {
        // a pencil of segments through (0.1, 0.1): the crossings round to nearby doubles
        Random random = new Random(107);
        List<double[][]> chains = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            double angle = Math.PI * random.nextDouble();
            double dx = Math.cos(angle), dy = Math.sin(angle);
            chains.add(chain(0.1 - dx, 0.1 - dy, 0.1 + dx, 0.1 + dy));
        }
        checkProperties(chains, SnapRounder.snap(chains));
    }

    @Test
    public void fixedGridMovesVerticesToGridPoints() {
        Grid unit = Grid.fixed(1);
        // a crossing at (0.6, 0.6) rounds to (1, 1); both segments are routed through it
        List<double[][]> chains = new ArrayList<>();
        chains.add(chain(-0.4, -0.4, 1.6, 1.6));
        chains.add(chain(-0.4, 1.6, 1.6, -0.4));
        List<SnapRounder.Chain> snapped = SnapRounder.snap(chains, unit);
        assertArrayEquals(new double[]{0, 1, 2}, snapped.get(0).xs, 0);
        assertArrayEquals(new double[]{0, 1, 2}, snapped.get(0).ys, 0);
        assertArrayEquals(new double[]{0, 1, 2}, snapped.get(1).xs, 0);
        assertArrayEquals(new double[]{2, 1, 0}, snapped.get(1).ys, 0);
        // a chain within one cell collapses to a single vertex
        List<double[][]> small = new ArrayList<>();
        small.add(chain(0.1, 0.1, 0.3, 0.2, 0.2, 0.4));
        snapped = SnapRounder.snap(small, unit);
        assertEquals(1, snapped.get(0).size());
        checkFixedProperties(chains, SnapRounder.snap(chains, unit), unit);
    }

    @Test
    public void randomLinesOnFixedGrids() {
        Random random = new Random(109);
        String[] sizes = {"0.001", "0.5", "3", "7", "0.03"};
        for (int k = 0; k < 600; k++) {
            Grid grid = Grid.fixed(sizes[k % sizes.length]);
            double g = grid.cellSize().doubleValue();
            double ox = random.nextBoolean() ? 0 : 152000, oy = random.nextBoolean() ? 0 : 212000;
            double extent = g * (2 + random.nextInt(40));
            List<double[][]> chains = new ArrayList<>();
            for (int c = 0; c < 1 + random.nextInt(5); c++) {
                chains.add(randomChain(random, ox, oy, extent, 2 + random.nextInt(15)));
            }
            chains.removeIf(c -> c[0].length < 2);
            checkFixedProperties(chains, SnapRounder.snap(chains, grid), grid);
        }
    }

    @Test
    public void manySegmentsThroughOnePointOnAFixedGrid() {
        Random random = new Random(113);
        Grid grid = Grid.fixed("0.001");
        List<double[][]> chains = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            double angle = Math.PI * random.nextDouble();
            double dx = Math.cos(angle) * 0.01, dy = Math.sin(angle) * 0.01;
            chains.add(chain(0.1 - dx, 0.1 - dy, 0.1 + dx, 0.1 + dy));
        }
        checkFixedProperties(chains, SnapRounder.snap(chains, grid), grid);
    }

    /**
     * The properties of chains snapped to a fixed grid: (1) as for the double grid, they never cross, and meet only
     * in common vertices or in identical segments; (2) all vertices are grid points of hot pixels (of input
     * vertices or of crossings of input segments); (3) they start and end at the grid points of the original ends,
     * pass through the grid points of the original vertices in order, and in between through grid points whose
     * cells the original segment meets, except for vertices inserted because they lie exactly on the snapped
     * segment.
     */
    static void checkFixedProperties(List<double[][]> chains, List<SnapRounder.Chain> snapped, Grid grid) {
        Set<List<Double>> hot = new HashSet<>();
        List<double[]> segments = new ArrayList<>();
        for (double[][] c : chains) {
            for (int i = 0; i < c[0].length; i++) hot.add(Arrays.asList(grid.round(c[0][i]), grid.round(c[1][i])));
            for (int i = 0; i + 1 < c[0].length; i++) segments.add(new double[]{c[0][i], c[1][i], c[0][i + 1], c[1][i + 1]});
        }
        for (int i = 0; i < segments.size(); i++) {
            for (int j = i + 1; j < segments.size(); j++) {
                double[] s = segments.get(i), t = segments.get(j);
                if (Predicates.segmentIntersection(s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3])
                        == SegmentIntersection.CROSSING) {
                    double[] p = grid.roundCrossing(s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3]);
                    hot.add(Arrays.asList(p[0], p[1]));
                }
            }
        }
        for (int k = 0; k < chains.size(); k++) {
            SnapRounder.Chain c = snapped.get(k);
            double[] xs = chains.get(k)[0], ys = chains.get(k)[1];
            assertEquals(grid.round(xs[0]), c.xs[0], 0);
            assertEquals(grid.round(ys[ys.length - 1]), c.ys[c.size() - 1], 0);
            int original = 0;
            for (int v = 0; v < c.size(); v++) {
                assertTrue("not a hot pixel", hot.contains(Arrays.asList(c.xs[v], c.ys[v])));
                assertEquals(c.xs[v], grid.round(c.xs[v]), 0);
                assertEquals(c.ys[v], grid.round(c.ys[v]), 0);
                // skip the original vertices that round to this vertex
                boolean isOriginal = false;
                while (original < xs.length && grid.round(xs[original]) == c.xs[v]
                        && grid.round(ys[original]) == c.ys[v]) {
                    original++;
                    isOriginal = true;
                }
                if (isOriginal) continue;
                int s = c.origin[v];
                boolean met = grid.intersects(xs[s], ys[s], xs[s + 1], ys[s + 1], c.xs[v], c.ys[v]);
                boolean onSnapped = v > 0 && v + 1 < c.size();
                assertTrue("vertex not near its segment", met || onSnapped);
            }
            assertEquals("all original vertices in order", xs.length, original);
        }
        List<double[][]> list = new ArrayList<>();
        for (SnapRounder.Chain c : snapped) {
            if (c.size() > 1) list.add(new double[][]{c.xs, c.ys});
        }
        checkContacts(list);
    }

    // -------------------------------------------------------------------------------------------------------

    private static double[][] chain(double... c) {
        double[] xs = new double[c.length / 2], ys = new double[c.length / 2];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = c[2 * i];
            ys[i] = c[2 * i + 1];
        }
        return new double[][]{xs, ys};
    }

    private static double[][] randomChain(Random random, double ox, double oy, double size, int n) {
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = ox + size * random.nextDouble();
            ys[i] = oy + size * random.nextDouble();
        }
        return GeometryElements.distinct(xs, ys);
    }

    /**
     * The snapped chains: (1) never cross, and meet only in common vertices or in identical segments; (2) consist
     * of the original vertices and hot pixel points (input vertices and rounded crossings of input segments); (3)
     * pass from each original vertex to the next through points whose cells the original segment meets, in
     * order, except for vertices inserted because they lie exactly on the snapped segment.
     */
    static void checkProperties(List<double[][]> chains, List<SnapRounder.Chain> snapped) {
        chains.removeIf(c -> c[0].length < 2);
        // (2) the hot pixel points, computed by brute force
        Set<List<Double>> hot = new HashSet<>();
        List<double[]> segments = new ArrayList<>();
        for (double[][] c : chains) {
            for (int i = 0; i < c[0].length; i++) hot.add(Arrays.asList(c[0][i] + 0.0, c[1][i] + 0.0));
            for (int i = 0; i + 1 < c[0].length; i++) segments.add(new double[]{c[0][i], c[1][i], c[0][i + 1], c[1][i + 1]});
        }
        for (int i = 0; i < segments.size(); i++) {
            for (int j = i + 1; j < segments.size(); j++) {
                double[] s = segments.get(i), t = segments.get(j);
                if (Predicates.segmentIntersection(s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3])
                        == SegmentIntersection.CROSSING) {
                    double[] p = Constructions.crossingPoint(s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3]);
                    hot.add(Arrays.asList(p[0], p[1]));
                }
            }
        }
        for (int k = 0; k < chains.size(); k++) {
            SnapRounder.Chain c = snapped.get(k);
            double[] xs = chains.get(k)[0], ys = chains.get(k)[1];
            assertEquals(xs[0], c.xs[0], 0);
            assertEquals(xs[xs.length - 1], c.xs[c.size() - 1], 0);
            int original = 0;
            for (int v = 0; v < c.size(); v++) {
                assertTrue("not a hot pixel", hot.contains(Arrays.asList(c.xs[v], c.ys[v])));
                boolean last = v == c.size() - 1 && original == xs.length - 1;
                if (original < xs.length && c.xs[v] == xs[original] && c.ys[v] == ys[original]
                        && (last || c.origin[v] == original)) {
                    original++;
                    continue;
                }
                // (3) a vertex between original vertices origin and origin + 1
                int s = c.origin[v];
                assertEquals(original - 1, s);
                boolean met = RoundingCell.intersects(xs[s], ys[s], xs[s + 1], ys[s + 1], c.xs[v], c.ys[v]);
                boolean onSnapped = v > 0 && v + 1 < c.size();
                assertTrue("vertex not near its segment", met || onSnapped);
            }
            assertEquals("all original vertices in order", xs.length, original);
        }
        // (1) the contacts of the snapped chains
        List<double[][]> list = new ArrayList<>();
        for (SnapRounder.Chain c : snapped) list.add(new double[][]{c.xs, c.ys});
        checkContacts(list);
    }

    private static void checkContacts(List<double[][]> list) {
        SegmentContacts.find(list, (a, i, b, j, kind) -> {
            double[][] ca = list.get(a), cb = list.get(b);
            double ax = ca[0][i], ay = ca[1][i], bx = ca[0][i + 1], by = ca[1][i + 1];
            double cx = cb[0][j], cy = cb[1][j], dx = cb[0][j + 1], dy = cb[1][j + 1];
            assertNotEquals("snapped segments cross", SegmentIntersection.CROSSING, kind);
            boolean sharedEnd = (ax == cx && ay == cy) || (ax == dx && ay == dy) || (bx == cx && by == cy)
                    || (bx == dx && by == dy);
            if (kind == SegmentIntersection.TOUCHING) {
                // they touch in a common vertex
                assertTrue("touch outside common vertices", sharedEnd && !(SegmentContacts.onSegment(ax, ay, bx, by, cx, cy)
                        && !(cx == ax && cy == ay) && !(cx == bx && cy == by))
                        && !(SegmentContacts.onSegment(ax, ay, bx, by, dx, dy) && !(dx == ax && dy == ay) && !(dx == bx && dy == by))
                        && !(SegmentContacts.onSegment(cx, cy, dx, dy, ax, ay) && !(ax == cx && ay == cy) && !(ax == dx && ay == dy))
                        && !(SegmentContacts.onSegment(cx, cy, dx, dy, bx, by) && !(bx == cx && by == cy) && !(bx == dx && by == dy)));
            } else {
                // they overlap entirely
                boolean same = (ax == cx && ay == cy && bx == dx && by == dy) || (ax == dx && ay == dy && bx == cx && by == cy);
                assertTrue("partial overlap", same);
            }
            return false;
        });
    }
}
