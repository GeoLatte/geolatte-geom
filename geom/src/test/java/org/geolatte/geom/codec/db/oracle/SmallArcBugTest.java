package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

// for testing issue #151
public class SmallArcBugTest {

    @Test
    public void testCase1(){
        int[] elems =  {1,1005,3,1,2,1,15,2,2,19,2,1};
        Double[] coords = {209499.475,578259.358,209503.708,578359.503,209505.325,578409.615,209505.144,578416.324,209504.933,578419.552,209505.118,578420.213,209505.693,578421.019,209500.401,578421.362,209500.401,578421.361,209500.402,578421.361,209500.832,578421.033,209501.057,578420.591,209501.063,578420.225,209499.607,578371.806,209497.623,578305.131,209495.574,578238.184,209495.409,578222.433,209495.062,578211.578,209498.02,578211.469,209499.475,578259.358};
        SDOGeometry sdo = SDOGeometryHelper.sdoGeometry(
                2003, 28992, null, elems, coords
        );
        Geometry<?> geom = Decoders.decode(sdo);
        assertNotNull(geom); //check that this doesn't throw an exception
    }

}
