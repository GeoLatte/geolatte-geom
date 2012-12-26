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
abstract class AbstractWkbEncoder implements WkbEncoder {
    /**
     * Encodes a <code>Geometry</code> into a WKB representation using the specified byte-order.
     *
     * @param geometry  The <code>Geometry</code> to be encoded as WKB.
     * @param byteOrder The WKB byte order, either {@link org.geolatte.geom.ByteOrder#XDR XDR} or {@link org.geolatte.geom.ByteOrder#NDR NDR}
     * @return A buffer of bytes that contains the WKB-encoded <code>Geometry</code>.
     */
    @Override
    public ByteBuffer encode(Geometry geometry, ByteOrder byteOrder) {
        ByteBuffer output = ByteBuffer.allocate(calculateSize(geometry, true));
        if (byteOrder != null) {
            output.setByteOrder(byteOrder);
        }
        writeGeometry(geometry, output);
        output.rewind();
        return output;
    }

    protected void writeGeometry(Geometry geom, ByteBuffer output) {
        geom.accept(newWkbVisitor(output));
    }

    protected WkbVisitor newWkbVisitor(ByteBuffer output) {
        return new WkbVisitor(output);
    }

    protected int calculateSize(Geometry geom, boolean includeSrid) {
        int size = 1 + ByteBuffer.UINT_SIZE; //size for order byte + type field
        if (geom.getSRID() > 0 && includeSrid) {
            size += 4;
        }
        //empty geoms have same representation as an empty GeometryCollection
        if (geom.isEmpty()) return size + sizeEmptyGeometry(geom);
        if (geom instanceof GeometryCollection) {
            size += sizeOfGeometryCollection((GeometryCollection) geom);
        } else if (geom instanceof Polygon) {
            size += getPolygonSize((Polygon) geom);
        } else if (geom instanceof Point) {
            size += getPointByteSize(geom);
        } else if (geom instanceof PolyHedralSurface) {
            size += getPolyHedralSurfaceSize((PolyHedralSurface) geom);
        } else {
            size += ByteBuffer.UINT_SIZE; //to hold number of points
            size += getPointByteSize(geom) * geom.getNumPoints();
        }
        return size;
    }

    abstract protected int sizeEmptyGeometry(Geometry geometry);

    private int getPointByteSize(Geometry geom) {
        return geom.getCoordinateDimension() * ByteBuffer.DOUBLE_SIZE;
    }

    private int getPolyHedralSurfaceSize(PolyHedralSurface geom) {
        int size = ByteBuffer.UINT_SIZE;
        for (int i = 0; i < geom.getNumPatches(); i++) {
            size += getPolygonSize(geom.getPatchN(i));
        }
        return size;
    }

    private int getPolygonSize(Polygon geom) {
        //to hold the number of linear rings
        int size = ByteBuffer.UINT_SIZE;
        //for each linear ring, a UINT holds the number of points
        size += geom.isEmpty() ? 0 : ByteBuffer.UINT_SIZE * (geom.getNumInteriorRing() + 1);
        size += getPointByteSize(geom) * geom.getNumPoints();
        return size;
    }

    private int sizeOfGeometryCollection(GeometryCollection collection) {
        int size = ByteBuffer.UINT_SIZE;
        for (Geometry g : collection) {
            size += calculateSize(g, false);
        }
        return size;
    }


}
