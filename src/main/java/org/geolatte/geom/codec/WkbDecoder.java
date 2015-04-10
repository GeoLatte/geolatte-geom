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
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A decoder for WKB (Well-Known Binary) encoded <code>Geometries</code>.
 * <p/>
 * <p>In general <code>WkbDecoder</code> implementations are not be thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 9/29/12
 */
public interface WkbDecoder {

    /**
     * Decodes a WKB encoded representation of a <code>Geometry</code>
     *
     * @param byteBuffer the WKB encoded binary representation
     * @return the represented <code>Geometry</code>
     * @throws WkbDecodeException if the specified ByteBuffer is an illegal or unsupported WKB representation
     */
    Geometry<?> decode(ByteBuffer byteBuffer);


    /**
     * Decodes a WKB encoded representation of a <code>Geometry</code>, assuming the specified
     * {@code CoordinateReferenceSystem}
     *
     * @param byteBuffer the WKB encoded binary representation
     * @param crs        the base coordinate reference system assumed for the wkt
     * @param <P>        the Position type for the (base)
     * @return the represented <code>Geometry</code>
     * @throws WkbDecodeException if the specified ByteBuffer is an illegal or unsupported WKB representation
     */
    <P extends Position> Geometry<P> decode(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs);
}
