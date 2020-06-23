package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.AbstractGeometryCollection;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.GeographicCoordinateReferenceSystem;

import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class TestSdoGeometryGeometryCollectionDecoder {

    GeographicCoordinateReferenceSystem<G2D> wgs84 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);

    @Test
    public void testGeometryCollection(){
        SDOGeometry sdo = SDOGeometryHelper.sdoGeometry(2004, 4326, null, new int[]{ 1, 1, 1, 3, 2, 1, 7, 1003, 1, 17, 2003, 1} ,
                new Double[]{10.,5.,10.,10.,20.,10.,0.,0.,50.,0.,100.,100.,0.,100.,0.,0.,1.,1.,49.,1.,99.,99.,1.,99.,1.,1.});

        AbstractGeometryCollection<G2D, Geometry<G2D>> geom = geometrycollection(wgs84, point(g(10, 5)), linestring(g(10, 10)
                , g(20, 10)), polygon(
                        ring(g(0, 0), g(50, 0), g(100, 100), g(0, 100), g(0, 0)),
                        ring(g(1, 1), g(49, 1), g(99, 99), g(1, 99), g(1, 1))));

        assertEquals(geom, Decoders.decode(sdo));
    }

}
