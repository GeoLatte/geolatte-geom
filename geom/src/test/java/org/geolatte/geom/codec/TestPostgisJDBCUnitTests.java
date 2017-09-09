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

import org.geolatte.geom.support.WktWkbCodecTestBase;
import org.geolatte.geom.support.PostgisJDBCUnitTestInputs;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 9/29/12
 */
public class TestPostgisJDBCUnitTests extends CodecUnitTestBase {

    PostgisJDBCUnitTestInputs testCases = new PostgisJDBCUnitTestInputs();

    WktDecoder wktDecoder = Wkt.newDecoder(Wkt.Dialect.POSTGIS_EWKT_1);
    WktEncoder wktEncoder = Wkt.newEncoder(Wkt.Dialect.POSTGIS_EWKT_1);
    WkbDecoder wkbDecoder = Wkb.newDecoder(Wkb.Dialect.POSTGIS_EWKB_1);
    WkbEncoder wkbEncoder = Wkb.newEncoder(Wkb.Dialect.POSTGIS_EWKB_1);


    @Override
    protected WktWkbCodecTestBase getTestCases() {
        return testCases;
    }

    @Override
    protected WktDecoder getWktDecoder() {
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
