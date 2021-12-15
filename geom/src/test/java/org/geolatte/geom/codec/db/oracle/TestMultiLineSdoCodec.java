package org.geolatte.geom.codec.db.oracle;

import junit.framework.TestCase;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.MultiLineString;

import static org.geolatte.geom.CrsMock.WGS84;
import static org.geolatte.geom.builder.DSL.*;
import org.junit.Test;

public class TestMultiLineSdoCodec extends TestCase {

    @Test
    public void testBasic(){
        MultiLineString<G2D> mls = multilinestring(WGS84,
                linestring(g(1,2), g(3,4)),
                linestring(g(4,6),g(2, 1)));
        SDOGeometry encoded = Encoders.encode(mls);
        Geometry<?> decoded = Decoders.decode(encoded);
        assertEquals(mls, decoded);
    }

    @Test
    public void testEmpty(){
        MultiLineString<G2D> mls = multilinestring(WGS84);
        SDOGeometry encoded = Encoders.encode(mls);
        Geometry<?> decoded = Decoders.decode(encoded);
        assertEquals(mls, decoded);
    }


}