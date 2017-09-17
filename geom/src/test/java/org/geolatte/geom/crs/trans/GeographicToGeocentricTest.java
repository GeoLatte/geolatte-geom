package org.geolatte.geom.crs.trans;

import org.geolatte.geom.DecimalDegree;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 21/07/17.
 */
public class GeographicToGeocentricTest {

    Geographic2DCoordinateReferenceSystem wgs84 = CoordinateReferenceSystems.WGS84;
    GeographicToGeocentric toGeocentric = new GeographicToGeocentric(wgs84);


    @Test
    public void testForward(){

        double[] result = new double[3];
        toGeocentric.forward(new double[]{2.12955, 53.809394, 73.0},  result);
        assertEquals(3771793.97, result[0], 0.1);
        assertEquals(140253.34, result[1], 0.1);
        assertEquals(5124304.35, result[2], 0.1);

    }

    @Test
    public void testReverse(){

        double[] result = new double[3];
        toGeocentric.reverse(new double[]{3771793.97, 140253.34,5124304.35 },  result);
        assertEquals(2.12955, result[0], 0.001);
        assertEquals(53.809394, result[1], 0.001);
        assertEquals(73.0, result[2], 0.01);
    }


}