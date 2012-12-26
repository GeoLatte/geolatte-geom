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


import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.Geometry;

/**
 * A WKBEncoder for the PostGIS EWKB dialect (versions 1.0 to 1.5).
 * <p/>
 * <p>This class is not thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Nov 11, 2010
 */
class PostgisWkbEncoder extends AbstractWkbEncoder {

    //size of an empty geometry is the size of an emp
    @Override
    protected int sizeEmptyGeometry(Geometry geometry) {
        return ByteBuffer.UINT_SIZE;
    }


    protected WkbVisitor newWkbVisitor(ByteBuffer output) {
        return new PostgisWkbVisitor(output);
    }

    static private class PostgisWkbVisitor extends WkbVisitor {

        private boolean hasWrittenSrid = false;

        PostgisWkbVisitor(ByteBuffer byteBuffer) {
            super(byteBuffer);
        }

        protected void writeTypeCodeAndSrid(Geometry geometry, DimensionalFlag dimension, ByteBuffer output) {
            int typeCode = getGeometryType(geometry);
            boolean hasSrid = (geometry.getSRID() > 0);
            if (hasSrid && !hasWrittenSrid) {
                typeCode |= PostgisWkbTypeMasks.SRID_FLAG;
            }
            if (dimension.isMeasured()) {
                typeCode |= PostgisWkbTypeMasks.M_FLAG;
            }
            if (dimension.is3D()) {
                typeCode |= PostgisWkbTypeMasks.Z_FLAG;
            }
            output.putUInt(typeCode);
            if (hasSrid && !hasWrittenSrid) {
                output.putInt(geometry.getSRID());
                hasWrittenSrid = true;
            }
        }

    }


}



