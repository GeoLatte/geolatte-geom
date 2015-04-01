package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.GeographicCoordinateReferenceSystem;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.builder.DSL.g;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class TestSdoGeometryCollectionEncoder {

    GeographicCoordinateReferenceSystem<G2D> wgs84 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);


    @Test
    public void testGeometryCollection(){
        SDOGeometry expected = SDOGeometryHelper.sdoGeometry(2004, 4326, null, new int[]{ 1, 1, 1, 3, 2, 1, 7, 1003, 1} ,
                new Double[]{10. ,5. , 10. ,10. , 20. ,10. , 10. ,105. , 15. ,105. , 20. ,110. , 10. ,110.,
                        10., 105.});

        GeometryCollection<G2D, Geometry<G2D>> geom = geometrycollection(wgs84, point(g(10, 5)), linestring(g(10, 10)
                , g(20, 10)), polygon(ring(g(10, 105), g(15, 105), g(20, 110), g(10, 110), g(10, 105))));

        assertEquals(expected, Encoders.encode(geom));

    }


}
