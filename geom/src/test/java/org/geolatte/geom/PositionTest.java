package org.geolatte.geom;

import org.geolatte.geom.crs.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by maesenka on 12/08/14.
 */
public class PositionTest {

    @Test
    public void testGetCoordinateDimension() {
        CoordinateReferenceSystem<G2D> crs = new Geographic2DCoordinateReferenceSystem(CrsId.valueOf("EPSG", 1),
                "test",
                new EllipsoidalCoordinateSystem2D(CoordinateSystemAxis.mkLatAxis(), CoordinateSystemAxis.mkLonAxis()));

        G2D pos = new G2D(5, 50);
        double testLon = pos.getCoordinate(CoordinateSystemAxis.mkLonAxis());
        double testLat = pos.getCoordinate(CoordinateSystemAxis.mkLatAxis());
        assertEquals(5, testLon, 0.00001);
        assertEquals(50, testLat, 0.0001);

    }
}

