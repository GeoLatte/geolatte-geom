package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.*;
import static org.junit.Assert.*;

import org.geolatte.geom.crs.LinearUnit;
import org.junit.Test;

/**
 * Created by Karel Maesen, Geovise BVBA on 02/06/2020.
 */
public class BoxTest {

    CoordinateReferenceSystem<G3D> WGS = addVerticalSystem(WGS84, G3D.class, LinearUnit.METER);
    CoordinateReferenceSystem<C2DM> PRM = PROJECTED_2DM_METER;

    @Test
    public void checkEmpty(){
        Box<G3D> box = Box.mkEmpty(WGS);
        Box<G3D> box2 = Box.mkEmpty(WGS);
        assert (box.lowerLeft().isEmpty());
        assert (box.upperRight().isEmpty());
        assertEquals(box, box2);

        Box<G3D> boxNe = new Box(new G3D(1.0, 1.0), new G3D(10.0, 10.0), WGS);
        assertFalse(box.equals(boxNe));

    }

    @Test
    public void checkUnion(){
        Box<C2DM> box1 = new Box(new C2DM(1.0, 1.0, 1.0), new C2DM(10.0, 10.0, 10.0), PRM);
        Box<C2DM> box2 = new Box(new C2DM(0.0, 5.0, 0.0), new C2DM(5.0, 20, 9.0), PRM);
        Box<C2DM> expected = new Box(new C2DM(0.0, 1.0, 0.0), new C2DM(10.0, 20, 10.0), PRM);

        assertEquals(expected, box1.union(box2));
        assertEquals(expected, box2.union(box1));
        Box<C2DM> unioned = box1.union(box2);
        assert (unioned.contains(box1) && unioned.contains(box2));
    }

    @Test
    public void checkIntersection(){
        Box<C2DM> box1 = new Box(new C2DM(1.0, 1.0, 1.0), new C2DM(10.0, 10.0, 10.0), PRM);
        Box<C2DM> box2 = new Box(new C2DM(0.0, 5.0, 0.0), new C2DM(5.0, 20, 9.0), PRM);
        Box<C2DM> expected = new Box(new C2DM(1.0, 5.0, 1.0), new C2DM(5.0, 10, 9.0), PRM);

        assertEquals(expected, box1.intersect(box2));
        assertEquals(expected, box2.intersect(box1));
        Box<C2DM> intersection = box1.intersect(box2);
        assert (box1.contains(intersection) && box2.contains(intersection));
    }



}
