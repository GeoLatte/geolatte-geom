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
import org.geolatte.geom.crs.CoordinateReferenceSystems;

import static org.geolatte.geom.builder.DSL.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/1/12
 */
public class MySqlUnitTestInputs extends WktWkbCodecTestBase {

    private static CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;


    public MySqlUnitTestInputs() {
        addCase(1,
                "POINT(10 10)",
                "00000000010100000000000000000024400000000000002440",
                point(crs, c(10, 10)));

        addCase(2,
                "POINT(10 10)",
                "00000000010100000000000000000024400000000000002440",
                point(crs, c(10, 10)));

        addCase(5, //-- broken format
                "MULTIPOINT(11 12, 20 20)",
                "00000000010400000002000000010100000000000000000026400000000000002840010100000000000000000034400000000000003440",
                new MultiPoint<C2D>(point(crs, c(11, 12)), point(crs, c(20, 20))),
                false);

        addCase(13,
                "LINESTRING(10 10,20 20,50 50,34 34)",
                "0000000001020000000400000000000000000024400000000000002440000000000000344000000000000034400000000000004940000000000000494000000000000041400000000000004140",
                new LineString<C2D>(PositionSequenceBuilders.fixedSized(4, crs.getPositionClass()).add(10, 10).add(20, 20).add(50, 50).add(34, 34).toPositionSequence(), crs) );

        Polygon<C2D> polygon2D = polygon(
                ring(crs, c(10, 10), c(20, 10), c(20, 20), c(20, 10), c(10, 10)),
                ring(crs, c(5, 5), c(5, 6), c(6, 6), c(6, 5), c(5, 5)));

        addCase(17,
                "POLYGON((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))",
                "00000000010300000002000000050000000000000000002440000000000000244000000000000034400000000000002440000000000000344000000000000034400000000000003440000000000000244000000000000024400000000000002440050000000000000000001440000000000000144000000000000014400000000000001840000000000000184000000000000018400000000000001840000000000000144000000000000014400000000000001440",
                polygon2D
        );


        addCase(21,
                "MULTIPOLYGON(((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)),((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)))",
                "00000000010600000002000000010300000002000000050000000000000000002440000000000000244000000000000034400000000000002440000000000000344000000000000034400000000000003440000000000000244000000000000024400000000000002440050000000000000000001440000000000000144000000000000014400000000000001840000000000000184000000000000018400000000000001840000000000000144000000000000014400000000000001440010300000002000000050000000000000000002440000000000000244000000000000034400000000000002440000000000000344000000000000034400000000000003440000000000000244000000000000024400000000000002440050000000000000000001440000000000000144000000000000014400000000000001840000000000000184000000000000018400000000000001840000000000000144000000000000014400000000000001440",
                new MultiPolygon<C2D>(polygon2D, polygon2D));


        addCase(25,
                "MULTILINESTRING((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))",
                "0000000001050000000200000001020000000500000000000000000024400000000000002440000000000000344000000000000024400000000000003440000000000000344000000000000034400000000000002440000000000000244000000000000024400102000000050000000000000000001440000000000000144000000000000014400000000000001840000000000000184000000000000018400000000000001840000000000000144000000000000014400000000000001440",
                multilinestring(linestring(crs, c(10, 10), c(20, 10), c(20, 20), c(20, 10), c(10, 10)),
                        linestring(crs, c(5, 5), c(5, 6), c(6, 6), c(6, 5), c(5, 5))));

        addCase(29,
                "GEOMETRYCOLLECTION(POINT(10 10),POINT(20 20))",
                "00000000010700000002000000010100000000000000000024400000000000002440010100000000000000000034400000000000003440",
                geometrycollection(point(crs, c(10, 10)), point(crs, c(20, 20))));

    }

}
