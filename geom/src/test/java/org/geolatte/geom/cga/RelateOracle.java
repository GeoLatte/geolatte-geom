package org.geolatte.geom.cga;

import org.locationtech.jts.geom.*;

import java.math.BigInteger;
import java.util.*;

/**
 * A brute force, exact (rational) computation of the DE-9IM matrix with union semantics, independent of
 * {@link RelateComputer}, for testing. Intended for small geometries with small integer coordinates.
 *
 * <p>The plane is partitioned into cells by all segments of both geometries: nodes (vertices, points and
 * intersection points), the open pieces of segments between nodes, and faces. Each cell is represented by a sample
 * point (a node, the midpoint of a piece, or a point at a tiny distance beside the midpoint of a piece), whose
 * location in each geometry is computed directly from the definition of union semantics.</p>
 */
final class RelateOracle {

    private static final Q EPSILON = new Q(BigInteger.ONE, BigInteger.TEN.pow(12));
    private static final Q ETA = new Q(BigInteger.ONE, BigInteger.TEN.pow(6));

    static String relate(Geometry a, Geometry b) {
        Elements ea = new Elements(a), eb = new Elements(b);
        List<P[]> segments = new ArrayList<>();
        segments.addAll(ea.segments());
        segments.addAll(eb.segments());
        Set<P> nodes = new LinkedHashSet<>();
        nodes.addAll(ea.vertices());
        nodes.addAll(eb.vertices());
        for (int i = 0; i < segments.size(); i++) {
            for (int j = i + 1; j < segments.size(); j++) {
                P x = crossing(segments.get(i), segments.get(j));
                if (x != null) nodes.add(x);
            }
        }
        int[] m = new int[9];
        Arrays.fill(m, -1);
        m[8] = 2;
        for (P n : nodes) set(m, ea.locate(n), eb.locate(n), 0);
        for (P[] s : segments) {
            List<P> on = new ArrayList<>();
            for (P n : nodes) {
                if (onSegment(n, s[0], s[1])) on.add(n);
            }
            P d = s[1].sub(s[0]);
            on.sort(Comparator.comparing(n -> n.sub(s[0]).dot(d)));
            for (int i = 0; i + 1 < on.size(); i++) {
                P mid = on.get(i).add(on.get(i + 1)).scale(new Q(1, 2));
                set(m, ea.locate(mid), eb.locate(mid), 1);
                // beside the piece, at a distance relative to its length, so that tiny faces are not skipped
                P offset = on.get(i + 1).sub(on.get(i)).perp().scale(EPSILON);
                P left = mid.add(offset), right = mid.sub(offset);
                set(m, ea.locate(left), eb.locate(left), 2);
                set(m, ea.locate(right), eb.locate(right), 2);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int e : m) sb.append(e < 0 ? 'F' : (char) ('0' + e));
        return sb.toString();
    }

    private static void set(int[] m, int a, int b, int dim) {
        m[3 * a + b] = Math.max(m[3 * a + b], dim);
    }

    private static final int I = 0, B = 1, E = 2;

    /**
     * The elements of a geometry, with exact union semantics location
     */
    static final class Elements {
        final List<P> points = new ArrayList<>();
        final List<List<P>> lines = new ArrayList<>();
        final List<List<List<P>>> polygons = new ArrayList<>();   // rings of each polygon

        Elements(Geometry g) {
            collect(g);
        }

        private void collect(Geometry g) {
            if (g.isEmpty()) return;
            if (g instanceof Point) {
                points.add(P.of(g.getCoordinate()));
            } else if (g instanceof LineString) {
                List<P> line = distinct(g.getCoordinates());
                if (line.size() == 1) points.add(line.get(0));
                else lines.add(line);
            } else if (g instanceof Polygon) {
                Polygon p = (Polygon) g;
                List<List<P>> rings = new ArrayList<>();
                rings.add(distinct(p.getExteriorRing().getCoordinates()));
                for (int i = 0; i < p.getNumInteriorRing(); i++) {
                    rings.add(distinct(p.getInteriorRingN(i).getCoordinates()));
                }
                polygons.add(rings);
            } else {
                for (int i = 0; i < g.getNumGeometries(); i++) collect(g.getGeometryN(i));
            }
        }

        private static List<P> distinct(Coordinate[] coordinates) {
            List<P> result = new ArrayList<>();
            for (Coordinate c : coordinates) {
                P p = P.of(c);
                if (result.isEmpty() || !result.get(result.size() - 1).equals(p)) result.add(p);
            }
            return result;
        }

        List<P[]> segments() {
            List<P[]> result = new ArrayList<>();
            for (List<P> line : lines) addSegments(line, result);
            for (List<List<P>> polygon : polygons) {
                for (List<P> ring : polygon) addSegments(ring, result);
            }
            return result;
        }

        private static void addSegments(List<P> chain, List<P[]> result) {
            for (int i = 0; i + 1 < chain.size(); i++) result.add(new P[]{chain.get(i), chain.get(i + 1)});
        }

        Set<P> vertices() {
            Set<P> result = new LinkedHashSet<>(points);
            for (List<P> line : lines) result.addAll(line);
            for (List<List<P>> polygon : polygons) {
                for (List<P> ring : polygon) result.addAll(ring);
            }
            return result;
        }

        int locate(P p) {
            boolean onArea = false;
            for (List<List<P>> polygon : polygons) {
                int l = locateInPolygon(p, polygon);
                if (l == I) return I;
                if (l == B) onArea = true;
            }
            if (onArea) return coveredAround(p) ? I : B;
            boolean onLine = false;
            int endpoints = 0;
            for (List<P> line : lines) {
                for (int i = 0; i + 1 < line.size(); i++) {
                    if (onSegment(p, line.get(i), line.get(i + 1))) onLine = true;
                }
                if (line.get(0).equals(p)) endpoints++;
                if (line.get(line.size() - 1).equals(p)) endpoints++;
            }
            if (onLine) return endpoints % 2 == 1 ? B : I;
            return points.contains(p) ? I : E;
        }

        // p lies on the boundary of some polygons and in no polygon interior: is every sector around p covered by
        // a polygon?
        private boolean coveredAround(P p) {
            List<P> directions = new ArrayList<>();
            for (List<List<P>> polygon : polygons) {
                for (List<P> ring : polygon) {
                    for (int i = 0; i + 1 < ring.size(); i++) {
                        P u = ring.get(i), v = ring.get(i + 1);
                        if (!onSegment(p, u, v)) continue;
                        if (!p.equals(v)) directions.add(v.sub(u));
                        if (!p.equals(u)) directions.add(u.sub(v));
                    }
                }
            }
            for (P d : directions) {
                // a point in the sector that follows direction d (counterclockwise)
                P sample = p.add(d.add(d.perp().scale(ETA)).scale(EPSILON));
                boolean covered = false;
                for (List<List<P>> polygon : polygons) covered |= locateInPolygon(sample, polygon) != E;
                if (!covered) return false;
            }
            return true;
        }

        private static int locateInPolygon(P p, List<List<P>> rings) {
            boolean inside = false;
            for (List<P> ring : rings) {
                for (int i = 0; i + 1 < ring.size(); i++) {
                    P u = ring.get(i), v = ring.get(i + 1);
                    if (onSegment(p, u, v)) return B;
                    if ((u.y.compareTo(p.y) > 0) != (v.y.compareTo(p.y) > 0)) {
                        Q x = u.x.add(p.y.sub(u.y).mul(v.x.sub(u.x)).div(v.y.sub(u.y)));
                        if (p.x.compareTo(x) < 0) inside = !inside;
                    }
                }
            }
            return inside ? I : E;
        }
    }

    static boolean onSegment(P p, P a, P b) {
        if (b.sub(a).cross(p.sub(a)).signum() != 0) return false;
        return p.sub(a).dot(p.sub(b)).signum() <= 0;
    }

    // the intersection point of two segments, if they intersect in exactly one point that is not parallel
    static P crossing(P[] s, P[] t) {
        P d = s[1].sub(s[0]), e = t[1].sub(t[0]);
        Q den = d.cross(e);
        if (den.signum() == 0) return null;
        P w = t[0].sub(s[0]);
        Q ts = w.cross(e).div(den), tt = w.cross(d).div(den);
        if (ts.signum() < 0 || ts.compareTo(Q.ONE) > 0 || tt.signum() < 0 || tt.compareTo(Q.ONE) > 0) return null;
        return s[0].add(d.scale(ts));
    }

    /**
     * A rational point
     */
    static final class P {
        final Q x, y;

        P(Q x, Q y) {
            this.x = x;
            this.y = y;
        }

        static P of(Coordinate c) {
            return new P(Q.of(c.x), Q.of(c.y));
        }

        P add(P o) {
            return new P(x.add(o.x), y.add(o.y));
        }

        P sub(P o) {
            return new P(x.sub(o.x), y.sub(o.y));
        }

        P scale(Q f) {
            return new P(x.mul(f), y.mul(f));
        }

        P perp() {
            return new P(y.negate(), x);
        }

        Q cross(P o) {
            return x.mul(o.y).sub(y.mul(o.x));
        }

        Q dot(P o) {
            return x.mul(o.x).add(y.mul(o.y));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof P && x.equals(((P) o).x) && y.equals(((P) o).y);
        }

        @Override
        public int hashCode() {
            return 31 * x.hashCode() + y.hashCode();
        }
    }

    /**
     * An exact rational number
     */
    static final class Q implements Comparable<Q> {
        static final Q ONE = new Q(1, 1);
        final BigInteger num, den;

        Q(long num, long den) {
            this(BigInteger.valueOf(num), BigInteger.valueOf(den));
        }

        Q(BigInteger num, BigInteger den) {
            if (den.signum() < 0) {
                num = num.negate();
                den = den.negate();
            }
            BigInteger g = num.gcd(den);
            if (g.signum() != 0 && !g.equals(BigInteger.ONE)) {
                num = num.divide(g);
                den = den.divide(g);
            }
            this.num = num;
            this.den = den;
        }

        static Q of(double v) {
            java.math.BigDecimal d = new java.math.BigDecimal(v);
            return d.scale() <= 0 ? new Q(d.toBigInteger(), BigInteger.ONE)
                    : new Q(d.unscaledValue(), BigInteger.TEN.pow(d.scale()));
        }

        Q add(Q o) {
            return new Q(num.multiply(o.den).add(o.num.multiply(den)), den.multiply(o.den));
        }

        Q sub(Q o) {
            return add(o.negate());
        }

        Q negate() {
            return new Q(num.negate(), den);
        }

        Q mul(Q o) {
            return new Q(num.multiply(o.num), den.multiply(o.den));
        }

        Q div(Q o) {
            return new Q(num.multiply(o.den), den.multiply(o.num));
        }

        int signum() {
            return num.signum();
        }

        @Override
        public int compareTo(Q o) {
            return sub(o).signum();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Q && num.equals(((Q) o).num) && den.equals(((Q) o).den);
        }

        @Override
        public int hashCode() {
            return 31 * num.hashCode() + den.hashCode();
        }
    }
}
