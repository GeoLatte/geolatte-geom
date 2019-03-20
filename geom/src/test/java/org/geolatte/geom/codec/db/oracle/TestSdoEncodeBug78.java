package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-20.
 */
public class TestSdoEncodeBug78 {

    @Test
    public void testPolygonWithVerySmallPolygon() {

        Geometry<G2D> poly = Wkt.fromWkt(WKT, CoordinateReferenceSystems.WGS84);

        SdoPolygonEncoder encoder = new SdoPolygonEncoder();

        assertNotNull(encoder.encode(poly));

    }

    static String WKT = "POLYGON((-88.101304825 30.791176733, -88.101303916 30.791176749, -88.101304 30.791177, -88.101304825 30.791176733))";

}
