package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.MinimumDistance;

import java.util.Arrays;

/**
 * The correctly rounded distance between two geometries, in the plane of the first two coordinates.
 *
 * <p>The distance is the smallest Euclidean distance between a point of one geometry and a point of the other. It is
 * zero if the geometries have a point in common, which includes a geometry that lies inside a polygon of the other
 * ({@link Relate#intersects}, exactly). Otherwise it is attained between a vertex of one geometry and a vertex or
 * segment of the other, and it is the square root of a rational function of the coordinates. The result is the
 * double nearest to the exact distance (round to nearest, ties to even), computed with {@link MinimumDistance}.</p>
 *
 * <p>The pairs of elements (points and segments) that can be nearest are found with a branch-and-bound search on
 * two packed R-trees, in order of the distance of their boxes. Distances are first estimated in floating point with a
 * generous, rigorous error bound, so that no pair that can be nearest is discarded; only the pairs that remain are
 * evaluated exactly.</p>
 *
 * <p>The distance between an empty geometry and any geometry is NaN.</p>
 */
public final class Distance {

    // the error bound of an estimated distance, relative to the sum of the magnitudes of the coordinate differences
    private static final double ESTIMATE_ERROR = 0x1p-40;
    // estimates are only made when the largest coordinate difference has a magnitude in this range
    private static final double MIN_MAGNITUDE = 0x1p-400;
    private static final double MAX_MAGNITUDE = 0x1p400;

    private Distance() {
    }

    /**
     * @return the distance between the geometries, correctly rounded; NaN if one of them is empty
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static double of(Geometry<?> geometry, Geometry<?> other) {
        if (geometry.isEmpty() || other.isEmpty()) return Double.NaN;
        if (Relate.intersects(geometry, other)) return 0;
        return new Search(new Elements(geometry), new Elements(other)).distance();
    }

    // ---------------------------------------------------------------------------------------------------------
    // elements
    // ---------------------------------------------------------------------------------------------------------

    /**
     * The points and segments of a geometry; a point is stored as a segment with equal endpoints
     */
    static final class Elements {
        double[] x0, y0, x1, y1;
        int size;

        Elements(Geometry<?> geometry) {
            int capacity = Math.max(1, geometry.getNumPositions());     // at least the number of elements
            x0 = new double[capacity];
            y0 = new double[capacity];
            x1 = new double[capacity];
            y1 = new double[capacity];
            collect(geometry);
        }

        private void collect(Geometry<?> geometry) {
            if (geometry.isEmpty()) return;
            if (geometry instanceof Point || geometry instanceof LineString) {
                line(GeometryElements.distinct(geometry.getPositions()));
            } else if (geometry instanceof Polygon) {
                Polygon<?> polygon = (Polygon<?>) geometry;
                line(GeometryElements.distinct(polygon.getExteriorRing().getPositions()));
                for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                    line(GeometryElements.distinct(polygon.getInteriorRingN(i).getPositions()));
                }
            } else if (geometry instanceof AbstractGeometryCollection) {
                AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
                for (int i = 0; i < collection.getNumGeometries(); i++) collect(collection.getGeometryN(i));
            }
        }

        private void line(double[][] c) {
            double[] xs = c[0], ys = c[1];
            if (xs.length == 1) {
                add(xs[0], ys[0], xs[0], ys[0]);
            }
            for (int i = 0; i + 1 < xs.length; i++) add(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }

        private void add(double ax, double ay, double bx, double by) {
            if (size == x0.length) {
                x0 = Arrays.copyOf(x0, 2 * size);
                y0 = Arrays.copyOf(y0, 2 * size);
                x1 = Arrays.copyOf(x1, 2 * size);
                y1 = Arrays.copyOf(y1, 2 * size);
            }
            x0[size] = ax;
            y0[size] = ay;
            x1[size] = bx;
            y1[size] = by;
            size++;
        }

        boolean isPoint(int i) {
            return x0[i] == x1[i] && y0[i] == y1[i];
        }

        EnvelopeTree tree() {
            double[] minX = new double[size], minY = new double[size], maxX = new double[size], maxY = new double[size];
            for (int i = 0; i < size; i++) {
                minX[i] = Math.min(x0[i], x1[i]);
                minY[i] = Math.min(y0[i], y1[i]);
                maxX[i] = Math.max(x0[i], x1[i]);
                maxY[i] = Math.max(y0[i], y1[i]);
            }
            return new EnvelopeTree(minX, minY, maxX, maxY);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // the search
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Branch and bound over pairs of tree nodes, in order of the (lower bound of the) distance of their boxes. The
     * search keeps an upper bound of the distance; a pair of nodes whose boxes are farther apart cannot contain the
     * nearest elements. For pairs of elements, the distances of the vertices to the other element are estimated;
     * those whose lower bound does not exceed the final upper bound are the candidates for the exact stage.
     */
    private static final class Search {
        // below this number of element pairs, all pairs are evaluated without building trees
        static final long BRUTE_FORCE_PAIRS = 256;
        final Elements a, b;
        EnvelopeTree ta, tb;
        double upper = Double.POSITIVE_INFINITY;

        // the candidates: a vertex (element, endpoint 0 or 1, of geometry a or b) and an element of the other
        // geometry, with the lower bound of their distance
        int[] vertex = new int[64], flags = new int[64], other = new int[64];
        double[] lower = new double[64];
        int candidates;
        static final int END = 1, FROM_B = 2;

        // the heap of node pairs: key, and level and index of the node in a and b
        double[] keys = new double[64];
        int[] nodes = new int[256];
        int heapSize;

        final double[] boxA = new double[4], boxB = new double[4];

        Search(Elements a, Elements b) {
            this.a = a;
            this.b = b;
        }

        double distance() {
            if (Math.min(a.size, b.size) == 1 || (long) a.size * b.size <= BRUTE_FORCE_PAIRS) {
                // a single point or segment against the other geometry, or two small geometries
                for (int i = 0; i < a.size; i++) {
                    for (int j = 0; j < b.size; j++) {
                        if (gap(i, j) <= upper) evaluate(i, j);
                    }
                }
            } else {
                search();
            }
            return exact();
        }

        private void search() {
            ta = a.tree();
            tb = b.tree();
            push(ta.height(), 0, tb.height(), 0);
            while (heapSize > 0) {
                if (keys[0] > upper) break;
                int la = nodes[0], ia = nodes[1], lb = nodes[2], ib = nodes[3];
                pop();
                if (la == 0 && lb == 0) {
                    evaluate(ia, ib);
                } else if (lb == 0 || (la > 0 && la >= lb)) {
                    for (int c = ta.firstChild(la, ia); c < ta.endChild(la, ia); c++) push(la - 1, c, lb, ib);
                } else {
                    for (int c = tb.firstChild(lb, ib); c < tb.endChild(lb, ib); c++) push(la, ia, lb - 1, c);
                }
            }
        }

        // the exact distance over the remaining candidates
        private double exact() {
            MinimumDistance minimum = new MinimumDistance();
            for (int k = 0; k < candidates; k++) {
                if (lower[k] > upper) continue;
                Elements p = (flags[k] & FROM_B) != 0 ? b : a, s = (flags[k] & FROM_B) != 0 ? a : b;
                int v = vertex[k], o = other[k];
                boolean end = (flags[k] & END) != 0;
                double px = end ? p.x1[v] : p.x0[v], py = end ? p.y1[v] : p.y0[v];
                if (s.isPoint(o)) {
                    minimum.addPointPoint(px, py, s.x0[o], s.y0[o]);
                } else {
                    minimum.addPointSegment(px, py, s.x0[o], s.y0[o], s.x1[o], s.y1[o]);
                }
            }
            return minimum.distance();
        }

        // the vertices of each element against the other element; a point needs only be compared with the other
        // element
        private void evaluate(int i, int j) {
            if (a.isPoint(i)) {
                estimate(a, i, 0, b, j);
            } else if (b.isPoint(j)) {
                estimate(b, j, FROM_B, a, i);
            } else {
                estimate(a, i, 0, b, j);
                estimate(a, i, END, b, j);
                estimate(b, j, FROM_B, a, i);
                estimate(b, j, FROM_B | END, a, i);
            }
        }

        // estimates the distance of a vertex of element v of p to element o of s, and records it as a candidate
        private void estimate(Elements p, int v, int flag, Elements s, int o) {
            boolean end = (flag & END) != 0;
            double px = end ? p.x1[v] : p.x0[v], py = end ? p.y1[v] : p.y0[v];
            double ax = s.x0[o], ay = s.y0[o], bx = s.x1[o], by = s.y1[o];
            double fx = px - ax, fy = py - ay, ex = bx - ax, ey = by - ay;
            double magnitude = Math.max(Math.max(Math.abs(fx), Math.abs(fy)), Math.max(Math.abs(ex), Math.abs(ey)));
            double d, error;
            if (magnitude >= MIN_MAGNITUDE && magnitude <= MAX_MAGNITUDE) {
                double t = fx * ex + fy * ey;
                if (t <= 0) {
                    d = Math.sqrt(fx * fx + fy * fy);
                } else {
                    double l2 = ex * ex + ey * ey;
                    if (t >= l2) {
                        double gx = px - bx, gy = py - by;
                        d = Math.sqrt(gx * gx + gy * gy);
                    } else {
                        d = Math.abs(ex * fy - ey * fx) / Math.sqrt(l2);
                    }
                }
                error = ESTIMATE_ERROR * (Math.abs(fx) + Math.abs(fy) + Math.abs(ex) + Math.abs(ey));
            } else {
                // no reliable estimate: always a candidate, and no upper bound
                d = 0;
                error = Double.POSITIVE_INFINITY;
            }
            double low = d - error;
            if (low > upper) return;
            if (d + error < upper) upper = d + error;
            if (candidates == lower.length) {
                int capacity = 2 * candidates;
                vertex = Arrays.copyOf(vertex, capacity);
                flags = Arrays.copyOf(flags, capacity);
                other = Arrays.copyOf(other, capacity);
                lower = Arrays.copyOf(lower, capacity);
            }
            vertex[candidates] = v;
            flags[candidates] = flag;
            other[candidates] = o;
            lower[candidates] = low;
            candidates++;
            if (candidates >= 1024 && candidates % 1024 == 0) compact();
        }

        // removes the candidates that can no longer be nearest
        private void compact() {
            int kept = 0;
            for (int k = 0; k < candidates; k++) {
                if (lower[k] > upper) continue;
                vertex[kept] = vertex[k];
                flags[kept] = flags[k];
                other[kept] = other[k];
                lower[kept] = lower[k];
                kept++;
            }
            candidates = kept;
        }

        // a lower bound of the distance between the boxes of element i of a and element j of b
        private double gap(int i, int j) {
            double gx = Math.max(Math.min(b.x0[j], b.x1[j]) - Math.max(a.x0[i], a.x1[i]),
                    Math.min(a.x0[i], a.x1[i]) - Math.max(b.x0[j], b.x1[j]));
            double gy = Math.max(Math.min(b.y0[j], b.y1[j]) - Math.max(a.y0[i], a.y1[i]),
                    Math.min(a.y0[i], a.y1[i]) - Math.max(b.y0[j], b.y1[j]));
            return Math.max(gx, gy) * (1 - 0x1p-40);
        }

        // a lower bound of the distance between the boxes of the nodes
        private double boxDistance(int la, int ia, int lb, int ib) {
            ta.box(la, ia, boxA, 0);
            tb.box(lb, ib, boxB, 0);
            double dx = Math.max(0, Math.max(boxB[0] - boxA[2], boxA[0] - boxB[2]));
            double dy = Math.max(0, Math.max(boxB[1] - boxA[3], boxA[1] - boxB[3]));
            return Math.sqrt(dx * dx + dy * dy) * (1 - 0x1p-40);
        }

        private void push(int la, int ia, int lb, int ib) {
            double key = boxDistance(la, ia, lb, ib);
            if (key > upper) return;
            if (heapSize == keys.length) {
                keys = Arrays.copyOf(keys, 2 * heapSize);
                nodes = Arrays.copyOf(nodes, 8 * heapSize);
            }
            int i = heapSize++;
            while (i > 0) {
                int parent = (i - 1) >>> 1;
                if (keys[parent] <= key) break;
                move(parent, i);
                i = parent;
            }
            set(i, key, la, ia, lb, ib);
        }

        private void pop() {
            heapSize--;
            if (heapSize == 0) return;
            int last = heapSize;
            double key = keys[last];
            int la = nodes[4 * last], ia = nodes[4 * last + 1], lb = nodes[4 * last + 2], ib = nodes[4 * last + 3];
            int i = 0;
            while (true) {
                int child = 2 * i + 1;
                if (child >= heapSize) break;
                if (child + 1 < heapSize && keys[child + 1] < keys[child]) child++;
                if (keys[child] >= key) break;
                move(child, i);
                i = child;
            }
            set(i, key, la, ia, lb, ib);
        }

        private void move(int from, int to) {
            keys[to] = keys[from];
            System.arraycopy(nodes, 4 * from, nodes, 4 * to, 4);
        }

        private void set(int i, double key, int la, int ia, int lb, int ib) {
            keys[i] = key;
            nodes[4 * i] = la;
            nodes[4 * i + 1] = ia;
            nodes[4 * i + 2] = lb;
            nodes[4 * i + 3] = ib;
        }
    }
}
