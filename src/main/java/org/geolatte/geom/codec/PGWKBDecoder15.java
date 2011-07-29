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


import org.geolatte.geom.crs.CartesianCoordinateSystem;
import org.geolatte.geom.*;
import org.geolatte.geom.FixedSizePointSequenceBuilder;

/**
 * A WKB Decoder for PostGIS EWKB (version Postgis 1.5).
 * <p/>
 * <p>This WKBDecoder supports the EWKB dialect of PostGIS versions 1.0 tot 1.5.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Nov 11, 2010
 */
public class PGWKBDecoder15 {

    private int SRID = -1;

    public Geometry decode(Bytes bytes) {
        bytes.rewind();
        return decodeGeometry(bytes);
    }

    public Geometry decodeGeometry(Bytes bytes) {
        alignByteOrder(bytes);
        int typeCode = readTypeCode(bytes);
        WKBGeometryType wkbType = WKBGeometryType.parse((byte) typeCode);
        readSRID(bytes, typeCode);
        CartesianCoordinateSystem flag = getCoordinateDimension(typeCode);
        switch (wkbType) {
            case POINT:
                return decodePoint(bytes, flag);
            case LINE_STRING:
                return decodeLineString(bytes, flag);
            case POLYGON:
                return decodePolygon(bytes, flag);
            case GEOMETRY_COLLECTION:
                return decodeGeometryCollection(bytes);
            case MULTI_POINT:
                return decodeMultiPoint(bytes);
            case MULTI_POLYGON:
                return decodeMultiPolygon(bytes);
            case MULTI_LINE_STRING:
                return decodeMultiLineString(bytes);

        }
        throw new IllegalStateException(String.format("WKBType %s is not supported.", wkbType));
    }

    private MultiLineString decodeMultiLineString(Bytes bytes) {
        int numGeometries = bytes.getInt();
        LineString[] geometries = new LineString[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = (LineString) decodeGeometry(bytes);
        }
        return MultiLineString.create(geometries,SRID);
    }


    private MultiPoint decodeMultiPoint(Bytes bytes) {
        int numGeometries = bytes.getInt();
        Point[] geometries = new Point[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = (Point) decodeGeometry(bytes);
        }
        return MultiPoint.create(geometries, SRID);
    }

    private MultiPolygon decodeMultiPolygon(Bytes bytes) {
        int numGeometries = bytes.getInt();
        Polygon[] geometries = new Polygon[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = (Polygon) decodeGeometry(bytes);
        }
        return MultiPolygon.create(geometries, SRID);
    }

    private GeometryCollection decodeGeometryCollection(Bytes bytes) {
        int numGeometries = bytes.getInt();
        Geometry[] geometries = new Geometry[numGeometries];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = decodeGeometry(bytes);
        }
        return GeometryCollection.create(geometries,SRID);
    }

    private Polygon decodePolygon(Bytes bytes, CartesianCoordinateSystem flag) {
        int numRings = bytes.getInt();
        LinearRing[] rings = readPolygonRings(numRings, bytes, flag, SRID);
        return Polygon.create(rings, SRID);
    }

    private LineString decodeLineString(Bytes bytes, CartesianCoordinateSystem flag) {
        int numPoints = bytes.getInt();
        PointSequence points = readPoints(numPoints, bytes, flag);
        return LineString.create(points, SRID);
    }

    private Point decodePoint(Bytes bytes, CartesianCoordinateSystem flag) {
        PointSequence points = readPoints(1, bytes, flag);
        return Point.create(points, SRID);
    }

    protected PointSequence readPoints(int numPoints, Bytes bytes, CartesianCoordinateSystem coordinateSystem) {
        FixedSizePointSequenceBuilder psBuilder = new FixedSizePointSequenceBuilder(numPoints, coordinateSystem);
        double[] coordinates = new double[coordinateSystem.getCoordinateDimension()];
        for (int i = 0; i < numPoints; i++) {
            readPoint(bytes, coordinateSystem, coordinates);
            psBuilder.add(coordinates);
        }
        return psBuilder.toPointSequence();
    }

    private void readPoint(Bytes bytes, CartesianCoordinateSystem coordinateSystem, double[] coordinates) {
        for (int ci = 0; ci < coordinateSystem.getCoordinateDimension(); ci++) {
            coordinates[ci] = bytes.getDouble();
        }
    }

    protected LinearRing[] readPolygonRings(int numRings, Bytes bytes, CartesianCoordinateSystem coordinateSystem, int SRID) {
        LinearRing[] rings = new LinearRing[numRings];
        for (int i = 0; i < numRings; i++) {
            rings[i] = readRing(bytes, coordinateSystem, SRID);
        }
        return rings;
    }

    private LinearRing readRing(Bytes bytes, CartesianCoordinateSystem coordinateSystem, int SRID) {
        int numPoints = bytes.getInt();
        FixedSizePointSequenceBuilder psBuilder = new FixedSizePointSequenceBuilder(numPoints, coordinateSystem);
        double[] coordinates = new double[coordinateSystem.getCoordinateDimension()];
        for (int i = 0; i < numPoints; i++) {
            readPoint(bytes, coordinateSystem, coordinates);
            psBuilder.add(coordinates);
        }
        return LinearRing.create(psBuilder.toPointSequence(), SRID);
    }


    private CartesianCoordinateSystem getCoordinateDimension(int typeCode) {
        boolean hasM = (typeCode & PGWKBTypeMasks.M_FLAG) == PGWKBTypeMasks.M_FLAG;
        boolean hasZ = (typeCode & PGWKBTypeMasks.Z_FLAG) == PGWKBTypeMasks.Z_FLAG;
        return CartesianCoordinateSystem.parse(hasZ, hasM);
    }

    private void readSRID(Bytes bytes, int typeCode) {
        if (hasSrid(typeCode)) {
            SRID = bytes.getInt();
        }
    }

    private boolean hasSrid(int typeCode) {
        return (typeCode & PGWKBTypeMasks.SRID_FLAG) == PGWKBTypeMasks.SRID_FLAG;
    }


    private int readTypeCode(Bytes bytes) {
        return (int) bytes.getUInt();
    }

    private void alignByteOrder(Bytes bytes) {
        byte orderByte = bytes.get();
        WKBByteOrder byteOrder = WKBByteOrder.valueOf(orderByte);
        bytes.setWKBByteOrder(byteOrder);
    }

}
