package org.geolatte.geom.crs.trans;

import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.GeographicCoordinateReferenceSystem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 22/07/17.
 */
public class ConcatenatedCoordinateOperationTest {

    ConcatenatedOperation chain;

    @Before
    public void setUp(){

        GeographicCoordinateReferenceSystem bd72 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4313);
        GeographicCoordinateReferenceSystem wgs84 = CoordinateReferenceSystems.WGS84;

        chain = new ConcatenatedOperation.Builder()
                .reverse(new Geographic3DTo2DConversion())
                .forward(new GeographicToGeocentricConversion( bd72))
                .forward( PositionVectorTransformation.fromTOWGS84( bd72.getDatum().getToWGS84()))
                .reverse(new GeographicToGeocentricConversion( wgs84))
                .build();
    }

    @Test
    public void testForward(){
        double[] inCoordinate = new double[]{4, 50};
        double[] outCoordinate = new double[3];

        chain.forward(inCoordinate, outCoordinate);

        assertEquals(4.00124343197523, outCoordinate[0], 0.000001);
        assertEquals(49.9994334980867, outCoordinate[1], 0.000001);
    }


    @Test
    public void testReverse(){
        double[] inCoordinate = new double[]{4.00124343197523, 49.9994334980867, 0};
        double[] outCoordinate = new double[2];

        chain.reverse(inCoordinate, outCoordinate);

        assertEquals(4, outCoordinate[0], 0.000001);
        assertEquals(50, outCoordinate[1], 0.000001);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionWhenDimensionsDoNotMatch() {
        CoordinateOperation testOp = new TrivialOp();
        CoordinateOperation appended = chain.appendForward( testOp );
    }

    @Test
    public void testAppendForward(){

        CoordinateOperation testOp = new TrivialOp();

        CoordinateOperation appended = chain.reverse().appendForward( testOp );
        double[] inCoordinate = new double[]{4.00124343197523, 49.9994334980867, 0};
        double[] outCoordinate = new double[3];

        appended.forward(inCoordinate, outCoordinate);

        assertEquals(5, outCoordinate[0], 0.000001);
        assertEquals(51, outCoordinate[1], 0.000001);
        assertEquals(-1, outCoordinate[2], 0.000001);

    }

    @Test
    public void testAppendReverse(){

        CoordinateOperation testOp = new TrivialOp();

        CoordinateOperation appended = chain.appendReverse( testOp );
        double[] inCoordinate = new double[]{4.0, 50};
        double[] outCoordinate = new double[2];

        appended.forward(inCoordinate, outCoordinate);

        assertEquals(3.00124343197523, outCoordinate[0], 0.000001);
        assertEquals(48.9994334980867, outCoordinate[1], 0.000001);

    }

    static class TrivialOp implements CoordinateOperation{

        @Override
        public boolean isReversible() {
            return true;
        }

        @Override
        public int inCoordinateDimension() {
            return 2;
        }

        @Override
        public int outCoordinateDimension() {
            return 3;
        }

        @Override
        public void forward(double[] inCoordinate, double[] outCoordinate) {
            outCoordinate[0] = inCoordinate[0] + 1;
            outCoordinate[1] = inCoordinate[1] + 1;
            outCoordinate[2] = -1;
        }

        @Override
        public void reverse(double[] inCoordinate, double[] outCoordinate) {
            outCoordinate[0] = inCoordinate[0] - 1;
            outCoordinate[1] = inCoordinate[1] - 1;
        }
    };

}