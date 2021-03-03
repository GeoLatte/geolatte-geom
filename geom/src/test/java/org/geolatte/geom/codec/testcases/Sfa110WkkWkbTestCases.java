package org.geolatte.geom.codec.testcases;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;

import static org.geolatte.geom.builder.DSL.*;

public class Sfa110WkkWkbTestCases extends WktWkbCodecTestBase {

    public static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    public Sfa110WkkWkbTestCases() {

        addCase(1,
                "POINT(1.52 2.43)",
                "010100000052b81e85eb51f83f713d0ad7a3700340",
                point(crs, c(1.52, 2.43))
        );

        addCase(14,
                "POINT EMPTY",
                "0101000000000000000000f87f000000000000f87f",
                point(crs)
        );

        addCase(2,
                "LINESTRING(1.52 2.43,4.23 5.32)",
                "01020000000200000052b81e85eb51f83f713d0ad7a3700340ec51b81e85eb104048e17a14ae471540",
                linestring(crs, c(1.52, 2.43), c(4.23, 5.32))
        );

        addCase(3,
                "LINESTRING EMPTY",
                "010200000000000000",
                linestring(crs)
        );

        addCase(4,
                "POLYGON((1.52 2.43,4.23 5.32,4.23 6.32,1.52 3.43,1.52 2.43))",
                "0103000000010000000500000052b81e85eb51f83f713d0ad7a3700340ec51b81e85eb104048e17a14ae471540ec51b81e85eb104048e17a14ae47194052b81e85eb51f83f713d0ad7a3700b4052b81e85eb51f83f713d0ad7a3700340",
                polygon(crs, ring(c(1.52, 2.43), c(4.23, 5.32), c(4.23, 6.32), c(1.52, 3.43), c(1.52, 2.43)))
        );

        addCase(5,
                "POLYGON EMPTY",
                "010300000000000000",
                polygon(crs)
        );

        addCase(6,
                "MULTIPOINT(1.52 2.43,3.42 5.34)",
                "010400000002000000010100000052b81e85eb51f83f713d0ad7a370034001010000005c8fc2f5285c0b405c8fc2f5285c1540",
                multipoint(crs, c(1.52, 2.43), c(3.42, 5.34))
        );

        addCase(7,
                "MULTIPOINT EMPTY",
                "010400000000000000",
                multipoint(crs)
        );

        addCase(8,
                "MULTILINESTRING((1.52 2.43,3.42 5.34),(4.5 3.2,6.7 9.8))",
                "01050000000200000001020000000200000052b81e85eb51f83f713d0ad7a37003405c8fc2f5285c0b405c8fc2f5285c154001020000000200000000000000000012409a99999999990940cdcccccccccc1a409a99999999992340",
                multilinestring(crs,
                        linestring(c(1.52, 2.43), c(3.42, 5.34)),
                        linestring(c(4.5, 3.2), c(6.7, 9.8))
                )
        );

        addCase(9,
                "MULTILINESTRING EMPTY",
                "010500000000000000",
                multilinestring(crs));

        addCase(10,
                "MULTIPOLYGON(((1.52 2.43,4.23 5.32,4.23 6.32,1.52 3.43,1.52 2.43)),((3.52 4.43,6.23 7.32,6.23 8.32,3.52 5.43,3.52 4.43)))",
                "0106000000020000000103000000010000000500000052b81e85eb51f83f713d0ad7a3700340ec51b81e85eb104048e17a14ae471540ec51b81e85eb104048e17a14ae47194052b81e85eb51f83f713d0ad7a3700b4052b81e85eb51f83f713d0ad7a370034001030000000100000005000000295c8fc2f5280c40b81e85eb51b81140ec51b81e85eb184048e17a14ae471d40ec51b81e85eb1840a4703d0ad7a32040295c8fc2f5280c40b81e85eb51b81540295c8fc2f5280c40b81e85eb51b81140",
                multipolygon(crs,
                        polygon(ring(c(1.52, 2.43), c(4.23, 5.32), c(4.23, 6.32), c(1.52, 3.43), c(1.52, 2.43))),
                        polygon(ring(c(3.52, 4.43), c(6.23, 7.32), c(6.23, 8.32), c(3.52, 5.43), c(3.52, 4.43))))
                );

        addCase(11,
                "MULTIPOLYGON EMPTY",
                "010600000000000000",
                multipolygon(crs)
        );

        addCase(12,
                "GEOMETRYCOLLECTION(POINT(1.52 2.43),LINESTRING(1.52 2.43,4.23 5.32))",
                "010700000002000000010100000052b81e85eb51f83f713d0ad7a370034001020000000200000052b81e85eb51f83f713d0ad7a3700340ec51b81e85eb104048e17a14ae471540",
                geometrycollection(crs, point(c(1.52, 2.43)), linestring(c(1.52, 2.43), c(4.23, 5.32)))
        );

        addCase(13,
                "GEOMETRYCOLLECTION EMPTY",
                "010700000000000000",
                geometrycollection(crs)
        );
    }

}
