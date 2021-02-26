package org.geolatte.geom.codec;

import org.geolatte.geom.*;

public class PostgisWkb2Encoder extends PostgisWkbEncoder{

    public PostgisWkb2Encoder(){
        super(PostgisWkbV2Dialect.INSTANCE);
    }
}

//class PostgisWkb2Visitor<P extends Position> extends PostgisWkbEncoder.PostgisWkbVisitor<P> {
//
//
//    PostgisWkb2Visitor(ByteBuffer byteBuffer) {
//        super(byteBuffer);
//    }
//
//    @Override
//    public void visit(Point<P> geom) {
//        writeByteOrder(buffer());
//        writeTypeCodeAndSrid(geom, buffer());
//        if (geom.isEmpty()) {
//            buffer().putDouble(Double.NaN);
//            buffer().putDouble(Double.NaN);
//        } else {
//            writePoints(geom.getPositions(), geom.getCoordinateDimension(), buffer());
//        }
//    }
//
//    @Override
//    protected int geometryTypeCode(Geometry<P> geometry) {
//        WkbGeometryType type = WkbGeometryType.forClass(geometry.getClass());
//        if (type == null) {
//            throw new UnsupportedConversionException(
//                    String.format(
//                            "Can't convert geometries of type %s",
//                            geometry.getClass().getCanonicalName()
//                    )
//            );
//        }
//        return type.getTypeCode();
//    }
//
//
//};