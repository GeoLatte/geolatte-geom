package org.geolatte.geom;

import static org.geolatte.geom.builder.DSL.*;
import org.junit.Test;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_2D_METER;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_3DM_METER;
import static org.junit.Assert.assertEquals;

public class ForceToCrsTest {


    @Test
    public void test2DForceTo3DM(){
        Geometry<C2D> geom = point(PROJECTED_2D_METER, c(1, 2));
        Geometry<C3DM> forced = geom.toCrs(PROJECTED_3DM_METER, 0.0);
        assertEquals(point(PROJECTED_3DM_METER, c(1, 2, 0, 0)), forced);
    }

    @Test
    public void test3DMForceTo2D(){
        Geometry<C3DM> geom = point(PROJECTED_3DM_METER, c(1, 2, 0, 0));
        Geometry<C2D> forced = geom.toCrs(PROJECTED_2D_METER, 0.0);
        assertEquals(point(PROJECTED_2D_METER, c(1, 2)), forced);
    }

}
