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

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/1/12
 */
class WkbVisitor implements GeometryVisitor {


    private final ByteBuffer output;


    WkbVisitor(ByteBuffer byteBuffer) {
        this.output = byteBuffer;
    }

    @Override
    public void visit(Point geom) {
        writeByteOrder(output);
        DimensionalFlag dimension = DimensionalFlag.valueOf(geom.is3D(), geom.isMeasured());
        writeTypeCodeAndSrid(geom, dimension, output);
        if (geom.isEmpty()) {
            output.putUInt(0);
        } else {
            writePoints(geom.getPoints(), geom.getCoordinateDimension(), output);
        }
    }

    @Override
    public void visit(LineString geom) {
        writeByteOrder(output);
        DimensionalFlag dimension = DimensionalFlag.valueOf(geom.is3D(), geom.isMeasured());
        writeTypeCodeAndSrid(geom, dimension, output);
        if (geom.isEmpty()) {
            output.putUInt(0);
        } else {
            output.putUInt(geom.getNumPoints());
            writePoints(geom.getPoints(), geom.getCoordinateDimension(), output);
        }
    }

    @Override
    public void visit(Polygon geom) {
        writeByteOrder(output);
        DimensionalFlag dimension = DimensionalFlag.valueOf(geom.is3D(), geom.isMeasured());
        writeTypeCodeAndSrid(geom, dimension, output);
        if (geom.isEmpty()) {
            output.putUInt(0);
        } else {
            writeNumRings(geom, output);
            for (LinearRing ring : geom) {
                writeRing(ring);
            }
        }
    }

    @Override
    public void visit(PolyHedralSurface geom) {
        writeByteOrder(output);
        DimensionalFlag dimension = DimensionalFlag.valueOf(geom.is3D(), geom.isMeasured());
        writeTypeCodeAndSrid(geom, dimension, output);
        if (geom.isEmpty()) {
            output.putUInt(0);
        } else {
            output.putUInt(geom.getNumPatches());
            for (Polygon pg : geom) {
                pg.accept(this);
            }
        }
    }

    @Override
    public void visit(GeometryCollection geom) {
        writeByteOrder(output);
        DimensionalFlag dimension = DimensionalFlag.valueOf(geom.is3D(), geom.isMeasured());
        writeTypeCodeAndSrid(geom, dimension, output);
        output.putUInt(geom.getNumGeometries());
    }

    @Override
    public void visit(LinearRing geom) {
        writeByteOrder(output);
        DimensionalFlag dimension = DimensionalFlag.valueOf(geom.is3D(), geom.isMeasured());
        writeTypeCodeAndSrid(geom, dimension, output);
        if (geom.isEmpty()) {
            output.putUInt(0);
        } else {
            writeRing(geom);
        }
    }

    protected void writeRing(LinearRing geom) {
        output.putUInt(geom.getNumPoints());
        writePoints(geom.getPoints(), geom.getCoordinateDimension(), output);
    }

    protected void writeNumRings(Polygon geom, ByteBuffer byteBuffer) {
        byteBuffer.putUInt(geom.isEmpty() ? 0 : geom.getNumInteriorRing() + 1);
    }

    protected void writePoint(double[] coordinates, ByteBuffer output) {
        for (double coordinate : coordinates) {
            output.putDouble(coordinate);
        }
    }

    protected void writePoints(PointSequence points, int coordinateDimension, ByteBuffer output) {
        double[] coordinates = new double[coordinateDimension];
        for (int i = 0; i < points.size(); i++) {
            points.getCoordinates(coordinates, i);
            writePoint(coordinates, output);
        }
    }

    protected void writeByteOrder(ByteBuffer output) {
        output.put(output.getByteOrder().byteValue());
    }

    protected void writeTypeCodeAndSrid(Geometry geometry, DimensionalFlag dimension, ByteBuffer output) {
        int typeCode = getGeometryType(geometry);
        output.putUInt(typeCode);
    }

    protected int getGeometryType(Geometry geometry) {
        //empty geometries have the same representation as an empty geometry collection
        if (geometry.isEmpty()) {
            return WkbGeometryType.GEOMETRY_COLLECTION.getTypeCode();
        }
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

}

