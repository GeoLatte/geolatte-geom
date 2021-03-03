package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

public class Sfa110WkbDialect extends WkbDialect {

    public final static Sfa110WkbDialect INSTANCE = new Sfa110WkbDialect();

    protected Sfa110WkbDialect() {

    }

    protected <P extends Position> int getPositionSize(Geometry<P> geom) {
        return 2 * ByteBuffer.DOUBLE_SIZE;
    }
}
