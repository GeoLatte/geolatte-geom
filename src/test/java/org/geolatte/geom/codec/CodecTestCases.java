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

package org.geolatte.geom.codec;

import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class CodecTestCases {


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


    public final Map<Integer, String> wktcases = new HashMap<Integer, String>();
    public final Map<Integer, ByteBuffer> wkbcases = new HashMap<Integer, ByteBuffer>();
    public final Map<Integer, Geometry> expected = new HashMap<Integer, Geometry>();


    public CodecTestCases() {
        addCase(POINT_2D,
                "POINT(1 1)", "0101000000000000000000F03F000000000000F03F",
                Point.create(new double[]{1, 1}, DimensionalFlag.XY, -1));
        addCase(POINT_3D,
                "POINT(1 2 3)",
                "0101000080000000000000F03F00000000000000400000000000000840",
                Point.create(new double[]{1, 2, 3}, DimensionalFlag.XYZ, -1));
        addCase(POINT_3DM,
                "POINT(1 2 3 4)",
                "01010000C0000000000000F03F000000000000004000000000000008400000000000001040",
                Point.create(new double[]{1, 2, 3, 4}, DimensionalFlag.XYZM, -1));
        addCase(POINT_2DM,
                "POINTM(1 2 4)",
                "0101000040000000000000F03F00000000000000400000000000001040",
                Point.create(new double[]{1, 2, 4}, DimensionalFlag.XYM, -1));
        addCase(POINT_WITH_SRID,
                "SRID=4326;POINT(1 2 3 4)",
                "01010000E0E6100000000000000000F03F000000000000004000000000000008400000000000001040",
                Point.create(new double[]{1, 2, 3, 4}, DimensionalFlag.XYZM, 4326));

        PointSequenceBuilder psb = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(2, DimensionalFlag.XY);
        psb.add(-29.261, 66.000).add(-71.1074, -20.255);
        Geometry expected = LineString.create(psb.toPointSequence(), -1);
        addCase(LINESTRING_2D,
                "LINESTRING(-29.261 66,-71.1074 -20.255)",
                "010200000002000000894160E5D0423DC00000000000805040C9E53FA4DFC651C0E17A14AE474134C0",
                expected);

        psb = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(5, DimensionalFlag.XY);
        psb.add(0, 0).add(1, 0).add(1, 1).add(0, 1).add(0, 0);
        expected = Polygon.create(psb.toPointSequence(), -1);
        addCase(POLYGON_2D_NO_INNER_RINGS,
                "POLYGON((0 0,1 0,1 1,0 1,0 0))",
                "0103000000010000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F00000000000000000000000000000000",
                expected);

        PointSequenceBuilder psb1 = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(5, DimensionalFlag.XY);
        psb1.add(0, 0).add(1, 0).add(1, 1).add(0, 1).add(0, 0);
        PointSequenceBuilder psb2 = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(5, DimensionalFlag.XY);
        psb2.add(0.25, 0.25).add(0.25, 0.5).add(0.5, 0.5).add(0.5, 0.25).add(0.25, 0.25);
        LinearRing[] rings = new LinearRing[]{LinearRing.create(psb1.toPointSequence(), -1), LinearRing.create(psb2.toPointSequence(), -1)};
        expected = Polygon.create(rings, -1);
        addCase(POLYGON_2D_INNER_RINGS,
                "POLYGON((0 0,1 0,1 1,0 1,0 0),(0.25 0.25,0.25 0.5,0.5 0.5,0.5 0.25,0.25 0.25))",
                "0103000000020000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F0000000000000000000000000000000005000000000000000000D03F000000000000D03F000000000000D03F000000000000E03F000000000000E03F000000000000E03F000000000000E03F000000000000D03F000000000000D03F000000000000D03F",
                expected);


        Point pnt1 = Point.create(new double[]{1, 1}, DimensionalFlag.XY, -1);
        Point pnt2 = Point.create(new double[]{2, 2}, DimensionalFlag.XY, -1);
        expected = GeometryCollection.create(new Geometry[]{pnt1, pnt2}, -1);
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
                MultiPoint.create(new Point[]{pnt1, pnt2}, -1));

        pnt1 = Point.create(new double[]{1, 2}, DimensionalFlag.XY, 4326);
        pnt2 = Point.create(new double[]{3, 4}, DimensionalFlag.XY, 4326);
        addCase(MULTIPOINT_2D_WITH_SRID,
                "SRID=4326;MULTIPOINT((1 2),(3 4))",
                "0104000020E6100000020000000101000000000000000000F03F0000000000000040010100000000000000000008400000000000001040",
                MultiPoint.create(new Point[]{pnt1, pnt2}, -1));

        PointSequenceBuilder psl1 = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(3, DimensionalFlag.XY);
        psl1.add(1, 2).add(2, 3).add(4, 5);
        PointSequenceBuilder psl2 = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(2, DimensionalFlag.XY);
        psl2.add(6, 7).add(8, 9);
        LineString[] linestrings = new LineString[2];
        linestrings[0] = LineString.create(psl1.toPointSequence(), -1);
        linestrings[1] = LineString.create(psl2.toPointSequence(), -1);
        addCase(MULTILINESTRING_2D,
                "MULTILINESTRING((1 2,2 3,4 5),(6 7,8 9))",
                "010500000002000000010200000003000000000000000000F03F0000000000000040000000000000004000000000000008400000000000001040000000000000144001020000000200000000000000000018400000000000001C4000000000000020400000000000002240"
                , MultiLineString.create(linestrings, -1));

        linestrings = new LineString[2];
        linestrings[0] = LineString.create(psl1.toPointSequence(), 4326);
        linestrings[1] = LineString.create(psl2.toPointSequence(), 4326);
        addCase(MULTILINESTRING_2D_WITH_SRID,
                "SRID=4326;MULTILINESTRING((1 2,2 3,4 5),(6 7,8 9))",
                "0105000020E610000002000000010200000003000000000000000000F03F0000000000000040000000000000004000000000000008400000000000001040000000000000144001020000000200000000000000000018400000000000001C4000000000000020400000000000002240"
                , MultiLineString.create(linestrings, 4326));


        psb = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(5, DimensionalFlag.XY);
        psb.add(0, 0).add(1, 0).add(1, 1).add(0, 1).add(0, 0);
        Polygon[] polygons = new Polygon[2];
        polygons[0] = Polygon.create(psb.toPointSequence(), -1);
        psb1 = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(5, DimensionalFlag.XY);
        psb1.add(0, 0).add(1, 0).add(1, 1).add(0, 1).add(0, 0);
        psb2 = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(5, DimensionalFlag.XY);
        psb2.add(0.25, 0.25).add(0.25, 0.5).add(0.5, 0.5).add(0.5, 0.25).add(0.25, 0.25);
        rings = new LinearRing[]{LinearRing.create(psb1.toPointSequence(), -1), LinearRing.create(psb2.toPointSequence(), -1)};
        polygons[1] = Polygon.create(rings, -1);

        addCase(MULTIPOLYGON_2D,
                "MULTIPOLYGON(((0 0,1 0,1 1,0 1,0 0)),((0 0,1 0,1 1,0 1,0 0),(0.25 0.25,0.25 0.5,0.5 0.5,0.5 0.25,0.25 0.25)))",
                "0106000000020000000103000000010000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F000000000000000000000000000000000103000000020000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F0000000000000000000000000000000005000000000000000000D03F000000000000D03F000000000000D03F000000000000E03F000000000000E03F000000000000E03F000000000000E03F000000000000D03F000000000000D03F000000000000D03F",
                MultiPolygon.create(polygons, -1));

    }


    public void addCase(Integer key, String wkt, String wkb, Geometry geom) {
        this.wktcases.put(key, wkt);
        this.wkbcases.put(key, ByteBuffer.from(wkb));
        this.expected.put(key, geom);
    }

    public String getWKT(Integer name) {
        return this.wktcases.get(name);
    }

    public Geometry getExpected(Integer name) {
        return this.expected.get(name);
    }

    public ByteBuffer getWKB(Integer name) {
        return this.wkbcases.get(name);
    }

}
