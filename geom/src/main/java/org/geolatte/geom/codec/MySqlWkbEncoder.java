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
 * A WKB Encoder for MySQL
 *
 * @author Karel Maesen, Geovise BVBA
 * creation-date: 11/1/12
 */
public class MySqlWkbEncoder implements WkbEncoder {

    @Override
    public <P extends Position> ByteBuffer encode(Geometry<P> geometry, ByteOrder byteOrder) {
        Geometry<P> toEncode = geometry;
        if(geometry.isEmpty()) {
            toEncode = Geometries.mkEmptyGeometryCollection(geometry.getCoordinateReferenceSystem());
        }
        BaseWkbVisitor<P> visitor = MySqlWkbDialect.INSTANCE.mkVisitor(toEncode, byteOrder);
        //first write SRID
        visitor.buffer().putInt(Math.max(toEncode.getSRID(), 0));
        toEncode.accept(visitor);
        return visitor.result();
    }

}

