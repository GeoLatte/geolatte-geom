/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2026 and Ownership of code is shared by:
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.cga;

import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.index.PackedRTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the simplicity of the curve defined by a {@code PositionSequence} with linear interpolation between
 * consecutive positions (i.e. a {@code LineString}).
 *
 * <p>Following ISO/IEC 13249-3 (SQL/MM Spatial) and the OGC Simple Features specification, such a curve is simple
 * if it does not pass through the same point twice, except that the start and end point of a closed curve may
 * coincide. Only X and Y coordinates are considered; Z and M coordinate values are ignored. Consecutive positions
 * that coincide in X and Y (zero-length segments) do not affect simplicity.</p>
 *
 * <p><b>Algorithm.</b> The curve is partitioned into <em>monotone chains</em>: maximal runs of consecutive
 * segments that all point into the same quadrant, so that X and Y are both monotone along the chain. A monotone chain cannot intersect
 * itself, and the bounding box of any run of its segments is spanned by the first and last vertex of that run.</p>
 * <ol>
 *     <li>The chains are bulk-loaded into a {@link PackedRTree}.</li>
 *     <li>Every chain is compared only with the later chains whose bounding boxes overlap its own.</li>
 *     <li>Within such a pair of chains, the chains are recursively bisected, discarding halves whose bounding
 *     boxes do not overlap, until single segments remain, which are then tested exactly.</li>
 * </ol>
 * <p>This is the approach of JTS' {@code MonotoneChain} noding, and is O(n log n) in the number of positions
 * for typical curves. The orientation tests use the error-compensated determinant of
 * {@link NumericalMethods#determinant(double, double, double, double, double, double, double, double, double)}.</p>
 *
 * <p>This class does not depend on JTS.</p>
 */
public final class LineStringSimplicity {

    private LineStringSimplicity() {
    }

    /**
     * Determines whether the curve with linear interpolation between the specified positions is simple.
     *
     * @param positions the positions of the curve
     * @return true if the curve is simple. An empty sequence is simple.
     */
    public static boolean isSimple(PositionSequence<?> positions) {
        return new Checker(positions).isSimple();
    }

    /**
     * Holds the de-duplicated X/Y coordinates of the curve. Segment i runs from vertex i to vertex i + 1.
     */
    private static final class Checker {
        private final double[] xs;
        private final double[] ys;
        private final int numSegments;
        private boolean closed;

        Checker(PositionSequence<?> positions) {
            int size = positions.size();
            double[] x = new double[size];
            double[] y = new double[size];
            double[] buffer = new double[Math.max(2, positions.getCoordinateDimension())];
            int n = 0;
            for (int i = 0; i < size; i++) {
                positions.getCoordinates(i, buffer);
                // skip positions that repeat the previous one in X and Y (as does JTS's IsSimpleOp in
                // org.locationtech.jts.operation; the newer org.locationtech.jts.operation.valid.IsSimpleOp does not)
                if (n > 0 && x[n - 1] == buffer[0] && y[n - 1] == buffer[1]) continue;
                x[n] = buffer[0];
                y[n] = buffer[1];
                n++;
            }
            this.xs = x;
            this.ys = y;
            this.numSegments = Math.max(0, n - 1);
        }

        boolean isSimple() {
            if (numSegments < 2) return true;
            closed = xs[0] == xs[numSegments] && ys[0] == ys[numSegments];
            List<Chain> chains = monotoneChains();
            if (chains.size() == 1) return true; // a monotone chain cannot intersect itself

            PackedRTree.Builder<Chain> builder = PackedRTree.builder();
            for (Chain chain : chains) {
                builder.add(chain, chain.minX, chain.minY, chain.maxX, chain.maxY);
            }
            PackedRTree<Chain> index = builder.build();
            for (Chain chain : chains) {
                // compare with the later chains whose bounding boxes overlap; stop at the first violation
                boolean allowed = index.query(chain.minX, chain.minY, chain.maxX, chain.maxY,
                        other -> other.index <= chain.index
                                || hasOnlyAllowedInteractions(chain.start, chain.end, other.start, other.end));
                if (!allowed) return false;
            }
            return true;
        }

        /**
         * Partitions the curve into maximal monotone chains.
         */
        private List<Chain> monotoneChains() {
            List<Chain> chains = new ArrayList<>();
            int start = 0;
            int quadrant = quadrant(0);
            for (int s = 1; s < numSegments; s++) {
                int q = quadrant(s);
                if (q != quadrant) {
                    chains.add(new Chain(chains.size(), start, s, xs, ys));
                    start = s;
                    quadrant = q;
                }
            }
            chains.add(new Chain(chains.size(), start, numSegments, xs, ys));
            return chains;
        }

        /**
         * Returns the quadrant (0..3) into which segment s points. Vertical and horizontal segments are assigned
         * consistently, so that within a chain the X and Y coordinates are (non-strictly) monotone.
         */
        private int quadrant(int s) {
            double dx = xs[s + 1] - xs[s];
            double dy = ys[s + 1] - ys[s];
            if (dx >= 0) return dy >= 0 ? 0 : 3;
            return dy >= 0 ? 1 : 2;
        }

        /**
         * Checks the parts [a0, a1] and [b0, b1] (vertex indices, a1 <= b0) of two monotone chains by recursive
         * bisection. Because the parts are monotone, their bounding boxes are spanned by their end vertices.
         */
        private boolean hasOnlyAllowedInteractions(int a0, int a1, int b0, int b1) {
            if (!rangesOverlap(a0, a1, b0, b1)) return true;
            int aSegments = a1 - a0;
            int bSegments = b1 - b0;
            if (aSegments == 1 && bSegments == 1) return isAllowedInteraction(a0, b0);
            if (aSegments >= bSegments) {
                int mid = (a0 + a1) >>> 1;
                return hasOnlyAllowedInteractions(a0, mid, b0, b1) && hasOnlyAllowedInteractions(mid, a1, b0, b1);
            }
            int mid = (b0 + b1) >>> 1;
            return hasOnlyAllowedInteractions(a0, a1, b0, mid) && hasOnlyAllowedInteractions(a0, a1, mid, b1);
        }

        private boolean rangesOverlap(int a0, int a1, int b0, int b1) {
            return Math.max(xs[a0], xs[a1]) >= Math.min(xs[b0], xs[b1])
                    && Math.max(xs[b0], xs[b1]) >= Math.min(xs[a0], xs[a1])
                    && Math.max(ys[a0], ys[a1]) >= Math.min(ys[b0], ys[b1])
                    && Math.max(ys[b0], ys[b1]) >= Math.min(ys[a0], ys[a1]);
        }

        /**
         * Checks whether segments first < second intersect only in a way that is allowed for a simple curve.
         */
        private boolean isAllowedInteraction(int first, int second) {
            if (second == first + 1) {
                // Consecutive segments share vertex first + 1. This also covers a closed curve of 2 segments
                // (A -> B -> A), which always doubles back on itself.
                return !doublesBack(first, second);
            }
            if (closed && first == 0 && second == numSegments - 1) {
                // the last segment ends where the first one starts
                return !doublesBack(second, first);
            }
            return !intersects(first, second);
        }

        /**
         * Tests whether segment {@code next}, which starts where segment {@code prev} ends, is collinear with and
         * points back over {@code prev}. These are the only ways that two consecutive, non-degenerate segments can
         * intersect in more than their shared vertex.
         */
        private boolean doublesBack(int prev, int next) {
            // cheap test first: the segments must point in opposite directions
            double dot = (xs[prev + 1] - xs[prev]) * (xs[next + 1] - xs[next])
                    + (ys[prev + 1] - ys[prev]) * (ys[next + 1] - ys[next]);
            if (dot >= 0) return false;
            return orientation(xs[prev], ys[prev], xs[prev + 1], ys[prev + 1], xs[next + 1], ys[next + 1]) == 0;
        }

        /**
         * Tests whether two closed segments have at least one point in common.
         */
        private boolean intersects(int s1, int s2) {
            double p1x = xs[s1], p1y = ys[s1], q1x = xs[s1 + 1], q1y = ys[s1 + 1];
            double p2x = xs[s2], p2y = ys[s2], q2x = xs[s2 + 1], q2y = ys[s2 + 1];
            double d1 = orientation(p2x, p2y, q2x, q2y, p1x, p1y);
            double d2 = orientation(p2x, p2y, q2x, q2y, q1x, q1y);
            double d3 = orientation(p1x, p1y, q1x, q1y, p2x, p2y);
            double d4 = orientation(p1x, p1y, q1x, q1y, q2x, q2y);
            if (strictlyOpposite(d1, d2) && strictlyOpposite(d3, d4)) return true;
            return (d1 == 0 && inBounds(s2, p1x, p1y))
                    || (d2 == 0 && inBounds(s2, q1x, q1y))
                    || (d3 == 0 && inBounds(s1, p2x, p2y))
                    || (d4 == 0 && inBounds(s1, q2x, q2y));
        }

        private static boolean strictlyOpposite(double o1, double o2) {
            return (o1 > 0 && o2 < 0) || (o1 < 0 && o2 > 0);
        }

        /**
         * Tests whether a point that is known to be collinear with segment s lies within its bounds.
         */
        private boolean inBounds(int s, double x, double y) {
            return Math.min(xs[s], xs[s + 1]) <= x && x <= Math.max(xs[s], xs[s + 1])
                    && Math.min(ys[s], ys[s + 1]) <= y && y <= Math.max(ys[s], ys[s + 1]);
        }

        /**
         * Returns a value that is positive if (x0, y0), (x1, y1), (x2, y2) is counterclockwise, negative if it is
         * clockwise, and zero if the three points are collinear. Same computation as
         * {@link NumericalMethods#orientation(org.geolatte.geom.Position, org.geolatte.geom.Position,
         * org.geolatte.geom.Position)}.
         */
        private static double orientation(double x0, double y0, double x1, double y1, double x2, double y2) {
            return NumericalMethods.determinant(1, 1, 1, x0, x1, x2, y0, y1, y2);
        }
    }

    /**
     * A monotone chain, consisting of the segments from vertex {@code start} up to vertex {@code end}.
     */
    private static final class Chain {
        final int index;
        final int start;
        final int end;
        final double minX, maxX, minY, maxY;

        Chain(int index, int start, int end, double[] xs, double[] ys) {
            this.index = index;
            this.start = start;
            this.end = end;
            // the chain is monotone, so its bounding box is spanned by its end vertices
            this.minX = Math.min(xs[start], xs[end]);
            this.maxX = Math.max(xs[start], xs[end]);
            this.minY = Math.min(ys[start], ys[end]);
            this.maxY = Math.max(ys[start], ys[end]);
        }
    }
}
