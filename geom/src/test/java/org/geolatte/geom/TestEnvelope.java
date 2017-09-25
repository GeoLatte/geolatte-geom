package org.geolatte.geom;

import org.junit.Test;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.CrsMock.crs;
import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.cM;
import static org.geolatte.geom.builder.DSL.point;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 10/08/16.
 */
public class TestEnvelope {

    Point<C2D> point2D = point(crs, c(1, 2));
    Point<C3D> point3D = point(crsZ, c(1, 2, -3));
    Point<C2DM> point2DM = point(crsM, cM(1, 2, 3));
    Point<C3DM> point3DM = point(crsZM, c(1, 2, 3, 4));
    Point<C2D> emptyPoint = new Point<C2D>(crs);


    @Test
    public void testEnvelopeConstruction(){
        Envelope env = new Envelope(0,1,2,3,crsZM);
        C3DM ll = c(0, 1, 0, 0);
        C3DM ur = c(2, 3, 0, 0);
        assertEquals(ll, env.lowerLeft());
        assertEquals(ur, env.upperRight());
    }

    @Test
    public void testEquivalenceConstructionByPosition() {
        Envelope env = new Envelope(0,1,2,3,crsZM);
        C3DM ll = c(0, 1, 3, 9);
        C3DM ur = c(2, 3, 1, Double.NaN);
        Envelope env2 = new Envelope(ur, ll, crsZM);
        assertEquals(env, env2);
    }


    @Test
    public void testEnvelope() {
        assertEquals(new Envelope<C2D>(1, 2, 1, 2, crs), point2D.getEnvelope());
        assertEquals(new Envelope<C3D>(1, 2, 1, 2, crsZ), point3D.getEnvelope());
        assertEquals(new Envelope<C2DM>(1, 2, 1, 2, crsM), point2DM.getEnvelope());
        assertEquals(new Envelope<C3DM>(1, 2, 1, 2, crsZM), point3DM.getEnvelope());

    }

}
