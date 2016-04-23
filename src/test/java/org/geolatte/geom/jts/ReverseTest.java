package org.geolatte.geom.jts;

import com.vividsolutions.jts.geom.LineString;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by Karel Maesen, Geovise BVBA on 23/04/16.
 */
public class ReverseTest {

    @Test
    public void testReverseLineString2D(){
        LineString ls = (LineString) JTS.to(linestring(crs, c(1, 2), c(2, 3), c(4, 5)));
        LineString expected = (LineString) JTS.to(linestring(crs, c(4, 5), c(2, 3), c(1, 2)));
        assertEquals(expected, ls.reverse());
    }


    @Test
    public void testReverseLineString3D(){
        LineString ls = (LineString) JTS.to(linestring(crsZ, c(1, 2, 3), c(2, 3, 4), c(4, 5, 6)));
        LineString expected = (LineString) JTS.to(linestring(crs, c(4, 5, 6), c(2, 3, 4), c(1, 2, 3)));
        assertEquals(expected, ls.reverse());
    }

}
