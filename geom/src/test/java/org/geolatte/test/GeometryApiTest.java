package org.geolatte.test;

import org.geolatte.geom.C3DM;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.junit.Test;

import java.util.Arrays;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.mkCoordinateReferenceSystem;
import static org.geolatte.geom.crs.CoordinateSystemAxisDirection.*;
import static org.geolatte.geom.crs.Unit.METER;
import static org.junit.Assert.assertEquals;

/**
 * Test class for API test and validation
 *
 *
 * Created by Karel Maesen, Geovise BVBA on 16/07/2018.
 */
public class GeometryApiTest {


    @Test
    public void createExpandedCrsFromEpsgCode(){
        CoordinateReferenceSystem<?> crs = mkCoordinateReferenceSystem(31370, METER, METER);
        assertEquals(C3DM.class, crs.getPositionClass());
        assertEquals(4, crs.getCoordinateDimension());
        assertEquals( Arrays.asList(EAST, NORTH, UP, OTHER), crs.getCoordinateSystem().getAxisDirections());
    }


}
