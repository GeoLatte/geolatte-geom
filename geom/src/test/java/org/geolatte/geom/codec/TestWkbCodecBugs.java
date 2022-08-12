package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.builder.DSL;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_3D_METER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Karel Maesen, Geovise BVBA on 14/02/2020.
 */
public class TestWkbCodecBugs {



    @Test
    public void testForIssue89() {
        String WkbText = "0101000080000000000000244000000000000034400000000000003E40";
        CoordinateReferenceSystem<C3D> crs = (CoordinateReferenceSystem<C3D>)CrsRegistry.getCoordinateReferenceSystemForEPSG(555, PROJECTED_3D_METER);
        Point<C3D> pnt = DSL.point(crs, c(10, 20, 30));
        WkbDecoder decoder = Wkb.newDecoder(Wkb.Dialect.POSTGIS_EWKB_1);
        assertEquals(pnt, decoder.decode(ByteBuffer.from(WkbText)));
    }

    @Test
    public void testForIssue152(){
        WkbEncoder encoder = Wkb.newEncoder(Wkb.Dialect.MYSQL_WKB);

        PositionSequence<G2D> seq = PositionSequenceBuilders.fixedSized(5, G2D.class)
                .add(new G2D(0.0,10.0))
                .add(new G2D(10.0, 10.0))
                .add(new G2D(10.0, 0.0))
                .add(new G2D(0.0, 0.0))
                .add(new G2D(0.0, 10.0))
                .toPositionSequence(); // basically a closed LINESTRING

        Geometry<G2D> geometry = Geometries.mkLinearRing(seq, CoordinateReferenceSystems.WGS84);
        ByteBuffer buffer = encoder.encode(geometry, ByteOrder.NDR);
        assertNotNull(buffer);
    }
}
