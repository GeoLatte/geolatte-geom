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

package org.geolatte.geom.benchmark;

import org.geolatte.geom.codec.PGWKTDecoder15;
import org.geolatte.geom.Geometry;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/27/11
 */
public class WKTDecodeProfiling {

    public static void main(String[] args) {
        double dummy = 0d;
        PGWKTDecoder15 decoder = new PGWKTDecoder15();
        for (String s : TestDataSet.wktStrings) {
            Geometry geom = decoder.decode(s);
            dummy += geom.getPointN(0).getX();
        }
        System.out.println("dummy = " + dummy);
    }

}
