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
public class TransformationChainTest {

    TransformationChain chain;

    @Before
    public void setUp(){

        GeographicCoordinateReferenceSystem bd72 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4313);
        GeographicCoordinateReferenceSystem wgs84 = CoordinateReferenceSystems.WGS84;

        chain = new TransformationChain.Builder()
                .reverse(new Geographic3DTo2D())
                .forward(new GeographicToGeocentric(bd72))
                .forward( PositionVectorTransformation.fromTOWGS84( bd72.getDatum().getToWGS84()))
                .reverse(new GeographicToGeocentric(wgs84))
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
}