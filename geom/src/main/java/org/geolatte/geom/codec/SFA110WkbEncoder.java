package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

public class SFA110WkbEncoder implements WkbEncoder {

    @Override
    public <P extends Position> ByteBuffer encode(Geometry<P> geometry, ByteOrder byteOrder) {
        BaseWkbVisitor<P> visitor = SFA110WkbDialect.INSTANCE.mkVisitor(geometry, byteOrder);
        geometry.accept(visitor);
        return visitor.result();
    }

}
