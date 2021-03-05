package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

class Sfa110WkbDialect extends WkbDialect {

    public final static Sfa110WkbDialect INSTANCE = new Sfa110WkbDialect();

    protected Sfa110WkbDialect() {

    }

    protected <P extends Position> int getPositionSize(Geometry<P> geom) {
        return 2 * ByteBuffer.DOUBLE_SIZE;
    }

    @Override
    <P extends Position> BaseWkbVisitor<P> mkVisitor(Geometry<P> geom, ByteOrder bo) {
        ByteBuffer buffer = mkByteBuffer(geom, bo);
        return new SFA110WkbVisitor<>(buffer, this);
    }
}

class SFA110WkbVisitor<P extends Position> extends BaseWkbVisitor<P> {

    SFA110WkbVisitor(ByteBuffer byteBuffer, WkbDialect dialect) {
        super(byteBuffer, dialect);
    }

    @Override
    protected void writePoint(double[] coordinates, ByteBuffer output) {
        output.putDouble(coordinates[0]);
        output.putDouble(coordinates[1]);
    }
}