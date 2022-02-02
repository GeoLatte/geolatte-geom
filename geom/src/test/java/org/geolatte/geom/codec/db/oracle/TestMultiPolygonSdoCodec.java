package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.G2D;
import org.geolatte.geom.MultiPolygon;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.WGS84;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;

public class TestMultiPolygonSdoCodec {

    @Test
    public void testBasic() {
        MultiPolygon<G2D> mpoly = multipolygon(WGS84,
                polygon(
                        ring(g(-10, -10), g(10, -10), g(10, 10), g(-10, 10), g(-10, -10)),
                        ring(g(0, 0), g(0, 1), g(1, 1), g(1, 0), g(0, 0))
                ),
                polygon(
                        ring(g(-10, -10), g(10, -10), g(10, 10), g(-10, 10), g(-10, -10))
                )
        );
        SDOGeometry sdo = Encoders.encode(mpoly);
        assertEquals(mpoly, Decoders.decode(sdo));
    }
}
