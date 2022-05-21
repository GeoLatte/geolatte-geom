package org.geolatte.geom.codec;


import org.geolatte.geom.*;
import org.geolatte.geom.builder.DSL;
import org.junit.Test;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestMySqlWkbCodec {


    final private WkbDecoder decoder = new MySqlWkbDecoder();
    final private WkbEncoder encoder = new MySqlWkbEncoder();

    /**
     * This encodes an empty geometry into a geomtrycollection().
     *
     * The documentation states: "The only valid empty geometry is represented in the form
     * of an empty geometry collection value. MySQL does not support
     * GIS EMPTY values such as POINT EMPTY.
     */
    @Test
    public void testEmptyGeometryEncoding(){
        Geometry<G2D> geometry = DSL.linestring(WGS84);
        ByteBuffer wkb = encoder.encode(geometry);
        Geometry<G2D> decoded = decoder.decode(wkb, WGS84);
        assertTrue(decoded.isEmpty());
        assertSame(decoded.getGeometryType(), GeometryType.GEOMETRYCOLLECTION);


    }
}
