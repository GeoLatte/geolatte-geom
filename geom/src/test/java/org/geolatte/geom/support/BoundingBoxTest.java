package org.geolatte.geom.support;

import org.geolatte.geom.Box;
import org.geolatte.geom.C2D;
import org.geolatte.geom.MultiPoint;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WEB_MERCATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoundingBoxTest {

    @Test
    public void checkBBContainsAllPositions(){
        C2D p1 = c(-2, -2);
        C2D p2 = c(-1, -1);
        MultiPoint mp = multipoint(point(WEB_MERCATOR, p1), point(WEB_MERCATOR, p2));
        Box bb = mp.getBoundingBox();
        assertEquals(bb.lowerLeft(), p1);
        assertEquals(bb.upperRight(), p2);
    }
}
