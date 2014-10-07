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
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Unit;

import static org.geolatte.geom.builder.DSL.*;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PostgisTestCases extends WktWkbCodecTestBase {

    private static CoordinateReferenceSystem<P2D> crs = CrsRegistry.getUndefinedProjectedCoordinateReferenceSystem();
    private static CoordinateReferenceSystem<P3D> crsZ = crs.addVerticalAxis(Unit.METER);
    private static CoordinateReferenceSystem<P2DM> crsM = crs.addMeasureAxis(Unit.METER);
    private static CoordinateReferenceSystem<P3DM> crsZM = crsZ.addMeasureAxis(Unit.METER);

    private static CoordinateReferenceSystem<G2D> wgs84 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);
    private static CoordinateReferenceSystem<G3D> wgs84_Z= wgs84.addVerticalAxis(Unit.METER);
    private static CoordinateReferenceSystem<G2DM> wgs84_M = wgs84.addMeasureAxis(Unit.METER);
    private static CoordinateReferenceSystem<G3DM> wgs84_ZM = wgs84_Z.addMeasureAxis(Unit.METER);

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
                point(crs, p(1, 1)));
        addCase(POINT_3D,
                "POINT(1 2 3)",
                "0101000080000000000000F03F00000000000000400000000000000840",
                point(crsZ, p(1,2,3)));
        addCase(POINT_3DM,
                "POINT(1 2 3 4)",
                "01010000C0000000000000F03F000000000000004000000000000008400000000000001040",
                point(crsZM, p(1,2,3,4)));
        addCase(POINT_2DM,
                "POINTM(1 2 4)",
                "0101000040000000000000F03F00000000000000400000000000001040",
                point(crsM, pM(1,2,4)));
        addCase(POINT_WITH_SRID,
                "SRID=4326;POINT(1 2 3 4)",
                "01010000E0E6100000000000000000F03F000000000000004000000000000008400000000000001040",
                point(wgs84_ZM, g(1, 2, 3, 4)));



        Geometry expected = linestring(crs, p(-29.261, 66.000), p(-71.1074, -20.255));
        addCase(LINESTRING_2D,
                "LINESTRING(-29.261 66,-71.1074 -20.255)",
                "010200000002000000894160E5D0423DC00000000000805040C9E53FA4DFC651C0E17A14AE474134C0",
                expected);

        expected = polygon(ring(crs ,p(0, 0),p(1, 0),p(1, 1),p(0, 1),p(0, 0)));
        addCase(POLYGON_2D_NO_INNER_RINGS,
                "POLYGON((0 0,1 0,1 1,0 1,0 0))",
                "0103000000010000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F00000000000000000000000000000000",
                expected);

        expected = polygon(
            ring(crs ,p(0, 0),p(1, 0),p(1, 1),p(0, 1),p(0, 0)),
            ring(crs ,p(0.25, 0.25),p(0.25, 0.5),p(0.5, 0.5),p(0.5, 0.25),p(0.25, 0.25))

        );
        addCase(POLYGON_2D_INNER_RINGS,
                "POLYGON((0 0,1 0,1 1,0 1,0 0),(0.25 0.25,0.25 0.5,0.5 0.5,0.5 0.25,0.25 0.25))",
                "0103000000020000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F0000000000000000000000000000000005000000000000000000D03F000000000000D03F000000000000D03F000000000000E03F000000000000E03F000000000000E03F000000000000E03F000000000000D03F000000000000D03F000000000000D03F",
                expected);


        Point<P2D> pnt1 = point(crs, p(1, 1));
        Point<P2D> pnt2 = point(crs, p(2, 2));
        expected = geometrycollection(pnt1, pnt2 );
        addCase(GEOM_COLL_2D_POINTS,
                "GEOMETRYCOLLECTION(POINT(1 1),POINT(2 2))",
                "0107000000020000000101000000000000000000F03F000000000000F03F010100000000000000000000400000000000000040",
                expected);

        addCase(EMPTY_GEOM_COLL,
                "GEOMETRYCOLLECTION EMPTY",
                "010700000000000000",
                geometrycollection(crs));

        addCase(MULTIPOINT_2D,
                "MULTIPOINT((1 1),(2 2))",
                "0104000000020000000101000000000000000000F03F000000000000F03F010100000000000000000000400000000000000040",
                multipoint( pnt1, pnt2));

        Point<G2D> gpnt1 = point(wgs84, g(1, 2));
        Point<G2D> gpnt2 = point(wgs84, g(3, 4));
        addCase(MULTIPOINT_2D_WITH_SRID,
                "SRID=4326;MULTIPOINT((1 2),(3 4))",
                "0104000020E6100000020000000101000000000000000000F03F0000000000000040010100000000000000000008400000000000001040",
                multipoint(gpnt1, gpnt2));

        addCase(MULTILINESTRING_2D,
                "MULTILINESTRING((1 2,2 3,4 5),(6 7,8 9))",
                "010500000002000000010200000003000000000000000000F03F0000000000000040000000000000004000000000000008400000000000001040000000000000144001020000000200000000000000000018400000000000001C4000000000000020400000000000002240"
                ,
                multilinestring( linestring (crs ,p(1, 2),p(2, 3),p(4, 5) ), linestring(crs ,p(6, 7),p(8, 9) ))
                );


        addCase(MULTILINESTRING_2D_WITH_SRID,
                "SRID=4326;MULTILINESTRING((1 2,2 3,4 5),(6 7,8 9))",
                "0105000020E610000002000000010200000003000000000000000000F03F0000000000000040000000000000004000000000000008400000000000001040000000000000144001020000000200000000000000000018400000000000001C4000000000000020400000000000002240"
                , multilinestring( linestring (wgs84 ,g(1, 2),g(2, 3),g(4, 5) ), linestring(wgs84 ,g(6, 7),g(8, 9) )));




        addCase(MULTIPOLYGON_2D,
                "MULTIPOLYGON(((0 0,1 0,1 1,0 1,0 0)),((0 0,1 0,1 1,0 1,0 0),(0.25 0.25,0.25 0.5,0.5 0.5,0.5 0.25,0.25 0.25)))",
                "0106000000020000000103000000010000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F000000000000000000000000000000000103000000020000000500000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F0000000000000000000000000000000005000000000000000000D03F000000000000D03F000000000000D03F000000000000E03F000000000000E03F000000000000E03F000000000000E03F000000000000D03F000000000000D03F000000000000D03F",
                multipolygon(
                        polygon(ring(crs ,p(0, 0),p(1, 0),p(1, 1),p(0, 1),p(0, 0) )),
                        polygon(ring(crs ,p(0, 0),p(1, 0),p(1, 1),p(0, 1),p(0, 0)),
                                ring(crs ,p(0.25, 0.25),p(0.25, 0.5),p(0.5, 0.5),p(0.5, 0.25),p(0.25, 0.25))))
                );

        expected = linestring(crs, p(-29.261, 66.000), p(-71.1074, -20.255));
        addCase(LINESTRING_IRREGULAR_WHITE_SPACE_1,
                "LINESTRING ( -29.261 66 ,  -71.1074    -20.255     )",
                "010200000002000000894160E5D0423DC00000000000805040C9E53FA4DFC651C0E17A14AE474134C0",
                expected);

        addCase(POINT_SCIENTIFIC_NOTATION,
                "POINT(1e100 1.2345e-100 -2e-5)",
                "01010000807DC39425AD49B25402EBD79DF147312BF168E388B5F8F4BE",
                point(crsZ, p(1e100, 1.23454e-100, -2e-5)));

        addCase(INVALID_POINT,
                "POINT(10,12)",
                "01010000807DC39425AD4",
                new Point<>(crs));

        addCase(INVALID_POLYGON,
                "POLYGON((0 0,1 0,1 1,0 1))",
                "0103000000010000000400000000000000000000000000000000000000000000000000F03F0000000000000000000000000000F03F000000000000F03F0000000000000000000000000000F03F0",
                polygon(crs));

        expected = linestring(crsZM ,p(-29.261, 66.000, 1, 2),p(-71.1074, -20.255, 3, 5));
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
