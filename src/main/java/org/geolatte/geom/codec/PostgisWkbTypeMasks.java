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

/**
 * Bit masks for PostGIS Wkb Encoder/Decoder.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/19/11
 */
class PostgisWkbTypeMasks {

    /**
     * Mask for testing the presence of Z-coordinates in the WKB
     */
    public static final int Z_FLAG = 0x80000000;

    /**
     * Mask for testing the presence of M-coordinates in the WKB
     */
    public static final int M_FLAG = 0x40000000;

    /**
     * Mask for testing the presence of a SRID in the WKB
     */
    public static final int SRID_FLAG = 0x20000000;


}
