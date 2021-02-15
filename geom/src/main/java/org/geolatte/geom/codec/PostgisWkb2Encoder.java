package org.geolatte.geom.codec;

import org.geolatte.geom.*;

public class PostgisWkb2Encoder extends PostgisWkbEncoder{

    protected <P extends Position> WkbVisitor<P> newWkbVisitor(ByteBuffer output, Geometry<P> geom) {
        return new PostgisWkb2Visitor<P>(output);
    }

    protected <P extends Position> int sizeEmptyGeometry(Geometry<P> geometry) {
        if(geometry.getGeometryType() == GeometryType.POINT) {
            // two NaN
            return 2 * ByteBuffer.DOUBLE_SIZE;
        } else {
            // indicate no positions
            return ByteBuffer.UINT_SIZE;
        }
    }
}

class PostgisWkb2Visitor<P extends Position> extends PostgisWkbEncoder.PostgisWkbVisitor<P> {


    PostgisWkb2Visitor(ByteBuffer byteBuffer) {
        super(byteBuffer);
    }

    @Override
    public void visit(Point<P> geom) {
        writeByteOrder(output);
        writeTypeCodeAndSrid(geom, output);
        if (geom.isEmpty()) {
            output.putDouble(Double.NaN);
            output.putDouble(Double.NaN);
        } else {
            writePoints(geom.getPositions(), geom.getCoordinateDimension(), output);
        }
    }

    @Override
    protected int geometryTypeCode(Geometry<P> geometry) {
        WkbGeometryType type = WkbGeometryType.forClass(geometry.getClass());
        if (type == null) {
            throw new UnsupportedConversionException(
                    String.format(
                            "Can't convert geometries of type %s",
                            geometry.getClass().getCanonicalName()
                    )
            );
        }
        return type.getTypeCode();
    }


};