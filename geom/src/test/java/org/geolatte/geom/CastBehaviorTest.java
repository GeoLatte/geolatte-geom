package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.Unit;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WEB_MERCATOR;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;

/**
 * A Set of test cases to test the behavior of Geomtries when cast to different dimension
 * Created by Karel Maesen, Geovise BVBA on 25/07/2018.
 */
public class CastBehaviorTest {


    private final static double epsilon = 0.00001;

    @Test
    public void testCast3Dto2DPlanar(){
        CoordinateReferenceSystem<C3D> crs3D = CoordinateReferenceSystems.mkCoordinateReferenceSystem(WEB_MERCATOR, Unit.METER, null, C3D.class);
        Point<?> pnt = point(crs3D, c(1, 2, 3));
        Point<C2D> pntAs2D = pnt.as(C2D.class);
        assertEquals(1, pntAs2D.getPosition().getX(), epsilon);
        assertEquals(2, pntAs2D.getPosition().getY(), epsilon);
    }

    @Test
    public void testCast3Dto2DGeographic(){
        CoordinateReferenceSystem<G3D> crs3D = CoordinateReferenceSystems.mkCoordinateReferenceSystem(WGS84, Unit.METER, null, G3D.class);
        Point<?> pnt = point(crs3D, g(1, 2, 3));
        Point<G2D> pntAs2D = pnt.as(G2D.class);
        assertEquals(1, pntAs2D.getPosition().getLon(), epsilon);
        assertEquals(2, pntAs2D.getPosition().getLat(), epsilon);
    }

    @Test
    public void testCastMultiPoint3DPlanarto2DGeographic(){
        CoordinateReferenceSystem<C3D> crs3D = CoordinateReferenceSystems.mkCoordinateReferenceSystem(WEB_MERCATOR, Unit.METER, null, C3D.class);
        MultiPoint<?> pnt = multipoint(crs3D, c(1, 2, 3), c(4,5,6));
        MultiPoint<C2D> mPointAs2D = pnt.as(C2D.class);
        assertEquals(1, mPointAs2D.getPositionN(0).getX(), epsilon);
        assertEquals(4, mPointAs2D.getPositionN(1).getX(), epsilon);
    }

    @Test
    public void testCastMultiGeometry3DPlanarto2DGeographic(){
        CoordinateReferenceSystem<C3D> crs3D = CoordinateReferenceSystems.mkCoordinateReferenceSystem(WEB_MERCATOR, Unit.METER, null, C3D.class);
        GeometryCollection<C3D, Geometry<C3D>> gc = geometrycollection(crs3D, point(c(1, 2, 3)), point(c(4,5,6)));
        Geometry<C2D> gc2D = gc.as(C2D.class);
        assertEquals(1, gc2D.getPositionN(0).getX(), epsilon);
        assertEquals(5, gc2D.getPositionN(1).getY(), epsilon);
    }


    @Test(expected = ClassCastException.class)
    public void testCast3Dto2DMPlanar(){
        CoordinateReferenceSystem<C3D> crs3D = CoordinateReferenceSystems.mkCoordinateReferenceSystem(WEB_MERCATOR, Unit.METER, null, C3D.class);
        Point<?> pnt = point(crs3D, c(1, 2, 3));
        Point<C2DM> pntAs2Dm = pnt.as(C2DM.class);
        assertEquals(1, pntAs2Dm.getPosition().getX(), epsilon);
    }

    @Test(expected = ClassCastException.class)
    public void testCast2Dto3DPlanar(){

        Point<?> pnt = point(WEB_MERCATOR, c(1, 2));
        Point<C3D> pntAs3D = pnt.as(C3D.class);
        assertEquals(1, pntAs3D.getPosition().getX(), epsilon);
    }


    @Test(expected = ClassCastException.class)
    public void testCast3DPlanarto2DGeographic(){
        CoordinateReferenceSystem<C3D> crs3D = CoordinateReferenceSystems.mkCoordinateReferenceSystem(WEB_MERCATOR, Unit.METER, null, C3D.class);
        Point<?> pnt = point(crs3D, c(1, 2, 3));
        Point<G2D> pntAs2D = pnt.as(G2D.class);
        assertEquals(1, pntAs2D.getPosition().getLon(), epsilon);
        assertEquals(2, pntAs2D.getPosition().getLat(), epsilon);
    }




}
