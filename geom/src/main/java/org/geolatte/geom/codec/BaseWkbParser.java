package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.support.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;

class BaseWkbParser<P extends Position> {

    final protected ByteBuffer buffer;
    final protected WkbDialect dialect;
    protected CoordinateReferenceSystem<P> crs;

    protected boolean hasZ = false;
    protected boolean hasM = false;
    protected GeometryType gtype;

    @SuppressWarnings("unchecked")
    BaseWkbParser(WkbDialect dialect, ByteBuffer buffer, CoordinateReferenceSystem<P> crs) {
        this.buffer = buffer;
        this.buffer.rewind();
        this.dialect = dialect;
        this.crs = crs == null ? (CoordinateReferenceSystem<P>) CoordinateReferenceSystems.PROJECTED_2D_METER : crs;
    }

    Geometry<P> parse() throws WkbDecodeException {
        GeometryBuilder builder = parseGeometry();
        isCrsCompatible(this.crs);
        try {
            return builder.createGeometry(crs);
        } catch (DecodeException de) {
            throw new WkbDecodeException(de);
        }
    }

    private GeometryBuilder parseGeometry() {
        parseByteOrder();
        GeometryBuilder builder = parseWkbType();
        switch (gtype) {
            case POINT:
                matchPoint(builder);
                break;
            case LINESTRING:
                matchLineString(builder);
                break;
            case POLYGON:
                matchPolygon(builder);
                break;
            case MULTIPOINT:
                matchMultiPoint(builder);
                break;
            case MULTILINESTRING:
                matchMultiLineString(builder);
                break;
            case MULTIPOLYGON:
                matchMultiPolygon(builder);
                break;
            case GEOMETRYCOLLECTION:
                matchGeometryCollection((CollectionGeometryBuilder) builder);
                break;
            default:
                throw new WkbDecodeException("Can't decode a WKB of type " + gtype);
        }
        return builder;
    }


    protected void parseByteOrder() {
        byte bo = buffer.get();
        ByteOrder byteOrder = ByteOrder.valueOf(bo);
        buffer.setByteOrder(byteOrder);
    }

    protected GeometryBuilder parseWkbType() {
        long tpe = buffer.getUInt();
        gtype = dialect.parseType(tpe);
        hasZ = dialect.hasZ(tpe);
        hasM = dialect.hasM(tpe);
        return GeometryBuilder.create(gtype);
    }

    protected void matchPoint(GeometryBuilder builder) {
        PointHolder ph = readPosition();
        builder.setPositions(ph);
    }

    protected PointHolder readPosition() {
        PointHolder ph = new PointHolder();
        ph.push(buffer.getDouble());
        ph.push(buffer.getDouble());
        if (hasZ) ph.push((buffer.getDouble()));
        if (hasM) ph.push(buffer.getDouble());
        return ph;
    }

    protected void matchLineString(GeometryBuilder builder) {
        LinearPositionsHolder lh = readLinestring();
        builder.setPositions(lh);
    }

    protected LinearPositionsHolder readLinestring() {
        LinearPositionsHolder lh = new LinearPositionsHolder();
        long numPositions = buffer.getUInt();
        for (long i = 0; i < numPositions; i++) {
            lh.push(readPosition());
        }
        return lh;
    }

    protected void matchPolygon(GeometryBuilder builder) {
        LinearPositionsListHolder llh = readPolygon();
        builder.setPositions(llh);
    }

    private LinearPositionsListHolder readPolygon() {
        LinearPositionsListHolder llh = new LinearPositionsListHolder();
        long numRings = buffer.getUInt();
        for (long i = 0; i < numRings; i++) {
            llh.push(readLinestring());
        }
        return llh;
    }

    protected void matchMultiPoint(GeometryBuilder builder) {
        LinearPositionsHolder lh = new LinearPositionsHolder();
        long numPnts = buffer.getUInt();
        for (long i = 0; i < numPnts; i++) {
            parseByteOrder();
            buffer.getUInt(); //not necessary
            lh.push(readPosition());
        }
        builder.setPositions(lh);
    }

    protected void matchMultiLineString(GeometryBuilder builder) {
        LinearPositionsListHolder llh = new LinearPositionsListHolder();
        long numl = buffer.getUInt();
        for (long i = 0; i < numl; i++) {
            parseByteOrder();
            buffer.getUInt(); //skip type
            llh.push(readLinestring());
        }
        builder.setPositions(llh);
    }

    protected void matchMultiPolygon(GeometryBuilder builder) {
        PolygonListHolder plh = new PolygonListHolder();
        long nump = buffer.getUInt();
        for (long i = 0; i < nump; i++) {
            parseByteOrder();
            buffer.getUInt();
            plh.push(readPolygon());
        }
        builder.setPositions(plh);
    }

    //Is the CRS coordinate dimension compatible with the WKB
    protected void isCrsCompatible(CoordinateReferenceSystem<?> crs) {
        if ((hasM && !(crs.hasM())) ||
                (hasZ && !crs.hasZ())) {
            throw new WkbDecodeException("WKB inconsistent with specified Coordinate Reference System");
        }
    }

    private void matchGeometryCollection(CollectionGeometryBuilder builder) {
        long numg = buffer.getUInt();
        for (long i = 0; i < numg; i++) {
            builder.push(parseGeometry());
        }
    }
}
