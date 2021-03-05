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
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.support.GeometryBuilder;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

/**
 * A Wkb Decoder for PostGIS EWKB
 *
 * <p>This WKB Decoder interprets Points with NaN coordinates as empty points.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 * creation-date: Nov 11, 2010
 */
public class PostgisWkbDecoder implements WkbDecoder {

    @Override
    public <P extends Position> Geometry<P> decode(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        BaseWkbParser<P> parser = new PostgisWkbParser<>(PostgisWkbV1Dialect.INSTANCE, byteBuffer, crs);
        try {
            return parser.parse();
        } catch (WkbDecodeException e) {
            throw e;
        } catch (Throwable e) {
            throw new WkbDecodeException(e);
        }
    }

}

class PostgisWkbParser<P extends Position> extends BaseWkbParser<P> {

    private boolean crsRead = false;

    PostgisWkbParser(WkbDialect dialect, ByteBuffer buffer, CoordinateReferenceSystem<P> crs) {
        super(dialect, buffer, crs);
    }

    @Override
    protected GeometryBuilder parseWkbType() {
        long tpe = buffer.getUInt();
        gtype = dialect.parseType(tpe);
        if (!crsRead) {
            readCrs(buffer, (int) tpe);
            crsRead = true;
        }
        return GeometryBuilder.create(gtype);
    }

    protected void readCrs(ByteBuffer byteBuffer, int typeCode) {
        hasM = (typeCode & PostgisWkbTypeMasks.M_FLAG) == PostgisWkbTypeMasks.M_FLAG;
        hasZ = (typeCode & PostgisWkbTypeMasks.Z_FLAG) == PostgisWkbTypeMasks.Z_FLAG;

        int srid = 0;
        if (hasSrid(typeCode)) {
            srid = byteBuffer.getInt();
        }
        embeddedCRS = CrsRegistry.getCoordinateReferenceSystemForEPSG(srid, CoordinateReferenceSystems.PROJECTED_2D_METER);
    }


    protected boolean hasSrid(int typeCode) {
        return (typeCode & PostgisWkbTypeMasks.SRID_FLAG) == PostgisWkbTypeMasks.SRID_FLAG;
    }


}
