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
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;

import static org.geolatte.geom.Geometries.*;

/**
 * Base class for <code>WkbDecoder</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/1/12
 */
abstract class AbstractWkbDecoder implements WkbDecoder {

    @Override
    public <P extends Position> Geometry<P> decode(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        byteBuffer.rewind();
        try {
            prepare(byteBuffer);
            Geometry<P> geom = decodeGeometry(byteBuffer, crs);
            byteBuffer.rewind();
            return geom;
        } catch (BufferAccessException e) {
            throw new WkbDecodeException(e);
        }
    }

    @Override
    public Geometry<?> decode(ByteBuffer byteBuffer) {
        return decode(byteBuffer, (CoordinateReferenceSystem<?>) null);
    }

    private <P extends Position> Geometry<P> decodeGeometry(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        alignByteOrder(byteBuffer);
        int typeCode = readTypeCode(byteBuffer);
        WkbGeometryType wkbType = WkbGeometryType.parse((byte) typeCode);
        crs = (CoordinateReferenceSystem<P>)readCrs(byteBuffer, typeCode, crs);
        switch (wkbType) {
            case POINT:
                return decodePoint(byteBuffer, crs);
            case LINE_STRING:
                return decodeLineString(byteBuffer, crs);
            case POLYGON:
                return decodePolygon(byteBuffer, crs);
            case GEOMETRY_COLLECTION:
                return decodeGeometryCollection(byteBuffer, crs);
            case MULTI_POINT:
                return decodeMultiPoint(byteBuffer, crs);
            case MULTI_POLYGON:
                return decodeMultiPolygon(byteBuffer, crs);
            case MULTI_LINE_STRING:
                return decodeMultiLineString(byteBuffer,crs );
        }
        throw new WkbDecodeException(String.format("WKBType %s is not supported.", wkbType));
    }

    private <P extends Position> MultiLineString<P> decodeMultiLineString(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        int numGeometries = byteBuffer.getInt();
        if (numGeometries == 0) {
            return new MultiLineString<P>(crs);
        }
        List<LineString<P>> geometries = new ArrayList<LineString<P>>(numGeometries);
        for (int i = 0; i < numGeometries; i++) {
            geometries.add((LineString<P>)decodeGeometry(byteBuffer, crs));
        }
        return mkMultiLineString(geometries);
    }

    private <P extends Position> MultiPoint<P> decodeMultiPoint(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        int numGeometries = byteBuffer.getInt();
        if (numGeometries == 0) {
            return new MultiPoint<P>(crs);
        }
        List<Point<P>> geometries = new ArrayList<Point<P>>(numGeometries);
        for (int i = 0; i < numGeometries; i++) {
            geometries.add((Point<P>) decodeGeometry(byteBuffer, crs));
        }
        return mkMultiPoint(geometries);
    }

    private <P extends Position> MultiPolygon<P> decodeMultiPolygon(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        int numGeometries = byteBuffer.getInt();
        if (numGeometries == 0) {
            return new MultiPolygon<P>(crs);
        }
        List<Polygon<P>> geometries = new ArrayList<Polygon<P>>(numGeometries);
        for (int i = 0; i < numGeometries; i++) {
            geometries.add((Polygon<P>) decodeGeometry(byteBuffer, crs));
        }
        return mkMultiPolygon(geometries);
    }

    private <P extends Position> GeometryCollection<P, Geometry<P>>
    decodeGeometryCollection(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        int numGeometries = byteBuffer.getInt();
        if (numGeometries == 0) {
            return new GeometryCollection<P, Geometry<P>>(crs);
        }
        List<Geometry<P>> geometries = new ArrayList<Geometry<P>>(numGeometries);
        for (int i = 0; i < numGeometries; i++) {
            geometries.add(decodeGeometry(byteBuffer, crs));
        }
        return mkGeometryCollection(geometries);
    }

    private <P extends Position> Polygon<P> decodePolygon(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        int numRings = byteBuffer.getInt();
        List<LinearRing<P>> rings = readPolygonRings(numRings, byteBuffer, crs);
        return mkPolygon(rings);
    }

    private <P extends Position> LineString<P> decodeLineString(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        int numPoints = byteBuffer.getInt();
        PositionSequence<P> points = readPositions(numPoints, byteBuffer, crs);
        return new LineString<P>(points, crs);
    }

    private <P extends Position>  Point<P> decodePoint(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        PositionSequence<P> points = readPositions(1, byteBuffer, crs);
        return new Point<P>(points, crs);
    }

    private <P extends Position> PositionSequence<P> readPositions(int numPos, ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        PositionSequenceBuilder<P> psBuilder = PositionSequenceBuilders.fixedSized(numPos, crs.getPositionClass());
        double[] coordinates = new double[crs.getCoordinateDimension()];
        for (int i = 0; i < numPos; i++) {
            readPosition(byteBuffer, coordinates, crs);
            psBuilder.add(coordinates);
        }
        return psBuilder.toPositionSequence();
    }

    private <P extends Position> void readPosition(ByteBuffer byteBuffer, double[] coordinates, CoordinateReferenceSystem<P> crs) {
        for (int ci = 0; ci < crs.getCoordinateDimension(); ci++) {
            coordinates[ci] = byteBuffer.getDouble();
        }
    }

    private <P extends Position> List<LinearRing<P>> readPolygonRings(int numRings, ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        List<LinearRing<P>> rings = new ArrayList<LinearRing<P>>(numRings);
        for (int i = 0; i < numRings; i++) {
            rings.add(readRing(byteBuffer, crs));
        }
        return rings;
    }

    private <P extends Position> LinearRing<P> readRing(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        int numPoints = byteBuffer.getInt();
        PositionSequence<P> ps = readPositions(numPoints, byteBuffer, crs);
        try {
            return new LinearRing<P>(ps, crs);
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
     * Read and set the SRID (if it is present)
     *
     * @param byteBuffer
     * @param typeCode
     * @param crs
     */
    protected abstract <P extends Position> CoordinateReferenceSystem<P> readCrs(ByteBuffer byteBuffer, int typeCode, CoordinateReferenceSystem<P> crs);

    protected abstract boolean hasSrid(int typeCode);

    protected int readTypeCode(ByteBuffer byteBuffer) {
        return (int) byteBuffer.getUInt();
    }


    private void alignByteOrder(ByteBuffer byteBuffer) {
        byte orderByte = byteBuffer.get();
        ByteOrder byteOrder = ByteOrder.valueOf(orderByte);
        byteBuffer.setByteOrder(byteOrder);
    }
}
