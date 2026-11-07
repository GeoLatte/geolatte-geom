package org.geolatte.geom.cga;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.cga.NumericalMethods.orient2d;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Random;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.GeographicCoordinateReferenceSystem;
import org.junit.Test;

/**
 * @author Stephan Psader, GAF AG, 2017
 */
public class NumericalMethodsTest {
    GeographicCoordinateReferenceSystem<G2D> wgs84 =
            CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);

    CoordinateReferenceSystem<C2D> xy = CoordinateReferenceSystems.PROJECTED_2D_METER;

    @Test
    public void testNotCounterClockwiseLinearRing() {
        //LINEARRING(500 500, 550 500, 600 500, 600 450, 600 400, 550 450, 500 400, 500 450, 500 500)
        LinkedList<G2D> positions = new LinkedList<>();
        positions.add(g(500, 500));
        positions.add(g(550, 500));
        positions.add(g(600, 500));
        positions.add(g(600, 450));
        positions.add(g(600, 400));
        positions.add(g(550, 450));
        positions.add(g(500, 400));
        positions.add(g(500, 450));

        //test orientation once for each position as starting point
        for (int i = 1; i <= positions.size(); i++) {
            positions.add(positions.getFirst());
            G2D[] positionsArray = positions.toArray(new G2D[positions.size()]);
            positions.removeFirst();

            LinearRing<? extends Position> ring = ring(wgs84, positionsArray);

            assertFalse(NumericalMethods.isCounterClockwise(ring));
        }
    }

    @Test
    public void testNotCounterClockwiseLinearRingWithSpike() {
        //LINEARRING (500 500, 550 500, 600 500, 540 500, 600 450, 600 400, 600 350, 600 400, 550 450, 500 400, 450 400, 500 400, 500 450, 500 550, 500 500)
        LinkedList<G2D> positions = new LinkedList<>();
        positions.add(g(500, 500));
        positions.add(g(550, 500));
        positions.add(g(600, 500));
        positions.add(g(540, 500));
        positions.add(g(600, 450));
        positions.add(g(600, 400));
        positions.add(g(600, 350));
        positions.add(g(600, 400));
        positions.add(g(550, 450));
        positions.add(g(500, 400));
        positions.add(g(450, 400));
        positions.add(g(500, 400));
        positions.add(g(500, 450));
        positions.add(g(500, 550));

        //test orientation once for each position as starting point
        for (int i = 1; i <= positions.size(); i++) {
            positions.add(positions.getFirst());
            G2D[] positionsArray = positions.toArray(new G2D[positions.size()]);
            positions.removeFirst();

            LinearRing<? extends Position> ring = ring(wgs84, positionsArray);

            assertFalse(Wkt.toWkt(ring) , NumericalMethods.isCounterClockwise(ring));
        }
    }

    @Test
    public void testCounterClockwiseLinearRing() {
        //LINEARRING (500 500, 500 450, 500 400, 550 450, 600 400, 600 450, 600 500, 550 500, 500 500)
        LinkedList<G2D> positions = new LinkedList<>();
        positions.add(g(500, 500));
        positions.add(g(500, 450));
        positions.add(g(500, 400));
        positions.add(g(550, 450));
        positions.add(g(600, 400));
        positions.add(g(600, 450));
        positions.add(g(600, 500));
        positions.add(g(550, 500));

        //test orientation once for each position as starting point
        for (int i = 1; i <= positions.size(); i++) {
            positions.add(positions.getFirst());
            G2D[] positionsArray = positions.toArray(new G2D[positions.size()]);
            positions.removeFirst();

            LinearRing<? extends Position> ring = ring(wgs84, positionsArray);

            assertTrue(NumericalMethods.isCounterClockwise(ring));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testColinearVertical() {
        LinearRing<? extends Position> ring =
                ring(wgs84, g(500, 500), g(500, 400), g(500, 300), g(500, 200), g(500, 500));
        NumericalMethods.isCounterClockwise(ring);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testColinearHorizontal() {
        LinearRing<? extends Position> ring =
                ring(wgs84, g(500, 500), g(400, 500), g(300, 500), g(200, 500), g(500, 500));
        NumericalMethods.isCounterClockwise(ring);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testColinearZigZag() {
        LinearRing<? extends Position> ring =
                ring(wgs84, g(500, 500), g(400, 400), g(300, 600), g(200, 200), g(300, 600), g(400,400), g(500, 500));
        NumericalMethods.isCounterClockwise(ring);
    }

    @Test
    public void testCounterClockwiseLineString() {
        LinearRing<C2D> cwLinestring  = ring(xy, c(0, 0), c(0, 1), c(1, 1), c(1, 0),   c(0, 0));
        LinearRing<C2D> ccwLinestring = ring(xy, c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0));

        assertFalse(NumericalMethods.isCounterClockwise(cwLinestring));
        assertEquals((int)orient2d(cwLinestring), -1);

        assertTrue(NumericalMethods.isCounterClockwise(ccwLinestring));
        assertEquals((int)orient2d(ccwLinestring), 1);

    }

    @Test
    public void testPolygonWithVerySmallPolygon() {
        String smallpolygon = "POLYGON((-88.101304825 30.791176733, -88.101303916 30.791176749, -88.101304 30.791177, -88.101304825 30.791176733))";
        Polygon<G2D> poly = (Polygon<G2D>)Wkt.fromWkt(smallpolygon, CoordinateReferenceSystems.WGS84);
        assertTrue(orient2d(poly.getExteriorRing()) != 0.0d);
    }

    /**
     * Near-collinear positions in projected coordinates. The original floating point implementation, which
     * multiplied raw coordinates, got about 77% of these wrong.
     */
    @Test
    public void testOrientationIsExactForNearCollinearPositions() {
        Random random = new Random(1L);
        for (int k = 0; k < 10_000; k++) {
            double x0 = 150000 + random.nextDouble() * 1000, y0 = 200000 + random.nextDouble() * 1000;
            double x1 = x0 + random.nextDouble() * 500, y1 = y0 + random.nextDouble() * 500;
            double t = random.nextDouble();
            double x2 = x0 + t * (x1 - x0), y2 = y0 + t * (y1 - y0);
            int exact = new BigDecimal(x1).subtract(new BigDecimal(x0)).multiply(new BigDecimal(y2).subtract(new BigDecimal(y0)))
                    .subtract(new BigDecimal(x2).subtract(new BigDecimal(x0)).multiply(new BigDecimal(y1).subtract(new BigDecimal(y0))))
                    .signum();
            NumericalMethods.Orientation expected = exact == 0 ? NumericalMethods.Orientation.Colinear :
                    exact > 0 ? NumericalMethods.Orientation.Counterclockwise : NumericalMethods.Orientation.Clockwise;
            assertEquals(expected, NumericalMethods.orientation(c(x0, y0), c(x1, y1), c(x2, y2)));
            assertEquals(exact == 0, NumericalMethods.collinear(c(x0, y0), c(x1, y1), c(x2, y2)));
        }
    }

    @Test
    public void testOrientationOfNearlyDegenerateRings() {
        // all positions on a horizontal line: zero area
        LinearRing<C2D> flat = ring(xy, c(150000.5, 200000.5), c(150500.5, 200000.5), c(Math.nextUp(150250.5), 200000.5), c(150000.5, 200000.5));
        assertEquals(0, (int) orient2d(flat));
        // a thin triangle: the apex lies 1 ulp above the base line
        LinearRing<C2D> thin = ring(xy, c(150000.5, 200000.5), c(150500.5, 200000.5), c(150250.5, Math.nextUp(200000.5)), c(150000.5, 200000.5));
        assertEquals(1, (int) orient2d(thin));
        assertTrue(NumericalMethods.isCounterClockwise(thin));
        // a degenerate diagonal ring: exact signed area of (0.1, 0.1), (0.3, 0.3), (0.2, 0.2) as doubles
        LinearRing<C2D> diagonal = ring(xy, c(0.1, 0.1), c(0.3, 0.3), c(0.2, 0.2), c(0.1, 0.1));
        BigDecimal a = new BigDecimal(0.1), b = new BigDecimal(0.3), d = new BigDecimal(0.2);
        int expected = a.multiply(b).subtract(b.multiply(a)).add(b.multiply(d)).subtract(d.multiply(b))
                .add(d.multiply(a)).subtract(a.multiply(d)).signum();
        assertEquals(expected, (int) orient2d(diagonal));
    }

    @Test
    public void testNonFiniteCoordinatesAreRejected() {
        double[] invalid = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double v : invalid) {
            for (int i = 0; i < 6; i++) {
                double[] crds = {0, 0, 1, 1, 2, 0};
                crds[i] = v;
                C2D p0 = c(crds[0], crds[1]), p1 = c(crds[2], crds[3]), p2 = c(crds[4], crds[5]);
                assertThrows(v + " at " + i, IllegalArgumentException.class, () -> NumericalMethods.orientation(p0, p1, p2));
                assertThrows(v + " at " + i, IllegalArgumentException.class, () -> NumericalMethods.collinear(p0, p1, p2));
                LinearRing<C2D> r = ring(xy, p0, p1, p2, p0);
                assertThrows("ring: " + v + " at " + i, IllegalArgumentException.class, () -> orient2d(r));
            }
        }
    }

    @Test
    public void testOverflowIsHandled() {
        C2D p0 = c(-1e300, -1e300), p1 = c(1e300, 1e300), p2 = c(0, Math.nextUp(0.0));
        assertEquals(NumericalMethods.Orientation.Counterclockwise, NumericalMethods.orientation(p0, p1, p2));
        LinearRing<C2D> r = ring(xy, c(-1e300, -1e300), c(1e300, -1e300), c(1e300, 1e300), c(-1e300, -1e300));
        assertEquals(1, (int) orient2d(r));
    }

}
