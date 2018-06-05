package org.geolatte.geom;

import org.junit.Test;

import static org.geolatte.geom.DecimalDegree.parseDMSLat;
import static org.geolatte.geom.DecimalDegree.parseDMSLon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Karel Maesen, Geovise BVBA on 21/07/17.
 */
public class DecimalDegreeTest {

    @Test
    public void testDMS(){
        assertEquals("4° 20' 30.00''E 50° 10' 20.00''N", DecimalDegree.DMS(4.341667, 50.172222));
    }

    @Test
    public void testSecondsToRadians(){
        assertEquals(0.0000026858677933494, DecimalDegree.secondsToRadians(0.554), Math.ulp(0.554));
    }

    @Test
    public void testparseDMS(){
        WithinTolerancePositionEquality eq = new WithinTolerancePositionEquality(0.00005);
        assertTrue(
                eq.equals(new G2D(4.341667, 50.172222),
                        DecimalDegree.parseDMS("4 20' 30.00''E 50 10' 20.00''N")
                )
        );
    }

    @Test
    public void testParseDMSLat() {
        assertEquals( 50.172222, parseDMSLat("50 10' 20.00''N"), 0.00001);
        assertEquals(52.1561606, parseDMSLat("52°09'22.178''N"), 0.00001);
    }


    @Test
    public void testParseDMSLon() {
        assertEquals( 4.341667, parseDMSLon("4 20' 30.00''E"), 0.00001);

    }




}
