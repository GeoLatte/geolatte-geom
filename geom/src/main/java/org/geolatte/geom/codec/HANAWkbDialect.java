package org.geolatte.geom.codec;

import org.geolatte.geom.*;

class HANAWkbDialect extends WkbDialect {

    final public static HANAWkbDialect INSTANCE = new HANAWkbDialect();

    private HANAWkbDialect() {
    }

    @Override
    boolean emptyPointAsNaN() {
        return false;
    }

    @Override
    GeometryType parseType(long tpe) {
        return super.parseType((tpe & 0xFFFF) % 100);
    }

    protected <P extends Position> int calculateSize(Geometry<P> geom, boolean includeSrid) {
        int size = 1 + ByteBuffer.UINT_SIZE; //size for order byte + type field
        if (includeSrid) {
            size += 4;
        }
        if (geom.isEmpty()) return size + ByteBuffer.UINT_SIZE;
        return size + geometrySize(geom);
    }

    @Override
    <P extends Position> BaseWkbVisitor<P> mkVisitor(Geometry<P> geom, ByteOrder bo) {
        return new HANAWkbVisitor<>(mkByteBuffer(geom, bo), this);
    }
}
