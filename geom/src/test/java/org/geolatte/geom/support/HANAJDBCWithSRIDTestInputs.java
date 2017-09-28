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

package org.geolatte.geom.support;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasMeasureAxis;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasVerticalAxis;

import static org.geolatte.geom.CrsMock.*;
/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 10/13/12
 */
public class HANAJDBCWithSRIDTestInputs extends WktWkbCodecTestBase {


    public HANAJDBCWithSRIDTestInputs() {
    	HANAJDBCUnitTestInputs base = new HANAJDBCUnitTestInputs();
        for (Integer testCase : base.getCases()) {
            addCase(testCase,
                    "SRID=4326;" + base.getWKT(testCase),
                    toSRIDPrefixedWKB(base, testCase),
                    addCrsId(base, testCase),
                    false
            );
        }
    }

    private Geometry addCrsId(HANAJDBCUnitTestInputs base, Integer testCase) {
        Geometry geom = base.getExpected(testCase);
        CoordinateReferenceSystem<?> crs = null;
        CoordinateReferenceSystem<?> srcCrs = geom.getCoordinateReferenceSystem();

        if (hasVerticalAxis(srcCrs) &&  hasMeasureAxis(srcCrs)) {
            crs = WGS84_ZM;
        } else if (hasVerticalAxis(srcCrs)) {
            crs = WGS84_Z;
        } else if (hasMeasureAxis(srcCrs)) {
            crs = WGS84_M;
        } else {
            crs = WGS84;
        }
        return Geometry.forceToCrs(base.getExpected(testCase), crs);

    }

    private String toSRIDPrefixedWKB(HANAJDBCUnitTestInputs base, Integer testCase) {
        String hexBase = base.getWKBHexString(testCase);
        ByteBuffer inBuffer = ByteBuffer.from(hexBase);
        //get the relevant parts
        inBuffer.setByteOrder(ByteOrder.NDR);
        byte bo = inBuffer.get();
        int type = inBuffer.getInt();
        byte[] bytes = inBuffer.toByteArray();

        //calculate the output size
        int outputSize = 4 + bytes.length;
        ByteBuffer outBuffer = ByteBuffer.allocate(outputSize);

        outBuffer.setByteOrder(ByteOrder.NDR);
        outBuffer.put(bo);
        type |= 0x20000000; // OR with the SRID-flag
        outBuffer.putInt(type);
        //write the srid
        outBuffer.putInt(4326);
        for (int i = 5; i < bytes.length; i++) {
            outBuffer.put(bytes[i]);
        }
        return outBuffer.toString();
    }

}
