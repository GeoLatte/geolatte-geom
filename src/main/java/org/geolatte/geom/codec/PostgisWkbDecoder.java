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
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.codec;


import org.geolatte.geom.*;

/**
 * A Wkb Decoder for PostGIS EWKB (as implemented in Postgis 1.5).
 *
 * <p/>
 * <p>This WKBDecoder supports the EWKB dialect of PostGIS versions 1.0 tot 1.5+.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Nov 11, 2010
 */
public class PostgisWkbDecoder {

    private int SRID = -1;

    /**
     * Decodes a Postgis WKB representation of a <code>Geometry</code>.
     *
     * @param byteBuffer
     * @return
     */
    public Geometry decode(ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        return decodeGeometry(byteBuffer);
    }

    private Geometry decodeGeometry(ByteBuffer byteBuffer) {
        alignByteOrder(byteBuffer);
        int typeCode = readTypeCode(byteBuffer);
        WkbGeometryType wkbType = WkbGeometryType.parse((byte) typeCode);
        readSRIDIfPresent(byteBuffer, typeCode);
        DimensionalFlag flag = getCoordinateDimension(typeCode);
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
        throw new IllegalStateException(String.format("WKBType %s is not supported.", wkbType));
    }

    private MultiLineString decodeMultiLineString(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        LineString[] geometries = new LineString[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = (LineString) decodeGeometry(byteBuffer);
        }
        return MultiLineString.create(geometries,SRID);
    }


    private MultiPoint decodeMultiPoint(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        Point[] geometries = new Point[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = (Point) decodeGeometry(byteBuffer);
        }
        return MultiPoint.create(geometries, SRID);
    }

    private MultiPolygon decodeMultiPolygon(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        Polygon[] geometries = new Polygon[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = (Polygon) decodeGeometry(byteBuffer);
        }
        return MultiPolygon.create(geometries, SRID);
    }

    private GeometryCollection decodeGeometryCollection(ByteBuffer byteBuffer) {
        int numGeometries = byteBuffer.getInt();
        Geometry[] geometries = new Geometry[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = decodeGeometry(byteBuffer);
        }
        return GeometryCollection.create(geometries,SRID);
    }

    private Polygon decodePolygon(ByteBuffer byteBuffer, DimensionalFlag flag) {
        int numRings = byteBuffer.getInt();
        LinearRing[] rings = readPolygonRings(numRings, byteBuffer, flag, SRID);
        return Polygon.create(rings, SRID);
    }

    private LineString decodeLineString(ByteBuffer byteBuffer, DimensionalFlag flag) {
        int numPoints = byteBuffer.getInt();
        PointSequence points = readPoints(numPoints, byteBuffer, flag);
        return LineString.create(points, SRID);
    }

    private Point decodePoint(ByteBuffer byteBuffer, DimensionalFlag flag) {
        PointSequence points = readPoints(1, byteBuffer, flag);
        return Point.create(points, SRID);
    }

    private PointSequence readPoints(int numPoints, ByteBuffer byteBuffer, DimensionalFlag dimensionalFlag) {
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(numPoints, dimensionalFlag);
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

    private LinearRing[] readPolygonRings(int numRings, ByteBuffer byteBuffer, DimensionalFlag dimensionalFlag, int SRID) {
        LinearRing[] rings = new LinearRing[numRings];
        for (int i = 0; i < numRings; i++) {
            rings[i] = readRing(byteBuffer, dimensionalFlag, SRID);
        }
        return rings;
    }

    private LinearRing readRing(ByteBuffer byteBuffer, DimensionalFlag dimensionalFlag, int SRID) {
        int numPoints = byteBuffer.getInt();
        PointSequence ps = readPoints(numPoints,byteBuffer, dimensionalFlag);
        return LinearRing.create(ps, SRID);
    }


    private DimensionalFlag getCoordinateDimension(int typeCode) {
        boolean hasM = (typeCode & PostgisWkbTypeMasks.M_FLAG) == PostgisWkbTypeMasks.M_FLAG;
        boolean hasZ = (typeCode & PostgisWkbTypeMasks.Z_FLAG) == PostgisWkbTypeMasks.Z_FLAG;
        return DimensionalFlag.valueOf(hasZ, hasM);
    }

    private void readSRIDIfPresent(ByteBuffer byteBuffer, int typeCode) {
        if (hasSrid(typeCode)) {
            SRID = byteBuffer.getInt();
        }
    }

    private boolean hasSrid(int typeCode) {
        return (typeCode & PostgisWkbTypeMasks.SRID_FLAG) == PostgisWkbTypeMasks.SRID_FLAG;
    }


    private int readTypeCode(ByteBuffer byteBuffer) {
        return (int) byteBuffer.getUInt();
    }

    private void alignByteOrder(ByteBuffer byteBuffer) {
        byte orderByte = byteBuffer.get();
        WkbByteOrder byteOrder = WkbByteOrder.valueOf(orderByte);
        byteBuffer.setWKBByteOrder(byteOrder);
    }

}
