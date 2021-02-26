package org.geolatte.geom.codec;

import org.geolatte.geom.*;

import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.GeometryType.*;

class WkbDialect {

    private final Map<Long, GeometryType> typemap = new HashMap<>();

    protected WkbDialect() {
        typemap.put(1L, POINT);
        typemap.put(2L, LINESTRING);
        typemap.put(3L, POLYGON);
        typemap.put(4L, MULTIPOINT);
        typemap.put(5L, MULTILINESTRING);
        typemap.put(6L, MULTIPOLYGON);
        typemap.put(7L, GEOMETRYCOLLECTION);
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

    <P extends Position> BaseWkbVisitor<P> mkVisitor(Geometry<P> geom, ByteOrder bo){
        ByteBuffer buffer = ByteBuffer.allocate(calculateSize(geom, true));
        if(bo != null) {
            buffer.setByteOrder(bo);
        }
        return new BaseWkbVisitor<>(buffer);
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
            //Empty Point encoded with NaN coordinates
            return getPositionSize(geometry);
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
}
