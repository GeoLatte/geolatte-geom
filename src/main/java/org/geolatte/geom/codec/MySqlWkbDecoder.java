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
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/1/12
 */
class MySqlWkbDecoder extends AbstractWkbDecoder {

    private int srid;


    /**
     * Read the first four bytes: this contains the SRID
     *
     * @param byteBuffer
     */
    @Override
    protected void prepare(ByteBuffer byteBuffer) {
        byteBuffer.setByteOrder(ByteOrder.NDR);
        srid = byteBuffer.getInt();

    }


    @Override
    protected <P extends Position> CoordinateReferenceSystem<P> readCrs(ByteBuffer byteBuffer, int typeCode, CoordinateReferenceSystem<P> crs) {
        // if a CRS is already specified, ignore this value
        if (crs != null) return crs;
        CoordinateReferenceSystem crsDeclared = CrsRegistry.getCoordinateReferenceSystemForEPSG(srid, CoordinateReferenceSystems.PROJECTED_2D_METER);
        return (CoordinateReferenceSystem<P>)crsDeclared;
    }

    @Override
    protected boolean hasSrid(int typeCode) {
        return true;
    }

}
