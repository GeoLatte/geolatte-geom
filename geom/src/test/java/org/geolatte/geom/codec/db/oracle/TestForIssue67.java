package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.jts.JTS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.Properties;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/08/2018.
 */
public class TestForIssue67 {

    private Connection conn;

    @Before
    public void mkConnection() throws SQLException {

        String url = "jdbc:oracle:thin:@localhost:1521/orcl12c";
        Properties props = new Properties();
        props.put("user", "C##hibernate");
        props.put("password", "hibernate");
        conn = DriverManager.getConnection (url, props);
    }

    @After
    public void closeConnection() throws SQLException {
        conn.close();
    }

    @Test
    public void testArrayStoreExceptionOn() throws SQLException {

        final ConnectionFinder finder = new DefaultConnectionFinder();

        System.setProperty("GEOLATTE_USE_SDO_POINT_TYPE", "true");

        Geometry<G2D> geom = point(WGS84, g(5 , 5));
        SDOGeometry sdoGeometry = Encoders.encode(geom);

        Struct struct = new OracleJDBCTypeFactory(finder).createStruct(sdoGeometry, conn);

        System.out.println(struct);

    }
}
