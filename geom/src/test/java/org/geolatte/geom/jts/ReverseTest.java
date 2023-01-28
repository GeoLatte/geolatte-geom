package org.geolatte.geom.jts;

import org.geolatte.geom.C2DM;
import org.geolatte.geom.GeometryOperations;
import org.geolatte.geom.ProjectedGeometryOperations;
import org.geolatte.geom.codec.Wkt;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;


/**
 * Created by Karel Maesen, Geovise BVBA on 23/04/16.
 */
public class ReverseTest {

    @Test
    public void testReverseLineString2D() {
        LineString ls = JTS.to(linestring(crs, c(1, 2), c(2, 3), c(4, 5)));
        LineString expected = JTS.to(linestring(crs, c(4, 5), c(2, 3), c(1, 2)));
        assertEquals(expected, ls.reverse());
    }


    @Test
    public void testReverseLineString3D() {
        LineString ls =  JTS.to(linestring(crsZ, c(1, 2, 3), c(2, 3, 4), c(4, 5, 6)));
        LineString expected = JTS.to(linestring(crs, c(4, 5, 6), c(2, 3, 4), c(1, 2, 3)));
        assertEquals(expected, ls.reverse());
    }

    @Test
    public void testReverse2DM() {
        org.geolatte.geom.LineString<C2DM> ls =
                linestring(crsM, cM(1, 1, 1), cM(2, 2, 2), cM(3, 3, 3));

        ProjectedGeometryOperations ops = GeometryOperations.projectedGeometryOperations();
        org.geolatte.geom.LineString<C2DM> reversed = ops.reverse(ls);
        assertEquals(linestring(crsM, cM(3, 3, 3), cM(2, 2, 2), cM(1, 1, 1)),
                reversed);
    }

    @Test
    public void testReverse2DMJts() {
        Geometry geometry = JTS.to(Wkt.fromWkt("LINESTRING M (1 1 1, 2 2 2, 3 3 3)"));
        Geometry reversed = geometry.reverse();

        //note: we can't test with assertEquals() because JTS only checks equality in 2D!
        for (int i = 0; i < geometry.getNumPoints(); i++) {
            assertEquals(geometry.getCoordinates()[i].getOrdinate(2),
                    reversed.getCoordinates()[geometry.getNumPoints() - 1 - i].getOrdinate(2), 0.001);
        }
    }
}
