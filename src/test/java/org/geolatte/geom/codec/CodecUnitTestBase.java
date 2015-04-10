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

import junit.framework.Assert;
import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.support.WktWkbCodecTestBase;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/1/12
 */
abstract public class CodecUnitTestBase {

    @Test
    public void test_wkt_codec() {
        for (Integer testCase : getTestCases().getCases()) {
            String wkt = getTestCases().getWKT(testCase);
            Geometry geom = getWktDecoder().decode(wkt);
            assertEquals(String.format("Wkt decoder gives incorrect result for case: %d : ", testCase) + wkt, getTestCases().getExpected(testCase), geom);
            if (getTestCases().getTestEncoding(testCase)) {
                Assert.assertEquals("Wkt encoder gives incorrect result for case:" + wkt, wkt, getWktEncoder().encode(geom));
            }
        }
    }

    @Test
    public void test_wkb_codec() {
        for (Integer testCase : getTestCases().getCases()) {
            ByteBuffer wkb = getTestCases().getWKB(testCase);
            Geometry geom = getWkbDecoder().decode(wkb);
            Assert.assertEquals("WKB decoder gives incorrect result for case: " + testCase, getTestCases().getExpected(testCase), geom);
            Assert.assertEquals("WKB encoder gives incorrect result for case: " + testCase, wkb, getWkbEncoder().encode(geom, ByteOrder.NDR));
            Assert.assertEquals("WKB encoder gives incorrect result for case: " + testCase, wkb, getWkbEncoder().encode(getTestCases().getExpected(testCase), ByteOrder.NDR));
        }
    }


    abstract protected WktWkbCodecTestBase getTestCases();

    abstract protected WktDecoder getWktDecoder();
    abstract protected WktEncoder getWktEncoder();

    abstract protected WkbDecoder getWkbDecoder();
    abstract protected WkbEncoder getWkbEncoder();

}
