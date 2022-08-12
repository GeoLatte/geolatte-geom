package org.geolatte.geom.codec;

import org.geolatte.geom.*;

import static org.geolatte.geom.GeometryType.*;

class WkbDialect {


    final public static int WKB_POINT = 1;
    final public static int WKB_LINESTRING = 2;
    final public static int WKB_POLYGON = 3;
    final public static int WKB_MULTIPOINT = 4;
    final public static int WKB_MULTILINESTRING = 5;
    final public static int WKB_MULTIPOLYGON = 6;
    final public static int WKB_GEOMETRYCOLLECTION = 7;

    GeometryType parseType(long tpe) {
        switch ((int) tpe) {
            case WKB_POINT:
                return POINT;
            case WKB_LINESTRING:
                return LINESTRING;
            case WKB_POLYGON:
                return POLYGON;
            case WKB_MULTIPOINT:
                return MULTIPOINT;
            case WKB_MULTIPOLYGON:
                return MULTIPOLYGON;
            case WKB_MULTILINESTRING:
                return MULTILINESTRING;
            case WKB_GEOMETRYCOLLECTION:
                return GEOMETRYCOLLECTION;
            default:
                throw new WkbDecodeException("Unsupported WKB type code: " + tpe);
        }
    }

    boolean hasZ(long tpe) {
        return false;
    }

    boolean hasM(long tpe) {
        return false;
    }

    <P extends Position> BaseWkbVisitor<P> mkVisitor(Geometry<P> geom, ByteOrder bo) {
        ByteBuffer buffer = mkByteBuffer(geom, bo);
        return new BaseWkbVisitor<>(buffer, this);
    }

    protected <P extends Position> ByteBuffer mkByteBuffer(Geometry<P> geom, ByteOrder bo) {
        ByteBuffer buffer = ByteBuffer.allocate(calculateSize(geom, true));
        if (bo != null) {
            buffer.setByteOrder(bo);
        }
        return buffer;
    }

    protected <P extends Position> int calculateSize(Geometry<P> geom, boolean topLevel) {

        int size = 1 + ByteBuffer.UINT_SIZE; //size for order byte + type field
        if (topLevel) {
            size += extraHeaderSize(geom);
        }
        //empty geometries have same representation as an empty GeometryCollection
        if (geom.isEmpty()) return size + sizeEmptyGeometry(geom);

        return size + geometrySize(geom);
    }

    protected <P extends Position> int extraHeaderSize(Geometry<P> geom) {
        return 0;
    }

    protected <P extends Position> int sizeEmptyGeometry(Geometry<P> geometry) {
        if (geometry.getGeometryType() == GeometryType.POINT) {
            //Empty Point encoded with NaN coordinates? then same size as one position,
            // else number of elements (0 elements)
            return emptyPointAsNaN() ? getPositionSize(geometry) : ByteBuffer.UINT_SIZE;
        } else {
            // indicate no positions follow
            return ByteBuffer.UINT_SIZE;
        }
    }

    protected <P extends Position> int geometrySize(Geometry<P> geom) {
        if (geom instanceof AbstractGeometryCollection) {
            return sizeOfGeometryCollection((AbstractGeometryCollection<P, ?>) geom);
        } else if (geom instanceof Polygon) {
            return getPolygonSize((Polygon<P>) geom);
        } else if (geom instanceof Point) {
            return getPositionSize(geom);
        } else {
            int numPoints = ByteBuffer.UINT_SIZE; //to hold number of points
            return  numPoints + getPositionSize(geom) * geom.getNumPositions();
        }
    }

    protected <P extends Position> int getPositionSize(Geometry<P> geom) {
        return geom.getCoordinateDimension() * ByteBuffer.DOUBLE_SIZE;
    }

    protected <P extends Position> int getPolygonSize(Polygon<P> geom) {
        //to hold the number of linear rings
        int size = ByteBuffer.UINT_SIZE;
        //for each linear ring, a UINT holds the number of points
        size += geom.isEmpty() ? 0 : ByteBuffer.UINT_SIZE * (geom.getNumInteriorRing() + 1);
        size += getPositionSize(geom) * geom.getNumPositions();
        return size;
    }

    protected <P extends Position, G extends Geometry<P>> int sizeOfGeometryCollection(AbstractGeometryCollection<P, G> collection) {
        int size = ByteBuffer.UINT_SIZE;
        for (G g : collection) {
            size += calculateSize(g, false);
        }
        return size;
    }

    boolean emptyPointAsNaN() {
        return true;
    }

    protected <P extends Position> Long geometryTypeCode(Geometry<P> geometry) {
        return (long) wkbTypeCode(geometry);
    }

    private <P extends Position> int wkbTypeCode(Geometry<P> geometry) {
        switch (geometry.getGeometryType()) {
            case POINT:
                return WKB_POINT;
            case LINESTRING:
            case LINEARRING:
                return WKB_LINESTRING;
            case POLYGON:
                return WKB_POLYGON;
            case MULTIPOINT:
                return WKB_MULTIPOINT;
            case MULTILINESTRING:
                return WKB_MULTILINESTRING;
            case MULTIPOLYGON:
                return WKB_MULTIPOLYGON;
            case GEOMETRYCOLLECTION:
                return WKB_GEOMETRYCOLLECTION;
            default:
                throw new UnsupportedConversionException(
                        String.format(
                                "Can't convert geometries of type %s",
                                geometry.getClass().getCanonicalName()
                        )
                );

        }
    }
}
