package org.geolatte.geom.crs.trans.projections;

import org.geolatte.geom.DecimalDegree;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.junit.Test;

import static java.lang.String.format;
import static org.geolatte.geom.DecimalDegree.parseDMS;
import static org.geolatte.geom.DecimalDegree.parseDMSLat;
import static org.geolatte.geom.DecimalDegree.parseDMSLon;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 05/06/2018.
 */
public class LambertCC2SPTest {

    Geographic2DCoordinateReferenceSystem RNB72 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4313);

    LambertConformalConic2SP BLambert72 = new LambertConformalConic2SP(
            RNB72,
            parseDMSLat("90 0' 00''N"),
            parseDMSLon("4 22' 2.952''E"),
            parseDMSLat("51°10'0.00204''N"),
            parseDMSLat("49°50'0.00204''N"),
            150000.013,
            5400088.438);

    @Test
    public void testForwardCase1(){

        double[] inCo = new double[]{5.3876389, 52.1561606};
        double[] outCo = new double[2];
        BLambert72.forward(inCo, outCo);

        assertEquals(outCo[0], 219843.841, 0.005);
        assertEquals(outCo[1], 316827.604, 0.005);


    }

    @Test
    public void testForwardCase2(){

        double[] inCo = new double[]{5, 58};
        double[] outCo = new double[2];
        BLambert72.forward(inCo, outCo);

        assertEquals(outCo[0], 187742.7, 0.005);
        assertEquals(outCo[1], 969521.653, 0.005);

    }

    @Test
    public void testReverseCase1(){

        double[] inCo = new double[]{219843.841,316827.604 };
        double[] outCo = new double[2];
        BLambert72.reverse(inCo, outCo);

        assertEquals(outCo[0], 5.3876389, 0.005);
        assertEquals(outCo[1], 52.1561606, 0.005);
    }


    @Test
    public void testReverseCase2(){
        double[] inCo = new double[]{187742.7, 969521.653};
        double[] outCo = new double[2];
        BLambert72.reverse(inCo, outCo);

        assertEquals(outCo[0], 5, 0.005);
        assertEquals(outCo[1], 58, 0.005);

    }

}
