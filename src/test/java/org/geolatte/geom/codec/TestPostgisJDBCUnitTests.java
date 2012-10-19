/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.support.CodecTestInputs;
import org.geolatte.geom.support.PostgisJDBCUnitTestInputs;
import org.geolatte.geom.support.PostgisJDBCWithSRIDTestInputs;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 9/29/12
 */
public class TestPostgisJDBCUnitTests {

    PostgisJDBCUnitTestInputs testCases = new PostgisJDBCUnitTestInputs();
    PostgisJDBCWithSRIDTestInputs testCasesWithSRID = new PostgisJDBCWithSRIDTestInputs();
    WktDecoder<Geometry> wktDecoder = Wkt.newWktDecoder(Wkt.Dialect.POSTGIS_EWKT_1);
    WktEncoder<Geometry> wktEncoder = Wkt.newWktEncoder(Wkt.Dialect.POSTGIS_EWKT_1);
    WkbDecoder wkbDecoder = Wkb.newWkbDecoder(Wkb.Dialect.POSTGIS_EWKB_1);
    WkbEncoder wkbEncoder = Wkb.newWkbEncoder(Wkb.Dialect.POSTGIS_EWKB_1);

    @Test
    public void test_wkt_codec() {
        testWktCase(testCases);
    }

    @Test
    public void test_wkt_with_srid_codec(){
        testWktCase(testCasesWithSRID);
    }

    private void testWktCase(CodecTestInputs inputs) {

        for (Integer testCase : inputs.getCases()) {
            String wkt = inputs.getWKT(testCase);
            Geometry geom = wktDecoder.decode(wkt);
            assertEquals("Wkt decoder gives incorrect result for case: " + wkt, inputs.getExpected(testCase), geom);
            if (inputs.getTestEncoding(testCase)) {
                assertEquals("Wkt encoder gives incorrect result for case:" + wkt, wkt, wktEncoder.encode(geom));
            }
        }
    }


    @Test
    public void test_wkb_codec() {
        for (Integer testCase : testCases.getCases()) {
            ByteBuffer wkb = testCases.getWKB(testCase);
            Geometry geom = wkbDecoder.decode(wkb);
            assertEquals("WKB decoder gives incorrect result for case: " + testCase, testCases.getExpected(testCase), geom);
            assertEquals(wkb, wkbEncoder.encode(geom, ByteOrder.NDR));
        }
    }
}
