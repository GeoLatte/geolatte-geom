package org.geolatte.geom.cga;

import org.geolatte.geom.cga.OverlayGraph.Edge;
import org.geolatte.geom.cga.OverlayGraph.End;
import org.geolatte.geom.cga.OverlayGraph.Node;
import org.geolatte.geom.cga.exact.Predicates;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the selected faces, edges and nodes of an {@link OverlayGraph} into polygons, lines and points.
 *
 * <p>Polygons: the boundary of the result is traced with the result to the left, taking at each node the first
 * boundary edge clockwise from the edge just traversed. A trace that passes a node more than once is split there
 * into cycles, so all cycles are simple. Counterclockwise cycles are shells, clockwise ones are holes. Each hole is
 * assigned to the innermost shell that contains it, decided exactly with a node of the hole that does not lie on
 * the shell.</p>
 *
 * <p>Lines: the result edges outside the faces of the result are merged into maximal lines, through nodes where
 * exactly two of them meet and nothing else of the result does, and that are not endpoints of an input line. A line
 * follows the direction of the first input line along it.</p>
 *
 * <p>Points: the nodes of the result that are not on a result edge or face.</p>
 */
final class OverlayResult {

    /**
     * The polygons, each as its shell followed by its holes; each ring as its nodes, without repeating the first
     */
    final List<List<List<Node>>> polygons = new ArrayList<>();
    final List<List<Node>> lines = new ArrayList<>();
    final List<Node> points = new ArrayList<>();

    private final OverlayGraph graph;
    private final Overlay.Operation op;
    private int stamp = 0;

    private OverlayResult(OverlayGraph graph, Overlay.Operation op) {
        this.graph = graph;
        this.op = op;
    }

    static OverlayResult of(OverlayGraph graph, Overlay.Operation op) {
        OverlayResult result = new OverlayResult(graph, op);
        for (Node n : graph.nodes) n.mark = 0;
        result.buildPolygons();
        result.buildLines();
        result.buildPoints();
        return result;
    }

    // ---------------------------------------------------------------------------------------------------------
    // polygons
    // ---------------------------------------------------------------------------------------------------------

    private static final class Cycle {
        final List<Node> nodes;
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        List<List<Node>> polygon;    // for a shell

        Cycle(List<Node> nodes) {
            this.nodes = nodes;
            for (Node n : nodes) {
                minX = Math.min(minX, n.x);
                minY = Math.min(minY, n.y);
                maxX = Math.max(maxX, n.x);
                maxY = Math.max(maxY, n.y);
            }
        }

        boolean envelopeContains(Cycle o) {
            return minX <= o.minX && o.maxX <= maxX && minY <= o.minY && o.maxY <= maxY;
        }
    }

    private void buildPolygons() {
        List<Cycle> shells = new ArrayList<>(), holes = new ArrayList<>();
        for (Edge e : graph.edges) {
            if (!e.isResultBoundary()) continue;
            End start = e.resultLeft ? e.forward : e.backward;
            if (start.visited) continue;
            for (List<Node> cycle : split(trace(start))) {
                Cycle c = new Cycle(cycle);
                int n = cycle.size();
                double[] xs = new double[n], ys = new double[n];
                for (int i = 0; i < n; i++) {
                    xs[i] = cycle.get(i).x;
                    ys[i] = cycle.get(i).y;
                }
                int orientation = Predicates.ringOrientation(xs, ys, n);
                if (orientation > 0) {
                    shells.add(c);
                } else if (orientation < 0) {
                    holes.add(c);
                } else {
                    throw new IllegalStateException("A cycle of the result boundary has no area");
                }
            }
        }
        for (Cycle s : shells) {
            s.polygon = new ArrayList<>();
            s.polygon.add(s.nodes);
            polygons.add(s.polygon);
        }
        if (holes.isEmpty()) return;
        int m = shells.size();
        double[] minX = new double[m], minY = new double[m], maxX = new double[m], maxY = new double[m];
        for (int i = 0; i < m; i++) {
            Cycle s = shells.get(i);
            minX[i] = s.minX;
            minY[i] = s.minY;
            maxX[i] = s.maxX;
            maxY[i] = s.maxY;
        }
        EnvelopeTree tree = new EnvelopeTree(minX, minY, maxX, maxY);
        for (Cycle h : holes) {
            Cycle[] inner = {null};
            tree.query(h.minX, h.minY, h.maxX, h.maxY, i -> {
                Cycle s = shells.get(i);
                if (!s.envelopeContains(h) || !contains(s, h)) return;
                // the shells that contain the hole are nested: keep the innermost
                if (inner[0] == null || (inner[0].envelopeContains(s) && contains(inner[0], s))) inner[0] = s;
            });
            if (inner[0] == null) throw new IllegalStateException("A hole of the result lies outside all shells");
            inner[0].polygon.add(h.nodes);
        }
    }

    // the boundary trace that starts with the end, keeping the result to the left
    private static List<Node> trace(End start) {
        List<Node> trace = new ArrayList<>();
        End current = start;
        do {
            current.visited = true;
            trace.add(current.from);
            Node v = current.to;
            int k = v.star.size(), r = current.reverse().index;
            End next = null;
            for (int j = 1; j < k && next == null; j++) {
                End e = v.star.get((r - j + k) % k);
                if (e.isResultShellSide()) next = e;
            }
            if (next == null) throw new IllegalStateException("The boundary of the result is not closed");
            current = next;
        } while (current != start);
        return trace;
    }

    // splits a closed trace into simple cycles at the nodes it passes more than once
    private List<List<Node>> split(List<Node> trace) {
        List<List<Node>> cycles = new ArrayList<>(1);
        int mark = ++stamp;
        List<Node> stack = new ArrayList<>(trace.size());
        List<Integer> position = new ArrayList<>();
        java.util.IdentityHashMap<Node, Integer> at = new java.util.IdentityHashMap<>();
        for (Node n : trace) {
            Integer i = n.mark == mark ? at.get(n) : null;
            if (i == null) {
                n.mark = mark;
                at.put(n, stack.size());
                stack.add(n);
                continue;
            }
            List<Node> cycle = new ArrayList<>(stack.subList(i, stack.size()));
            cycles.add(cycle);
            for (int j = i + 1; j < stack.size(); j++) at.remove(stack.get(j));
            stack.subList(i + 1, stack.size()).clear();
        }
        cycles.add(stack);
        return cycles;
    }

    /**
     * @return whether the shell contains the ring, which does not cross it and shares at most isolated nodes with
     * it: decided with a node of the ring that does not lie on the shell
     */
    private boolean contains(Cycle shell, Cycle ring) {
        int mark = ++stamp;
        for (Node n : shell.nodes) n.mark = mark;
        for (Node n : ring.nodes) {
            if (n.mark != mark) return insideRing(shell.nodes, n.x, n.y);
        }
        throw new IllegalStateException("A cycle of the result lies on another one");
    }

    // whether (x, y), which does not lie on the ring, lies inside it: by the parity of the crossings of a ray
    private static boolean insideRing(List<Node> ring, double x, double y) {
        boolean inside = false;
        int n = ring.size();
        for (int i = 0; i < n; i++) {
            Node p = ring.get(i), q = ring.get((i + 1) % n);
            if ((p.y <= y) == (q.y <= y)) continue;
            if (p.x < x && q.x < x) continue;
            int o = p.y < q.y ? Predicates.orientation(p.x, p.y, q.x, q.y, x, y)
                    : Predicates.orientation(q.x, q.y, p.x, p.y, x, y);
            if (o > 0) inside = !inside;   // (x, y) lies to the left of the upward edge: the ray to the right crosses it
        }
        return inside;
    }

    // ---------------------------------------------------------------------------------------------------------
    // lines
    // ---------------------------------------------------------------------------------------------------------

    // whether lines are not merged through the node
    private static boolean isLineBreak(Node n) {
        if (n.endpoints[OverlayGraph.A] + n.endpoints[OverlayGraph.B] > 0) return true;
        int count = 0;
        for (End e : n.star) {
            if (e.edge.resultLine) {
                count++;
            } else if (e.edge.isResult()) {
                return true;
            }
        }
        return count != 2;
    }

    // the other result line end at a node that is not a line break
    private static End otherLineEnd(Node n, End end) {
        for (End e : n.star) {
            if (e.edge.resultLine && e != end) return e;
        }
        throw new IllegalStateException();
    }

    private void buildLines() {
        for (Edge e : graph.edges) {
            if (!e.resultLine || e.forward.visited) continue;
            // go back to the start of the line: a line break, or e again if the line is closed
            End start = e.forward;
            while (!isLineBreak(start.from)) {
                End previous = otherLineEnd(start.from, start).reverse();
                if (previous.edge == e) break;
                start = previous;
            }
            List<Node> line = new ArrayList<>();
            List<End> ends = new ArrayList<>();
            End current = start;
            line.add(current.from);
            while (true) {
                current.visited = true;
                current.reverse().visited = true;
                ends.add(current);
                line.add(current.to);
                Node v = current.to;
                if (isLineBreak(v)) break;
                End next = otherLineEnd(v, current.reverse());
                if (next.visited) break;   // closed
                current = next;
            }
            // the direction of the first input line along it
            for (End end : ends) {
                int d = end.edge.direction;
                if (d == 0) continue;
                if ((d > 0) != end.forward) java.util.Collections.reverse(line);
                break;
            }
            lines.add(line);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // points
    // ---------------------------------------------------------------------------------------------------------

    private void buildPoints() {
        for (Node n : graph.nodes) {
            if (!op.test(n.in(OverlayGraph.A), n.in(OverlayGraph.B))) continue;
            boolean covered = false;
            for (End e : n.star) covered |= e.edge.isResult();
            if (!covered) points.add(n);
        }
    }
}
