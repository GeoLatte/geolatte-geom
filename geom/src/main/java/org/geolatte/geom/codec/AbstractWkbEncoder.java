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
    public  <P extends Position> ByteBuffer encode(Geometry<P> geometry, ByteOrder byteOrder) {
        ByteBuffer output = ByteBuffer.allocate(calculateSize(geometry, true));
        if (byteOrder != null) {
            output.setByteOrder(byteOrder);
        }
        writeGeometry(geometry, output);
        output.rewind();
        return output;
    }

    protected <P extends Position> void writeGeometry(Geometry<P> geom, ByteBuffer output) {
        geom.accept(newWkbVisitor(output, geom));
    }

    protected <P extends Position> WkbVisitor<P> newWkbVisitor(ByteBuffer output, Geometry<P> geometry) {
        return new WkbVisitor<P>(output);
    }

    protected <P extends Position>  int calculateSize(Geometry<P> geom, boolean includeSrid) {
        int size = 1 + ByteBuffer.UINT_SIZE; //size for order byte + type field
        if (geom.getSRID() > 0 && includeSrid) {
            size += 4;
        }
        //empty geoms have same representation as an empty GeometryCollection
        if (geom.isEmpty()) return size + sizeEmptyGeometry(geom);
        if (geom instanceof GeometryCollection) {
            size += sizeOfGeometryCollection((GeometryCollection<P, ?>) geom);
        } else if (geom instanceof Polygon) {
            size += getPolygonSize((Polygon<P>) geom);
        } else if (geom instanceof Point) {
            size += getPointByteSize(geom);
        } else {
            size += ByteBuffer.UINT_SIZE; //to hold number of points
            size += getPointByteSize(geom) * geom.getNumPositions();
        }
        return size;
    }

    abstract protected <P extends Position> int sizeEmptyGeometry(Geometry<P> geometry);

    private <P extends Position> int getPointByteSize(Geometry<P> geom) {
        return geom.getCoordinateDimension() * ByteBuffer.DOUBLE_SIZE;
    }


    private <P extends Position> int getPolygonSize(Polygon<P> geom) {
        //to hold the number of linear rings
        int size = ByteBuffer.UINT_SIZE;
        //for each linear ring, a UINT holds the number of points
        size += geom.isEmpty() ? 0 : ByteBuffer.UINT_SIZE * (geom.getNumInteriorRing() + 1);
        size += getPointByteSize(geom) * geom.getNumPositions();
        return size;
    }

    private <P extends Position, G extends Geometry<P>> int sizeOfGeometryCollection(GeometryCollection<P,G> collection) {
        int size = ByteBuffer.UINT_SIZE;
        for (G g : collection) {
            size += calculateSize(g, false);
        }
        return size;
    }


}
