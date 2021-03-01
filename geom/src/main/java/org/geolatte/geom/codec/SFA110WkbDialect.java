package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

public class SFA110WkbDialect extends WkbDialect {

    public final static SFA110WkbDialect INSTANCE = new SFA110WkbDialect();

    protected SFA110WkbDialect() {

    }

    protected <P extends Position> int getPositionSize(Geometry<P> geom) {
        return 2 * ByteBuffer.DOUBLE_SIZE;
    }
}
