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

import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.codec.PGWKTEncoder15;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.jts.JTS;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/26/11
 */
public class TestDataSet {

    public static final int SIZE = 100;
    public static Geometry[] geometries = RandomGeometryGenerator.createRandomGeometries(GeometryType.LINE_STRING, SIZE, DimensionalFlag.XY);
    public static  com.vividsolutions.jts.geom.Geometry[] jtsGeoms = new com.vividsolutions.jts.geom.Geometry[SIZE];
    public static String[] wktStrings = new String[SIZE];

    static {

        int i = 0;
        for (Geometry geom : TestDataSet.geometries) {
            TestDataSet.jtsGeoms[i++] = JTS.to(geom);
        }

        for (i = 0; i < SIZE; i++) {
            PGWKTEncoder15 encoder = new PGWKTEncoder15();
            wktStrings[i] = encoder.encode(geometries[i]);
        }
    }


}
