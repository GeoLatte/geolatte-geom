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

import static junit.framework.Assert.assertEquals;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.support.HANAJDBCUnitTestInputs;
import org.geolatte.geom.support.HANAJDBCWithSRIDTestInputs;
import org.geolatte.geom.support.WktWkbCodecTestBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;

/**
 * @author Jonathan Bregler
 */
public class HANACodecUnitTests extends CodecUnitTestBase {

	static final private Logger LOGGER = LoggerFactory.getLogger(HANACodecUnitTests.class);

	HANAJDBCWithSRIDTestInputs testCases = new HANAJDBCWithSRIDTestInputs();
	WktDecoder wktDecoder = Wkt.newDecoder(Wkt.Dialect.HANA_EWKT);
	WktEncoder wktEncoder = Wkt.newEncoder(Wkt.Dialect.HANA_EWKT);
	WkbDecoder wkbDecoder = Wkb.newDecoder(Wkb.Dialect.HANA_EWKB);
	WkbEncoder wkbEncoder = Wkb.newEncoder(Wkb.Dialect.HANA_EWKB);
	
    @Test
    public void test_wkt_codec_without_srid() {
    	HANAJDBCUnitTestInputs testCases = new HANAJDBCUnitTestInputs();
        for (Integer testCase : testCases.getCases()) {
            String wkt = addSRID(testCases.getWKT(testCase));
            Geometry geom = getWktDecoder().decode(wkt);
            assertEquals(String.format("Wkt decoder gives incorrect result for case: %d : ", testCase) + wkt, testCases.getExpected(testCase), geom);
            if (testCases.getTestEncoding(testCase)) {
                Assert.assertEquals("Wkt encoder gives incorrect result for case:" + wkt, wkt, getWktEncoder().encode(geom));
            }
        }
    }

    @Test
    public void test_wkb_codec_without_srid() {
    	HANAJDBCUnitTestInputs testCases = new HANAJDBCUnitTestInputs();
        for (Integer testCase : testCases.getCases()) {
            ByteBuffer wkb = addSRID(testCases.getWKB(testCase));
            Geometry geom = getWkbDecoder().decode(wkb);
            Assert.assertEquals("WKB decoder gives incorrect result for case: " + testCase, testCases.getExpected(testCase), geom);
            Assert.assertEquals("WKB encoder gives incorrect result for case: " + testCase, wkb, getWkbEncoder().encode(geom, ByteOrder.NDR));
            Assert.assertEquals("WKB encoder gives incorrect result for case: " + testCase, wkb, getWkbEncoder().encode(testCases.getExpected(testCase), ByteOrder.NDR));
        }
    }
    
    private ByteBuffer addSRID(ByteBuffer wkb) {
    	byte[] bytes = wkb.toByteArray();
    	byte[] eBytes = new byte[bytes.length + 4];
    	
    	eBytes[0] = bytes[0]; // byte order
    	if (eBytes[0] == 1) {
    		// little endian
    		eBytes[1] = bytes[1];
    		eBytes[2] = bytes[2];
    		eBytes[3] = bytes[3];
    		eBytes[4] = (byte)(bytes[4] | 0x20);
    	}
    	else {
    		// big endian
    		eBytes[1] = (byte)(bytes[1] | 0x20);
    		eBytes[2] = bytes[2];
    		eBytes[3] = bytes[3];
    		eBytes[4] = bytes[4];
    	}
    	
    	// add SRID 0
		eBytes[5] = 0;
		eBytes[6] = 0;
		eBytes[7] = 0;
		eBytes[8] = 0;
    	
		// add remaining data
    	for (int i = 5 ; i < bytes.length ; i++) {
    		eBytes[i+4] = bytes[i];
    	}

    	return ByteBuffer.from( eBytes );
    }
    
    private String addSRID(String wkt) {
    	return "SRID=0;" + wkt;
    }

	@Override
	protected WktWkbCodecTestBase getTestCases() {
		return this.testCases;
	}

	@Override
	protected WktDecoder getWktDecoder() {
		return this.wktDecoder;
	}

	@Override
	protected WktEncoder getWktEncoder() {
		return this.wktEncoder;
	}

	@Override
	protected WkbDecoder getWkbDecoder() {
		return this.wkbDecoder;
	}

	@Override
	protected WkbEncoder getWkbEncoder() {
		return this.wkbEncoder;
	}
}
