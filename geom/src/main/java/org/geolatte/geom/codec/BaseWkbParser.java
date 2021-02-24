package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

class BaseWkbParser {

    final private ByteBuffer buffer;

    BaseWkbParser(ByteBuffer buffer){
        this.buffer = buffer;
    }

    Geometry<? extends Position> parse() throws WkbDecodeException {
        return null;
    }

}
