package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

public class PostgisWkbV1Dialect extends WkbDialect {

    final public static WkbDialect INSTANCE = new PostgisWkbV1Dialect();

    @Override
    boolean emptyPointAsNaN() {
        return false;
    }

    @Override
    protected <P extends Position> Long geometryTypeCode(Geometry<P> geometry) {
        if (geometry.isEmpty() && geometry.getGeometryType() == GeometryType.POINT) {
            return WKB_GEOMETRYCOLLECTION;
        }
        return super.geometryTypeCode(geometry);
    }

    GeometryType parseType(long tpe) {
        return super.parseType( tpe & 0xFFFF );
    }

    @Override
    protected <P extends Position> int extraHeaderSize(Geometry<P> geom) {
        return geom.getSRID() > 0 ? 4 : 0; //4 bytes for SRID
    }

    @Override
    <P extends Position> BaseWkbVisitor<P> mkVisitor(Geometry<P> geom, ByteOrder bo) {
        return new PostgisWkbVisitor<P>(mkByteBuffer(geom, bo), this);
    }

    static class PostgisWkbVisitor<P extends Position> extends BaseWkbVisitor<P> {

        private boolean hasWrittenSrid = false;

        PostgisWkbVisitor(ByteBuffer byteBuffer, WkbDialect dialect) {
            super(byteBuffer, dialect);
        }

        protected void writeTypeCodeAndSrid(Geometry<P> geometry, ByteBuffer output) {
            long typeCode = dialect().geometryTypeCode(geometry);
            boolean hasSrid = (geometry.getSRID() > 0);
            CoordinateReferenceSystem<P> crs = geometry.getCoordinateReferenceSystem();
            if (hasSrid && !hasWrittenSrid) {
                typeCode |= PostgisWkbTypeMasks.SRID_FLAG;
            }
            if (geometry.hasM()) {
                typeCode |= PostgisWkbTypeMasks.M_FLAG;
            }
            if (geometry.hasZ()) {
                typeCode |= PostgisWkbTypeMasks.Z_FLAG;
            }
            output.putUInt(typeCode);
            if (hasSrid && !hasWrittenSrid) {
                output.putInt(geometry.getSRID());
                hasWrittenSrid = true;
            }
        }

    }
}
