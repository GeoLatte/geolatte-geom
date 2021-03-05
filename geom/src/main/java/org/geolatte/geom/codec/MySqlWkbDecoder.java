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
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

/**
 * @author Karel Maesen, Geovise BVBA
 * creation-date: 11/1/12
 */
public class MySqlWkbDecoder implements WkbDecoder {


    @Override
    public <P extends Position> Geometry<P> decode(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        BaseWkbParser<P> parser = new MySqlWkbParser<P>(MySqlWkbDialect.INSTANCE, byteBuffer, crs);
        try {
            return parser.parse();
        } catch (Throwable t) {
            throw new WkbDecodeException(t);
        }
    }
}

@SuppressWarnings("unchecked")
class MySqlWkbParser<P extends Position> extends BaseWkbParser<P> {

    MySqlWkbParser(WkbDialect dialect, ByteBuffer buffer, CoordinateReferenceSystem<P> crs) {
        super(dialect, buffer, crs);
        this.buffer.setByteOrder(ByteOrder.NDR);
        int srid = this.buffer.getInt();
        if (crs == null) {
            CoordinateReferenceSystem<?> crsDeclared = CrsRegistry.getCoordinateReferenceSystemForEPSG(srid, CoordinateReferenceSystems.PROJECTED_2D_METER);
            this.embeddedCRS = (CoordinateReferenceSystem<P>) crsDeclared;
        }
    }

}
