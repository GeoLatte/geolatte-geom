package org.geolatte.test;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LinearUnit;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WEB_MERCATOR;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.builder.DSL.*;

import org.geolatte.geom.codec.Wkt;

import static org.junit.Assert.assertEquals;

/**
 * Test class for API test and validation
 * <p>
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 16/07/2018.
 */
public class GeometryApiTest {


    @Test
    public void readme_test() {
        Point<G2D> pnt = point(WGS84, g(4.33, 53.21));
        LineString<G2D> lstr = linestring(WGS84, g(4.43, 53.21), g(4.44, 53.20), g(4.45, 53.19));
        Polygon<G2D> pgn = polygon(WGS84, ring(g(4.43, 53.21), g(4.44, 53.22), g(4.43, 53.21)));

        CoordinateReferenceSystem<G3D> wgs84E = WGS84.addVerticalSystem(LinearUnit.METER, G3D.class);
        Point<G3D> pntWithElevation = point(wgs84E, g(4.33, 53.21, 350));
        String wkt = Wkt.toWkt(pnt);
        assertEquals("SRID=4326;POINT(4.33 53.21)", wkt);
        String sfaWkt = Wkt.toWkt(pntWithElevation, Wkt.Dialect.SFA_1_2_1);
        assertEquals("POINT Z (4.33 53.21 350)", sfaWkt);

    }

    @Test
    public void readme_ops() {


        CoordinateReferenceSystem<C2DM> crsM = WEB_MERCATOR.addLinearSystem(LinearUnit.METER, C2DM.class);

        ProjectedGeometryOperations pgo = GeometryOperations.projectedGeometryOperations();
        Point<C2D> point = point(WEB_MERCATOR, c(4.33, 53.21));
        Polygon<C2D> polygon = polygon(WEB_MERCATOR, ring(c(4.43, 53.21), c(4.44, 53.22), c(4.43, 53.21)));
        boolean isPntInPoly = pgo.contains(polygon, point);

        // example  op an operation on measured geometries
        MeasureGeometryOperations mgo = GeometryOperations.measureGeometryOperations();
        LineString<C2DM> linestring = linestring(crsM, cM(4.43, 53.21, 0), cM(4.44, 53.20, 1), cM(4.45, 53.19, 2));
        //create a geometry along the linestring that is between 1.5 and 1.8
        Geometry<C2DM> c2DMGeometry = mgo.locateBetween(linestring, 1.5, 1.8);

    }

    @Test
    public void readme_jts(){
        org.locationtech.jts.geom.Point jtsPoint = JTS.to(point(WGS84, g(4.32, 53.12)));
        org.geolatte.geom.Point<?> glPnt = JTS.from(jtsPoint);

        assertEquals(point(WGS84, g(4.32, 53.12)), glPnt);

        org.geolatte.geom.Point<G2D> glPnt2 = JTS.from(jtsPoint, WGS84);

    }


    @Test
    public void readme_crs(){
        CoordinateReferenceSystem<C2D> crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
        assert crs != null;
        CoordinateReferenceSystem<C3D> crsZ = crs.addVerticalSystem(LinearUnit.METER, C3D.class);
        CoordinateReferenceSystem<C3DM> crsZM = crsZ.addLinearSystem(LinearUnit.METER, C3DM.class);
        assertEquals(4, crsZM.getCoordinateDimension());

    }
}
