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

package org.geolatte.geom.codec.testcases;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.WkbDecoder;
import org.geolatte.geom.codec.WkbEncoder;
import org.geolatte.geom.codec.WktDecoder;
import org.geolatte.geom.codec.WktEncoder;
import org.junit.Test;

import static java.lang.String.format;
import static junit.framework.Assert.assertEquals;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/1/12
 */
abstract public class CodecUnitTestBase {

    @Test
    public void test_decode_wkb() {
        for (Integer idx : getTestCases().getCases()) {
            ByteBuffer wkb = getTestCases().getWKB(idx);
            Geometry<?> geom = getWkbDecoder().decode(wkb);
            String wkt = getTestCases().getWKT(idx);
            assertEquals(format("WKB decoder gives incorrect result for case: %s (%d))", wkt, idx), getTestCases().getExpected(idx), geom);
        }
    }

    @Test
    public void test_encode_wkb() {
        for (Integer idx : getTestCases().getCases()) {
            ByteBuffer wkb = getTestCases().getWKB(idx);
            Geometry<?> geom = getTestCases().getExpected(idx);
            ByteBuffer encoded= getWkbEncoder().encode(geom);
            String wkt = getTestCases().getWKT(idx);
            assertEquals(format("WKB decoder gives incorrect result for case: %s (%d))", wkt, idx), wkb, encoded);
        }
    }

    @Test
    public void test_decode_wkt() {
        for (Integer idx : getTestCases().getCases()) {
            String wkt = getTestCases().getWKT(idx);
            Geometry<?> geom = getWktDecoder().decode(wkt);
            assertEquals(format("WKT decoder gives incorrect result for case: %s (%d))", wkt, idx), getTestCases().getExpected(idx), geom);
        }
    }

    @Test
    public void test_encode_wkt() {
        for (Integer idx : getTestCases().getCases()) {
            if (!getTestCases().getTestEncoding(idx)) {
                break;
            }
            String wkt = getTestCases().getWKT(idx);
            Geometry<?> geom = getTestCases().getExpected(idx);
            String encoded = getWktEncoder().encode(geom);
            assertEquals(format("WKT decoder gives incorrect result for case: %s (%d))", wkt, idx), wkt, encoded);
        }
    }

    abstract protected WktWkbCodecTestBase getTestCases();
    abstract protected WktDecoder getWktDecoder();
    abstract protected WktEncoder getWktEncoder();
    abstract protected WkbDecoder getWkbDecoder();
    abstract protected WkbEncoder getWkbEncoder();

}
