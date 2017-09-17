package org.geolatte.geom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 21/07/17.
 */
public class DecimalDegreeTest {

    @Test
    public void testDMS(){
        assertEquals("4 20' 30.00''E 50 10' 20.00''N", DecimalDegree.DMS(4.341667, 50.172222));
    }

    @Test
    public void testSecondsToRadians(){
        assertEquals(0.0000026858677933494, DecimalDegree.secondsToRadians(0.554), Math.ulp(0.554));
    }
}
