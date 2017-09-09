package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.builder.DSL;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.ProjectedCoordinateReferenceSystem;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.point;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestWkbDecodingIdempotent {

    ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);

    @Test
    public void testWkbConversionShouldBeStable() {
        Geometry original =  point(crs, DSL.c(68878.8400879, 169038.177124));

        String originalWkb = "01010000208A7A000084020071EDD0F040DBFCBF6A71A20441";

        assertEquals(originalWkb, Wkb.toWkb(original).toString());

        // geom to wkb to geom blijft stabliel
        assertEquals(Wkb.fromWkb(Wkb.toWkb(original)),original);
    }
}
