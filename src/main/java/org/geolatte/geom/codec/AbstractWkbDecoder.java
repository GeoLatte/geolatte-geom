/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CrsId;

/**
 * Base class for <code>WkbDecoder</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/1/12
 */
abstract class AbstractWkbDecoder implements WkbDecoder {

    private CrsId crsId;

    /**
     * Decodes a Postgis WKB representation in a <code>ByteBuffer</code> to a <code>Geometry</code>.
     *
     * @param byteBuffer A buffer of bytes that contains a WKB-encoded <code>Geometry</code>.
     * @return The <code>Geometry</code> that is encoded in the WKB.
     */
    @Override
    public Geometry decode(ByteBuffer byteBuffer) {
        crsId = CrsId.UNDEFINED;
        byteBuffer.rewind();
        try {
            prepare(byteBuffer);
            Geometry geom = decodeGeometry(byteBuffer);
            byteBuffer.rewind();
            return geom;
        } catch(BufferAccessException e){
            throw new WkbDecodeException(e);
        }
    }

    private Geometry decodeGeometry(ByteBuffer byteBuffer) {
        alignByteOrder(byteBuffer);
        int typeCode = readTypeCode(byteBuffer);
        WkbGeometryType wkbType = WkbGeometryType.parse((byte) typeCode);
        readSridIfPresent(byteBuffer, typeCode);
        DimensionalFlag flag = determineDimensionalFlag(typeCode);
        switch (wkbType) {
            case POINT:
                return decodePoint(byteBuffer, flag);
            case LINE_STRING:
                return decodeLineString(byteBuffer, flag);
            case POLYGON:
                return decodePolygon(byteBuffer, flag);
            case GEOMETRY_COLLECTION:
                return decodeGeometryCollection(byteBuffer);
            case MULTI_POINT:
                return decodeMultiPoint(byteBuffer);
            case MULTI_POLYGON:
                return decodeMultiPolygon(byteBuffer);
            case MULTI_LINE_STRING:
                return decodeMultiLineString(byteBuffer);
        }
        throw new WkbDecodeException(String.format("WKBType %s is not supported.", wkbType));
    }

    private MultiLineString decodeMultiLineString(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        LineString[] geometries = new LineString[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = (LineString) decodeGeometry(byteBuffer);
        }
        return new MultiLineString(geometries);
    }

    private MultiPoint decodeMultiPoint(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        Point[] geometries = new Point[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = (Point) decodeGeometry(byteBuffer);
        }
        return new MultiPoint(geometries);
    }

    private MultiPolygon decodeMultiPolygon(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        Polygon[] geometries = new Polygon[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = (Polygon) decodeGeometry(byteBuffer);
        }
        return new MultiPolygon(geometries);
    }

    private GeometryCollection decodeGeometryCollection(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        Geometry[] geometries = new Geometry[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = decodeGeometry(byteBuffer);
        }
        return new GeometryCollection(geometries);
    }

    private Polygon decodePolygon(ByteBuffer byteBuffer, DimensionalFlag flag) {
        int numRings = byteBuffer.getInt();
        LinearRing[] rings = readPolygonRings(numRings, byteBuffer, flag, crsId);
        return new Polygon(rings);
    }

    private LineString decodeLineString(ByteBuffer byteBuffer, DimensionalFlag flag) {
        int numPoints = byteBuffer.getInt();
        PointSequence points = readPoints(numPoints, byteBuffer, flag);
        return new LineString(points, crsId);
    }

    private Point decodePoint(ByteBuffer byteBuffer, DimensionalFlag flag) {
        PointSequence points = readPoints(1, byteBuffer, flag);
        return new Point(points, crsId);
    }

    private PointSequence readPoints(int numPoints, ByteBuffer byteBuffer, DimensionalFlag dimensionalFlag) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(numPoints, dimensionalFlag);
        double[] coordinates = new double[dimensionalFlag.getCoordinateDimension()];
        for (int i = 0; i < numPoints; i++) {
            readPoint(byteBuffer, dimensionalFlag, coordinates);
            psBuilder.add(coordinates);
        }
        return psBuilder.toPointSequence();
    }

    private void readPoint(ByteBuffer byteBuffer, DimensionalFlag dimensionalFlag, double[] coordinates) {
        for (int ci = 0; ci < dimensionalFlag.getCoordinateDimension(); ci++) {
            coordinates[ci] = byteBuffer.getDouble();
        }
    }

    private LinearRing[] readPolygonRings(int numRings, ByteBuffer byteBuffer, DimensionalFlag dimensionalFlag, CrsId crsId) {
        LinearRing[] rings = new LinearRing[numRings];
        for (int i = 0; i < numRings; i++) {
            rings[i] = readRing(byteBuffer, dimensionalFlag, crsId);
        }
        return rings;
    }

    private LinearRing readRing(ByteBuffer byteBuffer, DimensionalFlag dimensionalFlag, CrsId crsId) {
        int numPoints = byteBuffer.getInt();
        PointSequence ps = readPoints(numPoints, byteBuffer, dimensionalFlag);
        try {
            return new LinearRing(ps, crsId);
        } catch (IllegalArgumentException e) {
            throw new WkbDecodeException(e);
        }
    }

    /**
     * Perform any preparatory steps on the bytebuffer before
     * starting the decoding.
     *
     * @param byteBuffer
     */
    protected abstract void prepare(ByteBuffer byteBuffer);

    /**
     * Determine the DimensionalFlag from the typecode
     *
     * @param typeCode
     * @return
     */
    protected abstract DimensionalFlag determineDimensionalFlag(int typeCode);

    /**
     * Read and set the SRID (if it is present)
     *
     * @param byteBuffer
     * @param typeCode
     */
    protected abstract void readSridIfPresent(ByteBuffer byteBuffer, int typeCode);

    protected abstract boolean hasSrid(int typeCode);

    protected int readTypeCode(ByteBuffer byteBuffer) {
        return (int) byteBuffer.getUInt();
    }
    protected CrsId getCrsId() {
        return crsId;
    }

    protected void setCrsId(CrsId crsId) {
        this.crsId = crsId;
    }

    private void alignByteOrder(ByteBuffer byteBuffer) {
        byte orderByte = byteBuffer.get();
        ByteOrder byteOrder = ByteOrder.valueOf(orderByte);
        byteBuffer.setByteOrder(byteOrder);
    }
}
