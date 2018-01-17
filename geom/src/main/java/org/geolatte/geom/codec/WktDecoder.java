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

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A decoder for WKT (Well-Known Text) representations.
 * <p>In general <code>WkbDecoder</code> implementations are not be thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 */
public interface WktDecoder {

    /**
     * Decodes a WKT representation.
     *
     * @param wkt the WKT string to decode
     * @return the decoded <code>Geometry</code>
     * @throws WktDecodeException when the String is an invalid or unsupported WKT representation
     */
    Geometry<?> decode(String wkt);

    /**
     * Decodes a WKT representation using the specified {@code CoordinateReferenceSystem}.
	 *
	 * <p>If a (non-null) {@code CoordinateReferenceSystem} is provided, the SRID information in the WKT will be ignored.</p>
     *
     * @param wkt the WKT string to decode
     * @param crs the base coordinate reference system assumed for the wkt.
     * @param <P> the Position type for the returned Geometry
     * @return the decoded {@code Geometry}
     * @throws WktDecodeException when the String is an invalid or unsupported WKT representation, or the WKT is not
     * consistent with the specified {@code CoordinateReferenceSystem}
     */
    <P extends Position> Geometry<P> decode(String wkt, CoordinateReferenceSystem<P> crs);



}
