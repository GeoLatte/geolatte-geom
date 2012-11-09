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
import org.geolatte.geom.crs.CrsId;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/11/12
 */
public class MeasuredTestCases {

    DefaultMeasureGeometryOperations measureOps = new DefaultMeasureGeometryOperations();


    LineString lineString2d;
    MultiLineString multiLineString2D;
    LineString lineString3DM;
    LineString emptyLineString;
    LineString linearRing;

    Geometry measuredLineString2D;
    Geometry measuredMultiLineString2D;
    Geometry measuredLineString3DM;
    Geometry measuredLinearRing;
    MultiPoint measuredMultiPoint;

    //These test cases are from ISO/IEC 13249-3 (SQL-MM Spatial)
    // section 4.2.1.7.3 0 - Dimensional test cases
    MultiPoint caseD0A  = (MultiPoint)Wkt.fromWkt("multipointm((1 0 4), (1 1 1), (1 2 2), (3 1 4), (5 3 4))");
    MultiPoint expectedForD0A = (MultiPoint)Wkt.fromWkt("multipointm((1 0 4), (3 1 4), (5 3 4))");

    MultiPoint caseD0B = (MultiPoint)Wkt.fromWkt("multipointm((1 0 4), (1 1 1), (1 2 2), (3 1 4), (5 3 5), (9 5 3), (7 6 7))");
    MultiPoint expectedForD0B = (MultiPoint)Wkt.fromWkt("multipointm((1 0 4), (1 2 2), (3 1 4), (9 5 3))");

    Point caseD0C = (Point)Wkt.fromWkt("pointm(7 6 7)");
    Point expectedForD0C = Point.EMPTY;

    Point caseD0D = (Point)Wkt.fromWkt("pointm(7 6 7)");
    MultiPoint expectedForD0D = (MultiPoint)Wkt.fromWkt("multipointm((7 6 7))");


    //section 4.2.1.7.4 - 1-Dimensional test cases
    LineString caseD1A = (LineString)Wkt.fromWkt("linestringm(1 0 0, 3 1 4, 5 3 4, 5 5 1, 5 6 4, 7 8 4, 9 9 0)");
    MultiLineString expectedForD1A = (MultiLineString) Wkt.fromWkt("multilinestringm((3 1 4, 5 3 4), (5 6 4, 7 8 4))");

    LineString caseD1B = (LineString) Wkt.fromWkt("linestringm(1 0 0, 1 1 1, 1 2 2, 3 1 3, 5 3 4, 9 5 5, 7 6 6)");
    MultiLineString expectedForD1B = (MultiLineString) Wkt.fromWkt("multilinestringm((1 2 2, 3 1 3, 5 3 4))");

    LineString caseD1C = (LineString) Wkt.fromWkt("linestringm(1 0 0, 1 1 1, 1 2 2, 3 1 3, 5 3 4, 9 5 5, 7 6 6)");
    MultiPoint expectedForD1C = (MultiPoint)Wkt.fromWkt("multipointm((7 6 6))");

    LineString caseD1D = (LineString) Wkt.fromWkt("linestringm(0 0 1, 2 2 3, 4 4 2)");
    GeometryCollection expectedForD1D = (GeometryCollection) Wkt.fromWkt("geometrycollectionm (linestringm(0 0 1, 1 1 2), pointm(4 4 2))");

    MultiLineString caseD1E = (MultiLineString) Wkt.fromWkt("multilinestringm((1 0 0, 1 1 1, 1 2 2, 3 1 3), (4 5 3, 5 3 4, 9 5 5, 7 6 6))");
    MultiLineString expectedForD1E = (MultiLineString) Wkt.fromWkt("multilinestringm((1 2 2, 3 1 3),(4 5 3, 5 3 4))");

    LineString caseD1F = (LineString) Wkt.fromWkt("linestringm(0 0 0, 2 2 2, 4 4 4)");
    MultiLineString expectedForD1F = (MultiLineString) Wkt.fromWkt("multilinestringm((1 1 1, 2 2 2, 3 3 3))");

    MultiLineString caseD1G = (MultiLineString) Wkt.fromWkt("multilinestringm((1 0 0, 1 1 1, 1 2 2, 3 1 3), (4 5 3, 5 3 4, 9 5 5, 7 6 6))");
    Point expectedForD1G = Point.EMPTY;

    //Additional 1-Dimensional test cases
    LineString caseLS1 = (LineString) Wkt.fromWkt("linestringm(0 0 0, 1 0 1, 2 0 2, 3 0 3)");
    MultiLineString expectedForLS1 = (MultiLineString) Wkt.fromWkt("multilinestringm((0.7 0 0.7, 1 0 1, 2 0 2, 2.3 0 2.3))");

    LineString caseLS2 = (LineString) Wkt.fromWkt("linestringm(0 0 3, 1 0 2, 2 0 1, 3 0 0)");
    MultiLineString expectedForLS2 = (MultiLineString) Wkt.fromWkt("multilinestringm((0.5 0 2.5, 1 0 2, 2 0 1, 2.3 0 0.7))");

    LineString caseLS3 = (LineString) Wkt.fromWkt("linestringm(0 0 0, 1 0 -1, 2 0 -2, 3 0 -3)");
    MultiLineString expectedForLS3 = (MultiLineString) Wkt.fromWkt("multilinestringm((0.7 0 -0.7, 1 0 -1, 2 0 -2, 2.3 0 -2.3))");

    LineString caseLS4 = (LineString) Wkt.fromWkt("linestringm(0 0 0, 4 0 4, 8 0 0)");
    MultiLineString expectedForLS4 = (MultiLineString) Wkt.fromWkt("multilinestringm((2 0 2, 3 0 3),(5 0 3, 6 0 2))");

    LineString caseLS5 = (LineString) Wkt.fromWkt("linestringm(0 0 0, 0 1 1, 0 2 2, 0 3 3, 0 4 3, 0 5 0, 0 6 1, 0 7 2, 0 8 3)");
    MultiLineString expectedForLS5 = (MultiLineString) Wkt.fromWkt("multilinestringm((0 1.3 1.3, 0 2 2, 0 2.8 2.8)," +
            "(0.0 4.066666666666666 2.8 ,0.0 4.566666666666666 1.3 )," +
            "(0 6.3 1.3, 0 7 2, 0 7.8 2.8))");



    MeasuredTestCases(){
         PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(4, DimensionalFlag.d2D);
        psBuilder.add(0, 0)
                .add(1, 0)
                .add(1, 1)
                .add(2, 1);
        lineString2d = new LineString(psBuilder.toPointSequence(), CrsId.parse("EPSG:4326"));

        psBuilder = PointSequenceBuilders.fixedSized(3, DimensionalFlag.d2D);
        psBuilder.add(3, 1).add(4, 1).add(5, 1);
        LineString test2DLine2 = new LineString(psBuilder.toPointSequence(), CrsId.parse("EPSG:4326"));
        multiLineString2D = new MultiLineString(new LineString[]{lineString2d, test2DLine2});


        psBuilder = PointSequenceBuilders.fixedSized(4, DimensionalFlag.d3DM);
        psBuilder.add(0, 0, 2, 5)
                .add(1, 0, 3, 10)
                .add(1, 1, 4, 20)
                .add(2, 1, 5, 30);
        lineString3DM = new LineString(psBuilder.toPointSequence(), CrsId.parse("EPSG:4326"));

        measuredMultiPoint = new MultiPoint(new Point[]{
                Points.createMeasured(0,0,1, CrsId.UNDEFINED),
                Points.createMeasured(1,2,2, CrsId.UNDEFINED)
                });


        psBuilder = PointSequenceBuilders.fixedSized(5, DimensionalFlag.d2DM);
        psBuilder.add(0, 0, 0)
                .add(1, 0, 0)
                .add(1, 1, 0)
                .add(0, 1, 0)
                .add(0, 0, 0);
        linearRing = new LineString(psBuilder.toPointSequence(), null);



        emptyLineString = LineString.createEmpty();

        //measured lines
        measuredLineString2D = measureOps.createMeasureOnLengthOp(lineString2d, false).execute();
        measuredMultiLineString2D = measureOps.createMeasureOnLengthOp(multiLineString2D, false).execute();
        measuredLineString3DM = measureOps.createMeasureOnLengthOp(lineString3DM, false).execute();
        measuredLinearRing = measureOps.createMeasureOnLengthOp(linearRing, false).execute();
    }

}
