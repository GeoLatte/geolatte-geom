package org.geolatte.geom.codec;

import org.geolatte.geom.*;

import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.GeometryType.*;

class WkbDialect {

    final private Map<Long, GeometryType> typemap = new HashMap<>();

    final public static Long WKB_POINT = 1L;
    final public static Long WKB_LINESTRING = 2L;
    final public static Long WKB_POLYGON = 3L;
    final public static Long WKB_MULTIPOINT = 4L;
    final public static Long WKB_MULTILINESTRING = 5L;
    final public static Long WKB_MULTIPOLYGON = 6L;
    final public static Long WKB_GEOMETRYCOLLECTION = 7L;

    protected WkbDialect() {
        typemap.put(WKB_POINT, POINT);
        typemap.put(WKB_LINESTRING, LINESTRING);
        typemap.put(WKB_POLYGON, POLYGON);
        typemap.put(WKB_MULTIPOINT, MULTIPOINT);
        typemap.put(WKB_MULTILINESTRING, MULTILINESTRING);
        typemap.put(WKB_MULTIPOLYGON, MULTIPOLYGON);
        typemap.put(WKB_GEOMETRYCOLLECTION, GEOMETRYCOLLECTION);
    }

    GeometryType parseType(long tpe) {
        GeometryType gt = typemap.get(tpe);
        if (gt == null) throw new WkbDecodeException("Unsupported WKB type code: " + tpe);
        return gt;
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
        //empty geoms have same representation as an empty GeometryCollection
        if (geom.isEmpty()) return size + sizeEmptyGeometry(geom);
        if (geom instanceof AbstractGeometryCollection) {
            size += sizeOfGeometryCollection((AbstractGeometryCollection<P, ?>) geom);
        } else if (geom instanceof Polygon) {
            size += getPolygonSize((Polygon<P>) geom);
        } else if (geom instanceof Point) {
            size += getPositionSize(geom);
        } else {
            size += ByteBuffer.UINT_SIZE; //to hold number of points
            size += getPositionSize(geom) * geom.getNumPositions();
        }
        return size;
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
//        //empty geometries have the same representation as an empty geometry collection
//        if (geometry.isEmpty() && geometry.getGeometryType() == GeometryType.POINT) {
//            return WkbGeometryType.GEOMETRY_COLLECTION.getTypeCode();
//        }
        for (Map.Entry<Long, GeometryType> tpe : typemap.entrySet()) {
            if (tpe.getValue() == geometry.getGeometryType()) {
                return tpe.getKey();
            }
        }
        throw new UnsupportedConversionException(
                String.format(
                        "Can't convert geometries of type %s",
                        geometry.getClass().getCanonicalName()
                )
        );

    }
}
