package org.geolatte.geom.codec.testcases;

import org.geolatte.geom.C2D;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;

import static org.geolatte.geom.builder.DSL.*;

public class Sfa121WktWkbTestCases extends WktWkbCodecTestBase {

    public static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    public Sfa121WktWkbTestCases() {

        addCase(1,
                "POINT(1.52 2.43)",
                "010100000052b81e85eb51f83f713d0ad7a3700340",
                point(crs, c(1.52, 2.43))
        );

        addCase(2,
                "POINT Z (3 4 5)",
                "01E9030000000000000000084000000000000010400000000000001440",
                point(crsZ, c(3.0, 4.0, 5.0))
        );

        addCase(3,
                "POINT M (1 2 3)",
                "01D1070000000000000000F03F00000000000000400000000000000840",
                point(crsM, cM(3.0, 4.0, 5.0))
        );

        addCase(4,
                "POINT ZM (1 2 3 4)",
                "01B90B0000000000000000F03F000000000000004000000000000008400000000000001040",
                point(crsZM, c(1.0, 2.0, 3.0, 4.0))
        );

        //TODO -- change this to points with NaN for empty points
        addCase(5,
                "POINT EMPTY",
                "010400000000000000",
                point(crs)
        );

        addCase(6,
                "POINT Z EMPTY",
                "01EC03000000000000",
                point(crsZ));

        addCase(7,
                "POINT M EMPTY",
                "01D407000000000000",
                point(crsM));

        addCase(8,
                "POINT ZM EMPTY",
                "01BC0B000000000000",
                point(crsZM));


        addCase(9,
                "LINESTRING (1.52 2.43,4.23 5.32)",
                "01020000000200000000000000EC51F83F00000000A47003400000000085EB104000000000AE471540",
                linestring(crs, c(1.52, 2.43), c(4.23, 5.32))
        );

        addCase(10,
                "LINESTRING Z (1.52 2.43 1,4.23 5.32 3)",
                "01EA0300000200000000000000EC51F83F00000000A4700340000000000000F03F0000000085EB104000000000AE4715400000000000000840",
                linestring(crsZ, c(1.52, 2.43, 1.0), c(4.23, 5.32, 3.0))
        );

        addCase(11,
                "LINESTRING M (1.52 2.43 1,4.23 5.32 3)",
                "01D20700000200000000000000EC51F83F00000000A4700340000000000000F03F0000000085EB104000000000AE4715400000000000000840",
                linestring(crsM, cM(1.52, 2.43, 1.0), cM(4.23, 5.32, 3.0))
        );

        addCase(12,
                "LINESTRING ZM (1.52 2.43 1 2,4.23 5.32 3 4)",
                "01BA0B00000200000000000000EC51F83F00000000A4700340000000000000F03F00000000000000400000000085EB104000000000AE47154000000000000008400000000000001040",
                linestring(crsZM, c(1.52, 2.43, 1.0, 2.0), c(4.23, 5.32, 3.0, 4.0))
        );

        addCase(13,
                "LINESTRING EMPTY",
                "010200000000000000",
                linestring(crs)
        );

        addCase(14,
                "LINESTRING Z EMPTY",
                "01EA03000000000000",
                linestring(crsZ));

        addCase(15,
                "LINESTRING M EMPTY",
                "01D207000000000000",
                linestring(crsM)
        );

        addCase(16,
                "LINESTRING ZM EMPTY",
                "01BA0B000000000000",
                linestring(crsZM)
        );

        addCase(17,
                "POLYGON ((10 10,20 15,20 20,10 20,10 10))",
                "010300000001000000050000000000000000002440000000000000244000000000000034400000000000002E40000000000000344000000000000034400000000000002440000000000000344000000000000024400000000000002440",
                polygon(crs, ring(c(10, 10), c(20, 15), c(20, 20), c(10, 20), c(10, 10))
                )
        );

        addCase(18,
                "MULTIPOINT ((10 10),(20 20))",
                "010400000002000000010100000000000000000024400000000000002440010100000000000000000034400000000000003440",
                multipoint(crs, point(c(10, 10)), point(c(20, 20)))
        );

        addCase(19,
                "MULTILINESTRING ((10 10,20 20),(15 15,30 15))",
                "01050000000200000001020000000200000000000000000024400000000000002440000000000000344000000000000034400102000000020000000000000000002E400000000000002E400000000000003E400000000000002E40",
                multilinestring(crs, linestring(c(10, 10), c(20, 20)), linestring(c(15, 15), c(30, 15)))
        );


        addCase(20,
                "MULTILINESTRING Z ((10 10 1,20 20 2),(15 15 3,30 15 4))",
                "01ED0300000200000001EA0300000200000000000000000024400000000000002440000000000000F03F00000000000034400000000000003440000000000000004001EA030000020000000000000000002E400000000000002E4000000000000008400000000000003E400000000000002E400000000000001040",
                multilinestring(crsZ, linestring(c(10, 10, 1), c(20, 20, 2)), linestring(c(15, 15, 3), c(30, 15, 4)))
        );

        addCase(21,
                "MULTIPOLYGON (((10 10,20 15,20 20,10 20,10 10)),((60 60,80 60,70 70,60 60)))",
                "010600000002000000010300000001000000050000000000000000002440000000000000244000000000000034400000000000002E40000000000000344000000000000034400000000000002440000000000000344000000000000024400000000000002440010300000001000000040000000000000000004E400000000000004E4000000000000054400000000000004E40000000000080514000000000008051400000000000004E400000000000004E40",
                multipolygon(crs, polygon(ring(c(10, 10), c(20, 15), c(20, 20), c(10, 20), c(10, 10))), polygon(ring(c(60, 60), c(80, 60), c(70, 70), c(60, 60))))
        );

        addCase(22,
                "GEOMETRYCOLLECTION (POINT (10 10),POINT (30 30),LINESTRING (15 15,20 20))",
                "01070000000300000001010000000000000000002440000000000000244001010000000000000000003E400000000000003E400102000000020000000000000000002E400000000000002E4000000000000034400000000000003440",
                geometrycollection(crs, point(c(10, 10)), point(c(30, 30)), linestring(c(15, 15), c(20, 20)))
        );

        addCase(23,
                "GEOMETRYCOLLECTION Z (POINT Z (10 10 1),POINT Z (30 30 2),LINESTRING Z (15 15 1,20 20 2))",
                "01EF0300000300000001E903000000000000000024400000000000002440000000000000F03F01E90300000000000000003E400000000000003E40000000000000004001EA030000020000000000000000002E400000000000002E40000000000000F03F000000000000344000000000000034400000000000000040",
                geometrycollection(crsZ, point(c(10, 10, 1)), point(c(30, 30, 2)), linestring(c(15, 15, 1), c(20, 20, 2)))
        );

        addCase(24,
                "GEOMETRYCOLLECTION ZM (POINT ZM (10 10 1 2),POINT ZM (30 30 2 3),LINESTRING ZM (15 15 1 3,20 20 2 4))",
                "01BF0B00000300000001B90B000000000000000024400000000000002440000000000000F03F000000000000004001B90B00000000000000003E400000000000003E400000000000000040000000000000084001BA0B0000020000000000000000002E400000000000002E40000000000000F03F00000000000008400000000000003440000000000000344000000000000000400000000000001040",
                geometrycollection(crsZM, point(c(10, 10, 1, 2)), point(c(30, 30, 2, 3)), linestring(c(15, 15, 1, 3), c(20, 20, 2, 4)))
        );

        addCase(25,
                "MULTIPOINT ZM EMPTY",
                "01BC0B000000000000",
                geometrycollection(crsZM)
        );
    }


}
