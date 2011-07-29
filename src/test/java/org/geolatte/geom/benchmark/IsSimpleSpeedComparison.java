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

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import org.geolatte.geom.Geometry;


/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 5/3/11
 */
public class IsSimpleSpeedComparison extends SimpleBenchmark{

    public int timeJTSIsSimple(int reps) {
        int numSimple = 0;
        for (com.vividsolutions.jts.geom.Geometry geom : TestDataSet.jtsGeoms) {
            if (geom.isSimple()) numSimple++;
        }
        return numSimple;
    }

    public int timeIsSimple(int reps) {
        int numSimple = 0;
        for (Geometry geom : TestDataSet.geometries) {
            if (geom.isSimple()) numSimple++;
        }
        return numSimple;
    }


    public static void main(String... args) throws Exception {
        Runner.main(IsSimpleSpeedComparison.class, args);
    }


}
