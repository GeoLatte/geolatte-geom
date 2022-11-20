package org.geolatte.geom.cga;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.cga.NumericalMethods.orient2d;
import static org.junit.Assert.*;

import java.util.LinkedList;

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

}
