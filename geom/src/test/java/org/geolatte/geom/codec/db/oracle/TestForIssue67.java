package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.jts.JTS;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.Properties;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/08/2018.
 */
public class TestForIssue67 {

    private Connection conn;

    @Before
    public void before() throws SQLException {
        System.setProperty("GEOLATTE_USE_SDO_POINT_TYPE", "true");
    }

    @After
    public void after() throws SQLException {
        System.setProperty("GEOLATTE_USE_SDO_POINT_TYPE", "false");
    }

    @Test
    public void testArrayStoreExceptionOn() throws SQLException {
        Geometry<G2D> geom = point(WGS84, g(4.96 , 53.56));
        SDOGeometry sdoGeometry = Encoders.encode(geom);
        SDOGeometry expected = new SDOGeometry(SDOGType.parse(2001), 4326, new SDOPoint(4.96, 53.56), null ,null);

    }
}
