package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateSystemAxis;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.GeographicCoordinateReferenceSystem;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by maesenka on 12/08/14.
 */
public class PositionTest {

    @Test
    public void testGetCoordinateDimension() {
        CoordinateReferenceSystem<G2D> crs = new GeographicCoordinateReferenceSystem(CrsId.valueOf("EPSG", 1),
                "test",
                CoordinateSystemAxis.mkLatAxis(), CoordinateSystemAxis.mkLonAxis());

        G2D pos = new G2D(5, 50);
        double testLon = pos.getCoordinate(CoordinateSystemAxis.mkLonAxis(), crs);
        double testLat = pos.getCoordinate(CoordinateSystemAxis.mkLatAxis(), crs);
        assertEquals(5, testLon, 0.00001);
        assertEquals(50, testLat, 0.0001);

    }
}

