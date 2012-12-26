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

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.crs.CrsId;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/1/12
 */
public class MySqlWkbEncoder extends AbstractWkbEncoder {

    @Override
    public ByteBuffer encode(Geometry geometry, ByteOrder byteOrder) {
        if (geometry == null || hasEmpty(geometry)) return null;
        //size is size for WKB + 4 bytes for the SRID
        ByteBuffer output = ByteBuffer.allocate(calculateSize(geometry, false) + 4);
        if (byteOrder != null) {
            output.setByteOrder(byteOrder);
        }
        output.putInt(geometry.getCrsId() == CrsId.UNDEFINED ? 0 : geometry.getSRID());
        writeGeometry(geometry, output);
        output.rewind();
        return output;
    }

    @Override
    protected int sizeEmptyGeometry(Geometry geometry) {
        return 0;
    }

    private boolean hasEmpty(Geometry geometry) {
        if (geometry.isEmpty()) {
            return true;
        }
        if (geometry instanceof GeometryCollection) {
            for (Geometry part : (GeometryCollection) geometry) {
                if (hasEmpty(part)) return true;
            }
        }
        return false;
    }
}
