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
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.support.CodecTestBase;
import org.geolatte.geom.support.MySqlUnitTestInputs;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/1/12
 */
public class MySqlCodecUnitTests extends CodecUnitTestBase {

    static final private Logger LOGGER = LoggerFactory.getLogger(MySqlCodecUnitTests.class);

    MySqlUnitTestInputs testCases = new MySqlUnitTestInputs();
    WktDecoder<Geometry> wktDecoder = Wkt.newWktDecoder(Wkt.Dialect.MYSQL_WKT);
    WktEncoder wktEncoder = Wkt.newWktEncoder(Wkt.Dialect.MYSQL_WKT);
    WkbDecoder wkbDecoder = Wkb.newWkbDecoder(Wkb.Dialect.MYSQL_WKB);
    WkbEncoder wkbEncoder = Wkb.newWkbEncoder(Wkb.Dialect.MYSQL_WKB);


    @Test
    public void test_codec_cases_with_srid()  {
        for (Integer testCase : getTestCases().getCases()) {
            ByteBuffer wkb = prependSRID(getTestCases().getWKB(testCase));

            Geometry geom = getWkbDecoder().decode(wkb);
            Geometry expected = addSrid(getTestCases().getExpected(testCase));
            if (expected != null) {
                Assert.assertEquals("WKB decoder gives incorrect result for case: " + testCase, expected, geom);
                Assert.assertEquals("WKB encoder gives incorrect result for case: " + testCase, wkb, getWkbEncoder().encode(geom, ByteOrder.NDR));
                Assert.assertEquals("WKB encoder gives incorrect result for case: " + testCase, wkb, getWkbEncoder().encode(expected, ByteOrder.NDR));
            }
        }
    }

    private Geometry addSrid(Geometry expected)  {
        Geometry geom = null;
        Class gClass = expected.getClass();
        try {
            Constructor cons = gClass.getConstructor(PointSequence.class, CrsId.class, GeometryOperations.class);
            geom =  (Geometry) cons.newInstance(expected.getPoints(), CrsId.valueOf(4326), expected.getGeometryOperations());
        }catch (Exception e){
            LOGGER.warn("failure to create geom", e);
        }
        return geom;
    }

    private ByteBuffer prependSRID(ByteBuffer wkb) {
        byte[] bytes = wkb.toByteArray();
        ByteBuffer withSrid = ByteBuffer.allocate(bytes.length);
        withSrid.setByteOrder(ByteOrder.NDR);
        withSrid.putInt(4326);
        withSrid.setByteOrder(wkb.getByteOrder());
        for (int i = 4; i < bytes.length; i++) {
            withSrid.put(bytes[i]);
        }
        return withSrid;
    }

    @Override
    protected CodecTestBase getTestCases() {
        return testCases;
    }

    @Override
    protected WktDecoder<Geometry> getWktDecoder() {
        return wktDecoder;
    }

    @Override
    protected WktEncoder getWktEncoder() {
        return wktEncoder;
    }

    @Override
    protected WkbDecoder getWkbDecoder() {
        return wkbDecoder;
    }

    @Override
    protected WkbEncoder getWkbEncoder() {
        return wkbEncoder;
    }
}
