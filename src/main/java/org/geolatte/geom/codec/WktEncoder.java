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

/**
 * An Encoder for WKT (Well-Known Text) representations.
 * <p>Encoders are generic because both <code>Geometry</code>s and <code>CoordinateReferenceSystem</code>s can be
 * encoded.</p>
 * <p>In general <code>WktEncoder</code> implementations are not be thread-safe.</p>
 *
 * @param <T> the type to encode (can be <code>Geometry</code> or <code>CoordinateReferenceSystem</code>).
 *
 * @author Karel Maesen, Geovise BVBA
 */
public interface WktEncoder<T> {
    /**
     * Encodes an object to its WKT representation.
     * @param object the object to encode
     * @return a WKT representation of the specified object.
     */
    String encode(T object);
}
