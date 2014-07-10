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

    private CoordinateReferenceSystem<?> crs;

    //if a CRS is specified, is it consistent with the Wkb (in terms of presence of Vertical or Measure axes).
    private boolean crsValidated;


    @Override
    public <P extends Position> Geometry<P> decode(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        this.crs = crs;
        // if a null crs is specified, then no validation is needed.
        this.crsValidated = (crs == null);
        byteBuffer.rewind();
        try {
            prepare(byteBuffer);
            Geometry geom = decodeGeometry(byteBuffer);
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

    private Geometry<?> decodeGeometry(ByteBuffer byteBuffer) {
        alignByteOrder(byteBuffer);
        int typeCode = readTypeCode(byteBuffer);
        WkbGeometryType wkbType = WkbGeometryType.parse((byte) typeCode);
        readCrs(byteBuffer, typeCode);
        switch (wkbType) {
            case POINT:
                return decodePoint(byteBuffer);
            case LINE_STRING:
                return decodeLineString(byteBuffer);
            case POLYGON:
                return decodePolygon(byteBuffer);
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

    private MultiLineString<?> decodeMultiLineString(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        if (numGeometries == 0) {
            return new MultiLineString<>(crs);
        }
        List<LineString<?>> geometries = new ArrayList<>(numGeometries);
        for (int i = 0; i < numGeometries; i++) {
            geometries.add((LineString<?>) decodeGeometry(byteBuffer));
        }
        return mkMultiLineString(geometries);
    }

    private MultiPoint<?> decodeMultiPoint(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        if (numGeometries == 0) {
            return new MultiPoint<>(crs);
        }
        List<Point<?>> geometries = new ArrayList<>(numGeometries);
        for (int i = 0; i < numGeometries; i++) {
            geometries.add((Point<?>) decodeGeometry(byteBuffer));
        }
        return mkMultiPoint(geometries);
    }

    private MultiPolygon<?> decodeMultiPolygon(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        if (numGeometries == 0) {
            return new MultiPolygon<>(crs);
        }
        List<Polygon<?>> geometries = new ArrayList<>(numGeometries);
        for (int i = 0; i < numGeometries; i++) {
            geometries.add((Polygon<?>) decodeGeometry(byteBuffer));
        }
        return mkMultiPolygon(geometries);
    }

    private GeometryCollection<? extends Position, ? extends Geometry<?>> decodeGeometryCollection(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        if (numGeometries == 0) {
            return new GeometryCollection<>(crs);
        }
        List<Geometry<?>> geometries = new ArrayList<>(numGeometries);
        for (int i = 0; i < numGeometries; i++) {
            geometries.add(decodeGeometry(byteBuffer));
        }
        return mkGeometryCollection(geometries);
    }

    private Polygon<?> decodePolygon(ByteBuffer byteBuffer) {
        int numRings = byteBuffer.getInt();
        List<LinearRing<?>> rings = readPolygonRings(numRings, byteBuffer);
        return mkPolygon(rings);
    }

    private LineString decodeLineString(ByteBuffer byteBuffer) {
        int numPoints = byteBuffer.getInt();
        PositionSequence<?> points = readPositions(numPoints, byteBuffer);
        return new LineString<>(points);
    }

    private Point<?> decodePoint(ByteBuffer byteBuffer) {
        PositionSequence<?> points = readPositions(1, byteBuffer);
        return new Point<>(points);
    }

    private PositionSequence<?> readPositions(int numPos, ByteBuffer byteBuffer) {
        PositionSequenceBuilder<?> psBuilder = PositionSequenceBuilders.fixedSized(numPos, crs);
        double[] coordinates = new double[crs.getCoordinateDimension()];
        for (int i = 0; i < numPos; i++) {
            readPosition(byteBuffer, coordinates);
            psBuilder.add(coordinates);
        }
        return psBuilder.toPositionSequence();
    }

    private void readPosition(ByteBuffer byteBuffer, double[] coordinates) {
        for (int ci = 0; ci < crs.getCoordinateDimension(); ci++) {
            coordinates[ci] = byteBuffer.getDouble();
        }
    }

    private List<LinearRing<?>> readPolygonRings(int numRings, ByteBuffer byteBuffer) {
        List<LinearRing<?>> rings = new ArrayList<>(numRings);
        for (int i = 0; i < numRings; i++) {
            rings.add(readRing(byteBuffer));
        }
        return rings;
    }

    private LinearRing<?> readRing(ByteBuffer byteBuffer) {
        int numPoints = byteBuffer.getInt();
        PositionSequence<?> ps = readPositions(numPoints, byteBuffer);
        try {
            return new LinearRing<>(ps);
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
     */
    protected abstract void readCrs(ByteBuffer byteBuffer, int typeCode);

    protected abstract boolean hasSrid(int typeCode);

    protected int readTypeCode(ByteBuffer byteBuffer) {
        return (int) byteBuffer.getUInt();
    }

    protected CoordinateReferenceSystem<?> getCoordinateReferenceSystem() {
        return crs;
    }

    protected void setCoordinateReferenceSystem(CoordinateReferenceSystem<?> crs) {
        this.crs = crs;
    }

    protected boolean isCrsValidated() {
        return crsValidated;
    }

    protected void setCrsValidated(boolean crsValidated) {
        this.crsValidated = crsValidated;
    }


    private void alignByteOrder(ByteBuffer byteBuffer) {
        byte orderByte = byteBuffer.get();
        ByteOrder byteOrder = ByteOrder.valueOf(orderByte);
        byteBuffer.setByteOrder(byteOrder);
    }
}
