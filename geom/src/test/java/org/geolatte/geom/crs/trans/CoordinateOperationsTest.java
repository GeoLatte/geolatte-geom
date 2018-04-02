package org.geolatte.geom.crs.trans;

import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.junit.Test;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.crs.trans.CoordinateOperations.positionVectorTransformation2D;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 02/04/2018.
 */
public class CoordinateOperationsTest {

    Geographic2DCoordinateReferenceSystem ed87 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4231);
    Geographic2DCoordinateReferenceSystem b50 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4809);

    @Test(expected = IllegalArgumentException.class)
    public void failWhenSourceCRSHasNoToWGS84() {

        CoordinateOperation operation = positionVectorTransformation2D(b50, WGS84);
    }

    @Test
    public void testED87ToWGS84() {
        //the European ED87 Geodetic CRS

        CoordinateOperation operation = positionVectorTransformation2D(ed87, WGS84);
        double[] in = new double[]{3, 50};
        double[] out = new double[2];
        operation.forward(in, out);
        assertEquals(2.99871552530254, out[0], 0.000001);
        assertEquals(49.9991323076184, out[1], 0.000001);
    }

    @Test
    public void testWGS84ToED87() {
        //the European ED87 Geodetic CRS

        CoordinateOperation operation = positionVectorTransformation2D(WGS84, ed87);
        double[] in = new double[]{3, 50};
        double[] out = new double[2];
        operation.forward(in, out);
        assertEquals(3.00128448068281, out[0], 0.000001);
        assertEquals(50.0008676608662, out[1], 0.000001);
    }

}
