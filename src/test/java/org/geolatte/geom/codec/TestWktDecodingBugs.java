package org.geolatte.geom.codec;

import org.junit.Test;

/**
 * Created by Karel Maesen, Geovise BVBA on 13/03/15.
 */
public class TestWktDecodingBugs {


    @Test(expected=WktDecodeException.class)
    public void test_issue_31(){
        Wkt.newDecoder(Wkt.Dialect.POSTGIS_EWKT_1).decode("POLYGON( 24.881973486328125 60.15671327918588, 24.936218481445312 60.160642536827424, 24.978103857421875 60.16696253094966, 24.881973486328125 60.15671327918588 )");

    }
}
