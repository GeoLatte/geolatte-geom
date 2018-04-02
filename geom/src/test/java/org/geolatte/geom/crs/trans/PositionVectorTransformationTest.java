package org.geolatte.geom.crs.trans;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 21/07/17.
 */
public class PositionVectorTransformationTest {

    PositionVectorTransformation wgs72Towgs84 = new PositionVectorTransformation( 0, 0, 4.5, 0, 0, 0.554, 0.219);

    @Test
    public void testForward(){
        double[] in = new double[]{3657660.66, 255768.55, 5201382.11};
        double[] out = new double[3];
        wgs72Towgs84.forward(in, out);
        assertEquals(3657660.78, out[0], 0.01);
        assertEquals( 255778.43, out[1], 0.01);
        assertEquals(5201387.75, out[2], 0.01);
    }


    @Test
    public void testReverse(){
        double[] in = new double[]{3657660.78, 255778.43, 5201387.75};
        double[] out = new double[3];
        wgs72Towgs84.reverse(in, out);
        assertEquals(3657660.66, out[0], 0.01);
        assertEquals( 255768.55, out[1], 0.01);
        assertEquals(5201382.11, out[2], 0.01);
    }

}