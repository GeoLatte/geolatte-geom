package org.geolatte.geom.crs.trans.projections;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 02/04/2018.
 */
public class PseudoMercatorTest {

    @Test
    public void testCase1() {
        PseudoMercator pseudoMercator = new PseudoMercator();
        double[] in = new double[]{3, 50};
        double[] out = new double[2];
        pseudoMercator.forward(in, out);
        assertEquals(333958.47237982, out[0], 0.00001);
        assertEquals(6446275.84101716, out[1], 0.00001);
        pseudoMercator.reverse(out, in);
        assertEquals(3, in[0], 0.00001);
        assertEquals(50, in[1], 0.00001);
    }

    @Test
    public void testCase2() {
        PseudoMercator pseudoMercator = new PseudoMercator();
        double[] in = new double[2];
        double[] out = new double[2];
        in[0] = Math.toDegrees(-1.751147016);
        in[1] = Math.toDegrees(0.425542460);
        pseudoMercator.forward(in, out);
        double difference = -11169055.58 - out[0];
        //TODO -- can we improve accuracy??
        assertEquals(-11169055.58, out[0], 0.01);
        assertEquals(2800000.0, out[1], 0.01);

    }

}
