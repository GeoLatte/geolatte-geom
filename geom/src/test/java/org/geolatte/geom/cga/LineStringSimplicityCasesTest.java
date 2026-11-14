package org.geolatte.geom.cga;

import org.geolatte.geom.C2D;
import org.geolatte.geom.C2DM;
import org.geolatte.geom.C3D;
import org.geolatte.geom.LineString;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.operation.valid.IsSimpleOp;

import java.util.Random;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.*;

/**
 * Simplicity of line strings with {@link Simplicity} (these cases were written for the floating point
 * {@code LineStringSimplicity} of the 2.0 branch, which the exact {@link Simplicity} replaced).
 */
public class LineStringSimplicityCasesTest {

    private static boolean isSimple(LineString<?> line) {
        return Simplicity.isSimple(line);
    }

    @Test
    public void emptyLineIsSimple() {
        assertTrue(isSimple(linestring(crs)));
    }

    @Test
    public void singleSegmentIsSimple() {
        assertTrue(isSimple(linestring(crs, c(0, 0), c(1, 1))));
    }

    @Test
    public void simpleOpenLine() {
        assertTrue(isSimple(linestring(crs, c(0, 0), c(1, 1), c(2, 0), c(3, 1))));
    }

    @Test
    public void simpleClosedSquare() {
        assertTrue(isSimple(linestring(crs, c(0, 0), c(0, 1), c(1, 1), c(1, 0), c(0, 0))));
    }

    @Test
    public void simpleClosedTriangle() {
        assertTrue(isSimple(linestring(crs, c(0, 0), c(1, 0), c(0, 1), c(0, 0))));
    }

    @Test
    public void bowTieIsNotSimple() {
        assertFalse(isSimple(linestring(crs, c(0, 0), c(1, 1), c(1, 0), c(0, 1), c(0, 0))));
    }

    @Test
    public void crossingOpenLineIsNotSimple() {
        assertFalse(isSimple(linestring(crs, c(0, 0), c(2, 2), c(2, 0), c(0, 2))));
    }

    @Test
    public void lineTouchingItsInteriorAtAVertexIsNotSimple() {
        // the vertex (1, 0) lies in the interior of the first segment
        assertFalse(isSimple(linestring(crs, c(0, 0), c(2, 0), c(2, 1), c(1, 0))));
    }

    @Test
    public void lineRevisitingAVertexIsNotSimple() {
        assertFalse(isSimple(linestring(crs, c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(1, 0), c(2, 0))));
    }

    @Test
    public void collinearBacktrackIsNotSimple() {
        assertFalse(isSimple(linestring(crs, c(0, 0), c(2, 0), c(1, 0))));
    }

    @Test
    public void collinearOverlapOfNonAdjacentSegmentsIsNotSimple() {
        assertFalse(isSimple(linestring(crs, c(0, 0), c(3, 0), c(3, 1), c(1, 1), c(1, 0), c(2, 0))));
    }

    @Test
    public void collinearContinuationIsSimple() {
        assertTrue(isSimple(linestring(crs, c(0, 0), c(1, 0), c(2, 0), c(3, 0))));
    }

    @Test
    public void repeatedConsecutivePositionsAreIgnored() {
        assertTrue(isSimple(linestring(crs, c(0, 0), c(0, 0), c(0, 1), c(1, 1), c(1, 1), c(1, 0), c(0, 0))));
    }

    @Test
    public void repeatedVertexAtACornerIsIgnored() {
        // Note: JTS (1.19, 1.20) IsSimpleOp reports this line as non-simple. Repeated vertices do not change the
        // point set of the curve, so per ISO/IEC 13249-3 it is simple.
        assertTrue(isSimple(linestring(crs, c(0, 0), c(1, 1), c(1, 1), c(2, 0), c(3, 1))));
    }

    @Test
    public void closedLineWhoseInteriorPassesThroughStartIsNotSimple() {
        assertFalse(isSimple(linestring(crs, c(0, 0), c(1, 0), c(1, 1), c(0, 0), c(-1, 1), c(-1, 0), c(0, 0))));
    }

    @Test
    public void closingSegmentDoublingBackIsNotSimple() {
        // the closing segment (0 1 -> 0 0) continues over the first segment (0 0 -> 0 2)
        assertFalse(isSimple(linestring(crs, c(0, 0), c(0, 2), c(1, 1), c(0, 1), c(0, 0))));
    }

    @Test
    public void degenerateTwoSegmentClosedLineIsNotSimple() {
        assertFalse(isSimple(linestring(crs, c(0, 0), c(1, 0), c(0, 0))));
    }

    @Test
    public void zAndMCoordinatesAreIgnored() {
        // closed and simple in XY, although Z and M values differ at the start and end
        LineString<C3D> l3d = linestring(crsZ, c(0, 0, 0), c(0, 1, 1), c(1, 1, 2), c(0, 0, 9));
        LineString<C2DM> l2dm = linestring(crsM, cM(0, 0, 0), cM(0, 1, 1), cM(1, 1, 2), cM(0, 0, 9));
        assertTrue(isSimple(l3d));
        assertTrue(isSimple(l2dm));

        // crossing in XY, although the segments are at different heights
        LineString<C3D> crossing3d = linestring(crsZ, c(0, 0, 0), c(2, 2, 0), c(2, 0, 10), c(0, 2, 10));
        assertFalse(isSimple(crossing3d));
    }

    private static LineString<C2D> regularPolygon(int n) {
        C2D[] pts = new C2D[n + 1];
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            pts[i] = c(1000 * Math.cos(a), 1000 * Math.sin(a));
        }
        pts[n] = pts[0];
        return linestring(crs, pts);
    }

    @Test
    public void largeRegularPolygonIsSimple() {
        assertTrue(isSimple(regularPolygon(5000)));
    }

    @Test
    public void largePolygonWithOneCrossingIsNotSimple() {
        LineString<C2D> pgn = regularPolygon(5000);
        C2D[] pts = new C2D[pgn.getNumPositions()];
        for (int i = 0; i < pts.length; i++) pts[i] = pgn.getPositionN(i);
        pts[2500] = c(1000, 1); // pull a vertex from the far side across the polygon, past the start vertex
        assertFalse(isSimple(linestring(crs, pts)));
    }

    @Test
    public void largeZigzagIsSimpleUnlessClosedAcrossIt() {
        int teeth = 2000;
        C2D[] open = new C2D[teeth];
        for (int i = 0; i < teeth; i++) open[i] = c(i % 2, i);
        assertTrue(isSimple(linestring(crs, open)));

        C2D[] closedAcross = java.util.Arrays.copyOf(open, teeth + 1);
        closedAcross[teeth] = open[0]; // closing segment cuts through all teeth
        assertFalse(isSimple(linestring(crs, closedAcross)));
    }

    /**
     * A staircase that rises to the right: a single monotone chain of 2 * steps segments.
     */
    private static C2D[] staircase(int steps) {
        C2D[] pts = new C2D[2 * steps + 1];
        pts[0] = c(0, 0);
        for (int i = 0; i < steps; i++) {
            pts[2 * i + 1] = c(i + 1, i);
            pts[2 * i + 2] = c(i + 1, i + 1);
        }
        return pts;
    }

    @Test
    public void singleMonotoneChainIsSimple() {
        assertTrue(isSimple(linestring(crs, staircase(1000))));
    }

    @Test
    public void lineReturningToTouchAVertexDeepInsideALongChainIsNotSimple() {
        // after the staircase, the line moves away and comes back to touch vertex (500, 500)
        C2D[] stairs = staircase(1000);
        C2D[] pts = java.util.Arrays.copyOf(stairs, stairs.length + 2);
        pts[stairs.length] = c(1000, 2000);
        pts[stairs.length + 1] = c(500, 500);
        assertFalse(isSimple(linestring(crs, pts)));
    }

    @Test
    public void lineReturningToTouchASegmentInteriorDeepInsideALongChainIsNotSimple() {
        C2D[] stairs = staircase(1000);
        C2D[] pts = java.util.Arrays.copyOf(stairs, stairs.length + 2);
        pts[stairs.length] = c(1000, 2000);
        pts[stairs.length + 1] = c(500, 499.5); // on the vertical segment (500, 499) - (500, 500)
        assertFalse(isSimple(linestring(crs, pts)));
    }

    @Test
    public void lineReturningCloseToALongChainWithoutTouchingIsSimple() {
        C2D[] stairs = staircase(1000);
        C2D[] pts = java.util.Arrays.copyOf(stairs, stairs.length + 2);
        pts[stairs.length] = c(1000, 2000);
        pts[stairs.length + 1] = c(499.5, 500.25); // just above the staircase, not touching it
        assertTrue(isSimple(linestring(crs, pts)));
    }

    @Test
    public void spiralIsSimple() {
        int n = 5000;
        C2D[] pts = new C2D[n];
        for (int i = 0; i < n; i++) {
            double a = i * 0.05;
            double r = 10 + 2 * a;
            pts[i] = c(r * Math.cos(a), r * Math.sin(a));
        }
        assertTrue(isSimple(linestring(crs, pts)));
    }

    /**
     * Cross-checks against JTS on random walks with up to a few hundred vertices, which exercises the sweep and the
     * sorting of many segments.
     */
    @Test
    public void agreesWithJtsOnLargeRandomWalks() {
        Random rnd = new Random(42L);
        int simpleCount = 0;
        for (int run = 0; run < 2000; run++) {
            int numPoints = 16 + rnd.nextInt(300);
            boolean close = rnd.nextBoolean();
            C2D[] pts = new C2D[close ? numPoints + 1 : numPoints];
            double x = 0, y = 0, heading = 0;
            for (int i = 0; i < numPoints; i++) {
                pts[i] = c(Math.rint(x), Math.rint(y));
                heading += (rnd.nextDouble() - 0.5) * 0.6;
                x += 20 * Math.cos(heading);
                y += 20 * Math.sin(heading);
            }
            if (close) pts[numPoints] = pts[0];
            LineString<C2D> line = linestring(crs, pts);
            org.locationtech.jts.geom.Geometry jtsLine = JTS.to(line);
            org.locationtech.jts.geom.Coordinate[] deduped =
                    CoordinateArrays.removeRepeatedPoints(jtsLine.getCoordinates());
            boolean expected = new IsSimpleOp(jtsLine.getFactory().createLineString(deduped)).isSimple();
            assertEquals("Disagreement with JTS for " + line, expected, isSimple(line));
            if (expected) simpleCount++;
        }
        assertTrue("too few simple lines generated: " + simpleCount, simpleCount > 200);
        assertTrue("too few non-simple lines generated: " + simpleCount, simpleCount < 1800);
    }

    /**
     * Cross-checks against JTS' {@code IsSimpleOp} on random lines with vertices on a small integer grid. Integer
     * coordinates keep all orientation tests exact, while the small grid produces many touching and collinear
     * configurations.
     *
     * <p>JTS is given the line without consecutive repeated vertices, because JTS' {@code IsSimpleOp} treats a
     * repeated vertex at a corner as a self-intersection (see {@link #repeatedVertexAtACornerIsIgnored()}). The
     * line under test keeps its repeated vertices.</p>
     */
    @Test
    public void agreesWithJtsOnRandomLines() {
        Random rnd = new Random(20261107L);
        int nonSimpleCount = 0;
        for (int run = 0; run < 20000; run++) {
            int gridSize = 2 + rnd.nextInt(6);
            int numPoints = 2 + rnd.nextInt(7);
            boolean close = rnd.nextBoolean();
            C2D[] pts = new C2D[close ? numPoints + 1 : numPoints];
            for (int i = 0; i < numPoints; i++) {
                pts[i] = c(rnd.nextInt(gridSize), rnd.nextInt(gridSize));
            }
            if (close) pts[numPoints] = pts[0];
            LineString<C2D> line = linestring(crs, pts);

            org.locationtech.jts.geom.Geometry jtsLine = JTS.to(line);
            org.locationtech.jts.geom.Coordinate[] deduped =
                    CoordinateArrays.removeRepeatedPoints(jtsLine.getCoordinates());
            if (deduped.length < 2) continue; // all vertices coincide
            boolean expected = new IsSimpleOp(jtsLine.getFactory().createLineString(deduped)).isSimple();
            assertEquals("Disagreement with JTS for " + line, expected, isSimple(line));
            if (!expected) nonSimpleCount++;
        }
        // sanity check that the random data exercises both outcomes
        assertTrue(nonSimpleCount > 1000);
        assertTrue(nonSimpleCount < 19000);
    }
}
