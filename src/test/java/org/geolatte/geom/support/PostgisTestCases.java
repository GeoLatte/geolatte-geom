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
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.support;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CrsId;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PostgisTestCases extends CodecTestBase {


    public static final Integer POINT_2D = 0;
    public static final Integer POINT_3D = 1;
    public static final Integer POINT_3DM = 2;
    public static final Integer POINT_2DM = 3;
    public static final Integer POINT_WITH_SRID = 4;
    public static final Integer LINESTRING_2D = 5;
    public static final Integer POLYGON_2D_NO_INNER_RINGS = 6;
    public static final Integer POLYGON_2D_INNER_RINGS = 7;
    public static final Integer GEOM_COLL_2D_POINTS = 8;
    public static final Integer EMPTY_GEOM_COLL = 9;
    public static final Integer MULTIPOINT_2D = 10;
    public static final Integer MULTIPOINT_2D_WITH_SRID = 11;
    public static final Integer MULTILINESTRING_2D = 12;
    public static final Integer MULTILINESTRING_2D_WITH_SRID = 13;
    public static final Integer MULTIPOLYGON_2D = 14;
    public static final Integer LINESTRING_IRREGULAR_WHITE_SPACE_1 = 15;
    public static final Integer POINT_SCIENTIFIC_NOTATION = 16;
    public static final Integer INVALID_POINT = 17;
    public static final Integer INVALID_POLYGON = 18;
    public static final Integer LINESTRING_3DM = 19;
    public static final Integer EMPTY_POINT = 20;

    public PostgisTestCases() {
        addCase(POINT_2D,
                "POINT(1 1)", "0101000000000000000000F03F000000000000F03F",
                Points.create(1, 1, CrsId.UNDEFINED));
        addCase(POINT_3D,
                "POINT(1 2 3)",
                "0101000080000000000000F03F00000000000000400000000000000840",
                Points.create3D(1, 2, 3, CrsId.UNDEFINED));
        addCase(POINT_3DM,
                "POINT(1 2 3 4)",
                "01010000C0000000000000F03F000000000000004000000000000008400000000000001040",
                Points.create(1, 2, 3, 4, CrsId.UNDEFINED));
        addCase(POINT_2DM,
                "POINTM(1 2 4)",
                "0101000040000000000000F03F00000000000000400000000000001040",
                Points.createMeasured(1, 2, 4, CrsId.UNDEFINED));
        addCase(POINT_WITH_SRID,
                "SRID=4326;POINT(1 2 3 4)",
                "01010000E0E6100000000000000000F03F000000000000004000000000000008400000000000001040",
                Points.create(1, 2, 3, 4, CrsId.valueOf(4326)));

        PointSequenceBuilder psb = PointSequenceBuilders.fixedSized(2, DimensionalFlag.d2D);
        psb.add(-29.261, 66.000).add(-71.1074, -20.255);
        Geometry expected = new LineString(psb.toPointSequence(), CrsId.UNDEFINED);
        addCase(LINESTRING_2D,
                "LINESTRING(-29.261 66,-71.1074 -20.255)",
                "010200000002000000894160E5D0423DC00000000000805040C9E53FA4DFC651C0E17A14AE474134C0",
                expected);

        psb = PointSequenceBuilders.fixedSized(5, DimensionalFlag.d2D);
        psb.add(0, 0).add(1, 0).add(1, 1).add(0, 1).add(0, 0);
        expected = new Polygon(psb.toPointSequence(), CrsId.UNDEFINED);
        addCase(POLYGON_2D_NO_INNER_RINGS,
                "POLYGON((0 0,1 0,1 1,0 1,0 0))",
                "0103000000010000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F00000000000000000000000000000000",
                expected);

        PointSequenceBuilder psb1 = PointSequenceBuilders.fixedSized(5, DimensionalFlag.d2D);
        psb1.add(0, 0).add(1, 0).add(1, 1).add(0, 1).add(0, 0);
        PointSequenceBuilder psb2 = PointSequenceBuilders.fixedSized(5, DimensionalFlag.d2D);
        psb2.add(0.25, 0.25).add(0.25, 0.5).add(0.5, 0.5).add(0.5, 0.25).add(0.25, 0.25);
        LinearRing[] rings = new LinearRing[]{new LinearRing(psb1.toPointSequence(), CrsId.UNDEFINED), new LinearRing(psb2.toPointSequence(), CrsId.UNDEFINED)};
        expected = new Polygon(rings);
        addCase(POLYGON_2D_INNER_RINGS,
                "POLYGON((0 0,1 0,1 1,0 1,0 0),(0.25 0.25,0.25 0.5,0.5 0.5,0.5 0.25,0.25 0.25))",
                "0103000000020000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F0000000000000000000000000000000005000000000000000000D03F000000000000D03F000000000000D03F000000000000E03F000000000000E03F000000000000E03F000000000000E03F000000000000D03F000000000000D03F000000000000D03F",
                expected);


        Point pnt1 = Points.create(1, 1, CrsId.UNDEFINED);
        Point pnt2 = Points.create(2, 2, CrsId.UNDEFINED);
        expected = new GeometryCollection(new Geometry[]{pnt1, pnt2});
        addCase(GEOM_COLL_2D_POINTS,
                "GEOMETRYCOLLECTION(POINT(1 1),POINT(2 2))",
                "0107000000020000000101000000000000000000F03F000000000000F03F010100000000000000000000400000000000000040",
                expected);

        addCase(EMPTY_GEOM_COLL,
                "GEOMETRYCOLLECTION EMPTY",
                "010700000000000000",
                GeometryCollection.createEmpty());

        addCase(MULTIPOINT_2D,
                "MULTIPOINT((1 1),(2 2))",
                "0104000000020000000101000000000000000000F03F000000000000F03F010100000000000000000000400000000000000040",
                new MultiPoint(new Point[]{pnt1, pnt2}));

        pnt1 = Points.create(1, 2, CrsId.valueOf(4326));
        pnt2 = Points.create(3, 4, CrsId.valueOf(4326));
        addCase(MULTIPOINT_2D_WITH_SRID,
                "SRID=4326;MULTIPOINT((1 2),(3 4))",
                "0104000020E6100000020000000101000000000000000000F03F0000000000000040010100000000000000000008400000000000001040",
                new MultiPoint(new Point[]{pnt1, pnt2}));

        PointSequenceBuilder psl1 = PointSequenceBuilders.fixedSized(3, DimensionalFlag.d2D);
        psl1.add(1, 2).add(2, 3).add(4, 5);
        PointSequenceBuilder psl2 = PointSequenceBuilders.fixedSized(2, DimensionalFlag.d2D);
        psl2.add(6, 7).add(8, 9);
        LineString[] linestrings = new LineString[2];
        linestrings[0] = new LineString(psl1.toPointSequence(), CrsId.UNDEFINED);
        linestrings[1] = new LineString(psl2.toPointSequence(), CrsId.UNDEFINED);
        addCase(MULTILINESTRING_2D,
                "MULTILINESTRING((1 2,2 3,4 5),(6 7,8 9))",
                "010500000002000000010200000003000000000000000000F03F0000000000000040000000000000004000000000000008400000000000001040000000000000144001020000000200000000000000000018400000000000001C4000000000000020400000000000002240"
                , new MultiLineString(linestrings));

        linestrings = new LineString[2];
        linestrings[0] = new LineString(psl1.toPointSequence(), CrsId.valueOf(4326));
        linestrings[1] = new LineString(psl2.toPointSequence(), CrsId.valueOf(4326));
        addCase(MULTILINESTRING_2D_WITH_SRID,
                "SRID=4326;MULTILINESTRING((1 2,2 3,4 5),(6 7,8 9))",
                "0105000020E610000002000000010200000003000000000000000000F03F0000000000000040000000000000004000000000000008400000000000001040000000000000144001020000000200000000000000000018400000000000001C4000000000000020400000000000002240"
                , new MultiLineString(linestrings));


        psb = PointSequenceBuilders.fixedSized(5, DimensionalFlag.d2D);
        psb.add(0, 0).add(1, 0).add(1, 1).add(0, 1).add(0, 0);
        Polygon[] polygons = new Polygon[2];
        polygons[0] = new Polygon(psb.toPointSequence(), CrsId.UNDEFINED);
        psb1 = PointSequenceBuilders.fixedSized(5, DimensionalFlag.d2D);
        psb1.add(0, 0).add(1, 0).add(1, 1).add(0, 1).add(0, 0);
        psb2 = PointSequenceBuilders.fixedSized(5, DimensionalFlag.d2D);
        psb2.add(0.25, 0.25).add(0.25, 0.5).add(0.5, 0.5).add(0.5, 0.25).add(0.25, 0.25);
        rings = new LinearRing[]{new LinearRing(psb1.toPointSequence(), CrsId.UNDEFINED), new LinearRing(psb2.toPointSequence(), CrsId.UNDEFINED)};
        polygons[1] = new Polygon(rings);

        addCase(MULTIPOLYGON_2D,
                "MULTIPOLYGON(((0 0,1 0,1 1,0 1,0 0)),((0 0,1 0,1 1,0 1,0 0),(0.25 0.25,0.25 0.5,0.5 0.5,0.5 0.25,0.25 0.25)))",
                "0106000000020000000103000000010000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F000000000000000000000000000000000103000000020000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F0000000000000000000000000000000005000000000000000000D03F000000000000D03F000000000000D03F000000000000E03F000000000000E03F000000000000E03F000000000000E03F000000000000D03F000000000000D03F000000000000D03F",
                new MultiPolygon(polygons));

        psb = PointSequenceBuilders.fixedSized(2, DimensionalFlag.d2D);
        psb.add(-29.261, 66.000).add(-71.1074, -20.255);
        expected = new LineString(psb.toPointSequence(), CrsId.UNDEFINED);
        addCase(LINESTRING_IRREGULAR_WHITE_SPACE_1,
                "LINESTRING ( -29.261 66 ,  -71.1074    -20.255     )",
                "010200000002000000894160E5D0423DC00000000000805040C9E53FA4DFC651C0E17A14AE474134C0",
                expected);

        addCase(POINT_SCIENTIFIC_NOTATION,
                "POINT(1e100 1.2345e-100 -2e-5)",
                "01010000807DC39425AD49B25402EBD79DF147312BF168E388B5F8F4BE",
                Points.create3D(1e100, 1.23454e-100, -2e-5, CrsId.UNDEFINED));

        addCase(INVALID_POINT,
                "POINT(10,12)",
                "01010000807DC39425AD4",
                Points.createEmpty());

        addCase(INVALID_POLYGON,
                "POLYGON((0 0,1 0,1 1,0 1))",
                "0103000000010000000400000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F0",
                Polygon.createEmpty());

        psb = PointSequenceBuilders.fixedSized(2, DimensionalFlag.d3DM);
        psb.add(-29.261, 66.000, 1, 2).add(-71.1074, -20.255, 3, 5);
        expected = new LineString(psb.toPointSequence(), CrsId.UNDEFINED);
        addCase(LINESTRING_3DM,
                "LINESTRING(-29.261 66 1 2, -71.1074 -20.255 3 5)",
                "01020000C002000000894160E5D0423DC00000000000805040000000000000F03F0000000000000040C9E53FA4DFC651C0E17A14AE474134C000000000000008400000000000001440",
                expected);

        addCase(EMPTY_POINT,
                "POINT EMPTY",
                "010700000000000000",
                expected);


    }


}
