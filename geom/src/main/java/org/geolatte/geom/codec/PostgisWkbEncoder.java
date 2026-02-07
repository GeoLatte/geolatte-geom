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
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

/**
 * A WKBEncoder for the PostGIS EWKB dialect (for Postgis  versions &lt; 2.2.2).
 *
 *<p>This {@code WKBEncoder} encodes empty points as empty GeometryCollections. This convention
 * was changed in Postgis version 2.2.2 to an encoding with NaN coordinates. For the later convention
 * use the {@code PostgisWkbV2Encoder}</p>
 *
 * @author Karel Maesen, Geovise BVBA
 * creation-date: Nov 11, 2010
 * @see PostgisWkbV2Encoder
 */
public class PostgisWkbEncoder implements WkbEncoder {
    final private WkbDialect dialect;

    protected PostgisWkbEncoder(WkbDialect dialect) {
        this.dialect = dialect;
    }

    protected PostgisWkbEncoder() {
        this(PostgisWkbV1Dialect.INSTANCE);
    }

    @Override
    public <P extends Position> ByteBuffer encode(Geometry<P> geometry, ByteOrder byteOrder) {
        BaseWkbVisitor<P> visitor = dialect.mkVisitor(geometry, byteOrder);
        geometry.accept(visitor);
        return visitor.result();
    }
}



