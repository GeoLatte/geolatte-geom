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

package org.geolatte.geom;

import org.geolatte.geom.codec.Wkt;

import static org.geolatte.geom.builder.DSL.cM;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.CrsMock.*;
/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/11/12
 */
public class MeasuredTestCases {


    DefaultMeasureGeometryOperations measureOps = new DefaultMeasureGeometryOperations();


    LineString<C2D> lineString2d;
    MultiLineString<C2D> multiLineString2D;
    LineString<C3DM> lineString3DM;
    LineString<C2D> emptyLineString;
    LineString<C2D> linearRing;

    Geometry<C2DM> measuredLineString2D;
    Geometry<C2DM> measuredMultiLineString2D;
    Geometry<C3DM> measuredLineString3DM;
    Geometry<C2DM> measuredLinearRing;
    MultiPoint<C2DM> measuredMultiPoint;
    LineString<C2DM> emptyMeasuredLineString;


    //These test cases are from ISO/IEC 13249-3 (SQL-MM Spatial)
    // section 4.2.1.7.3 0 - Dimensional test cases
    MultiPoint<C2DM> caseD0A = (MultiPoint<C2DM>) Wkt.fromWkt("multipointm((1 0 4), (1 1 1), (1 2 2), (3 1 4), (5 3 4))");
    MultiPoint<C2DM> expectedForD0A = (MultiPoint<C2DM>) Wkt.fromWkt("multipointm((1 0 4), (3 1 4), (5 3 4))");

    MultiPoint<C2DM> caseD0B = (MultiPoint<C2DM>) Wkt.fromWkt("multipointm((1 0 4), (1 1 1), (1 2 2), (3 1 4), (5 3 5), (9 5 3), (7 6 7))");
    MultiPoint<C2DM> expectedForD0B = (MultiPoint<C2DM>) Wkt.fromWkt("multipointm((1 0 4), (1 2 2), (3 1 4), (9 5 3))");

    Point<C2DM> caseD0C = (Point<C2DM>) Wkt.fromWkt("pointm(7 6 7)");
    Point<C2DM> expectedForD0C = new Point<C2DM>(crsM);

    Point<C2DM> caseD0D = (Point<C2DM>) Wkt.fromWkt("pointm(7 6 7)");
    MultiPoint<C2DM> expectedForD0D = (MultiPoint<C2DM>) Wkt.fromWkt("multipointm((7 6 7))");


    //section 4.2.1.7.4 - 1-Dimensional test cases
    LineString<C2DM> caseD1A = (LineString<C2DM>) Wkt.fromWkt("linestringm(1 0 0, 3 1 4, 5 3 4, 5 5 1, 5 6 4, 7 8 4, 9 9 0)");
    MultiLineString<C2DM> expectedForD1A = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((3 1 4, 5 3 4), (5 6 4, 7 8 4))");

    LineString<C2DM> caseD1B = (LineString<C2DM>) Wkt.fromWkt("linestringm(1 0 0, 1 1 1, 1 2 2, 3 1 3, 5 3 4, 9 5 5, 7 6 6)");
    MultiLineString<C2DM> expectedForD1B = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((1 2 2, 3 1 3, 5 3 4))");

    LineString<C2DM> caseD1C = (LineString<C2DM>) Wkt.fromWkt("linestringm(1 0 0, 1 1 1, 1 2 2, 3 1 3, 5 3 4, 9 5 5, 7 6 6)");
    MultiPoint<C2DM> expectedForD1C = (MultiPoint<C2DM>) Wkt.fromWkt("multipointm((7 6 6))");

    LineString<C2DM> caseD1D = (LineString<C2DM>) Wkt.fromWkt("linestringm(0 0 1, 2 2 3, 4 4 2)");
    GeometryCollection<C2DM, Geometry<C2DM>> expectedForD1D = (GeometryCollection<C2DM, Geometry<C2DM>>) Wkt.fromWkt("geometrycollectionm (linestringm(0 0 1, 1 1 2), pointm(4 4 2))");

    MultiLineString<C2DM> caseD1E = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((1 0 0, 1 1 1, 1 2 2, 3 1 3), (4 5 3, 5 3 4, 9 5 5, 7 6 6))");
    MultiLineString<C2DM> expectedForD1E = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((1 2 2, 3 1 3),(4 5 3, 5 3 4))");

    LineString<C2DM> caseD1F = (LineString<C2DM>) Wkt.fromWkt("linestringm(0 0 0, 2 2 2, 4 4 4)");
    MultiLineString<C2DM> expectedForD1F = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((1 1 1, 2 2 2, 3 3 3))");

    MultiLineString<C2DM> caseD1G = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((1 0 0, 1 1 1, 1 2 2, 3 1 3), (4 5 3, 5 3 4, 9 5 5, 7 6 6))");
    Point<C2DM> expectedForD1G = new Point<C2DM>(crsM);

    //Additional 1-Dimensional test cases
    LineString<C2DM> caseLS1 = (LineString<C2DM>) Wkt.fromWkt("linestringm(0 0 0, 1 0 1, 2 0 2, 3 0 3)");
    MultiLineString<C2DM> expectedForLS1 = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((0.7 0 0.7, 1 0 1, 2 0 2, 2.3 0 2.3))");

    LineString<C2DM> caseLS2 = (LineString<C2DM>) Wkt.fromWkt("linestringm(0 0 3, 1 0 2, 2 0 1, 3 0 0)");
    MultiLineString<C2DM> expectedForLS2 = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((0.5 0 2.5, 1 0 2, 2 0 1, 2.3 0 0.7))");

    LineString<C2DM> caseLS3 = (LineString<C2DM>) Wkt.fromWkt("linestringm(0 0 0, 1 0 -1, 2 0 -2, 3 0 -3)");
    MultiLineString<C2DM> expectedForLS3 = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((0.7 0 -0.7, 1 0 -1, 2 0 -2, 2.3 0 -2.3))");

    LineString<C2DM> caseLS4 = (LineString<C2DM>) Wkt.fromWkt("linestringm(0 0 0, 4 0 4, 8 0 0)");
    MultiLineString<C2DM> expectedForLS4 = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((2 0 2, 3 0 3),(5 0 3, 6 0 2))");

    LineString<C2DM> caseLS5 = (LineString<C2DM>) Wkt.fromWkt("linestringm(0 0 0, 0 1 1, 0 2 2, 0 3 3, 0 4 3, 0 5 0, 0 6 1, 0 7 2, 0 8 3)");
    MultiLineString<C2DM> expectedForLS5 = (MultiLineString<C2DM>) Wkt.fromWkt("multilinestringm((0 1.3 1.3, 0 2 2, 0 2.8 2.8)," +
            "(0.0 4.066666666666666 2.8 ,0.0 4.566666666666666 1.3 )," +
            "(0 6.3 1.3, 0 7 2, 0 7.8 2.8))");


    MeasuredTestCases() {
        PositionSequenceBuilder<C2D> psBuilder = PositionSequenceBuilders.fixedSized(4, C2D.class);
        psBuilder.add(0, 0)
                .add(1, 0)
                .add(1, 1)
                .add(2, 1);
        lineString2d = new LineString<C2D>(psBuilder.toPositionSequence(), crs);

        psBuilder = PositionSequenceBuilders.fixedSized(3, C2D.class);
        psBuilder.add(3, 1).add(4, 1).add(5, 1);
        LineString<C2D> test2DLine2 = new LineString<C2D>(psBuilder.toPositionSequence(), crs);
        multiLineString2D = new MultiLineString<C2D>(lineString2d, test2DLine2);


        PositionSequenceBuilder<C3DM> psBuilder3DM = PositionSequenceBuilders.fixedSized(4, C3DM.class);
        psBuilder3DM.add(0, 0, 2, 5)
                .add(1, 0, 3, 10)
                .add(1, 1, 4, 20)
                .add(2, 1, 5, 30);
        lineString3DM = new LineString<C3DM>(psBuilder3DM.toPositionSequence(),crsZM);

        measuredMultiPoint = new MultiPoint<C2DM>(
                point(crsM, cM(0, 0, 1)),
                point(crsM, cM(1, 2, 2))
        );


        psBuilder = PositionSequenceBuilders.fixedSized(5, C2D.class);
        psBuilder.add(0, 0)
                .add(1, 0)
                .add(1, 1)
                .add(0, 1)
                .add(0, 0);
        linearRing = new LineString(psBuilder.toPositionSequence(), crs);


        emptyLineString = new LineString<C2D>(crs);

        //measured lines

        measuredLineString2D = measureOps.measureOnLength(lineString2d, C2DM.class, false);
        measuredMultiLineString2D = measureOps.measureOnLength(multiLineString2D, C2DM.class, false);
        measuredLineString3DM = measureOps.measureOnLength(lineString3DM, C3DM.class, false);
        measuredLinearRing = measureOps.measureOnLength(linearRing, C2DM.class, false);
        emptyMeasuredLineString = (LineString<C2DM>) measureOps.measureOnLength(emptyLineString, C2DM.class, false);
    }

}
