package org.geolatte.geom.cga;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.ring;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.geolatte.geom.G2D;
import org.geolatte.geom.LinearRing;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.GeographicCoordinateReferenceSystem;
import org.junit.Test;

/**
 * @author Stephan Psader, GAF AG, 2017
 */
public class NumericalMethodsTest {
    GeographicCoordinateReferenceSystem<G2D> wgs84 =
            CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);

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

            assertFalse(NumericalMethods.isCounterClockwise(ring));
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
}
