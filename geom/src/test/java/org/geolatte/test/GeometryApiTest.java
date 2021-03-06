package org.geolatte.test;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.LinearUnit;
import org.junit.Test;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.builder.DSL.*;

import org.geolatte.geom.codec.Wkt;

import static org.junit.Assert.assertEquals;

/**
 * Test class for API test and validation
 *
 *
 * Created by Karel Maesen, Geovise BVBA on 16/07/2018.
 */
public class GeometryApiTest {




    @Test
    public void readme_test(){
        Point<G2D> pnt = point(WGS84, g(4.33,53.21));
        LineString<G2D> lstr = linestring(WGS84, g(4.43, 53.21), g(4.44, 53.20), g(4.45, 53.19));
        Polygon<G2D> pgn = polygon(WGS84, ring(g(4.43, 53.21), g(4.44, 53.22), g(4.43, 53.21)));

        CoordinateReferenceSystem<G3D> wgs84E =  WGS84.addVerticalSystem(LinearUnit.METER, G3D.class);
        Point<G3D> pntWithElevation = point(wgs84E, g(4.33, 53.21, 350));
        String wkt = Wkt.toWkt(pnt);
        assertEquals("SRID=4326;POINT(4.33 53.21)", wkt);
        String sfaWkt = Wkt.toWkt(pntWithElevation, Wkt.Dialect.SFA_1_2_1);
        assertEquals("POINT Z (4.33 53.21 350)", sfaWkt);


    }

}
