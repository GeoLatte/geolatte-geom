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
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.geolatte.geom.codec.*;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.jts.JTS;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/26/11
 */
public class WKTSpeedTest extends SimpleBenchmark {


    public WKTSpeedTest() {

    }

    public double timeAsJTS(int reps) {
        double dummy = 0;
        for (int i = 0; i < reps; i++) {
            for (Geometry geometry : TestDataSet.geometries) {
                com.vividsolutions.jts.geom.Geometry geometry1 = JTS.to(geometry);
                dummy = geometry1.getCoordinate().x;
            }
        }
        return dummy;
    }

    public double timeJTSToWKT(int reps){
        long dummy = 0;
        for (int i = 0; i < reps; i++) {
            for (com.vividsolutions.jts.geom.Geometry geom : TestDataSet.jtsGeoms) {
                String wkt = geom.toText();
                dummy += wkt.length();
            }
        }
        return dummy;
    }

    public double timeToWKT(int reps){
        long dummy = 0;
        for (int i = 0; i < reps; i++) {
            for (Geometry geom : TestDataSet.geometries) {
                PGWKTEncoder15 encoder = new PGWKTEncoder15();
                String wkt = encoder.encode(geom);
                dummy += wkt.length();
            }
        }
        return dummy;
    }

    public double timeFromWKTToJTS(int reps) throws ParseException {
        double dummy = 0;
        WKTReader reader = new WKTReader();
        for (int i = 0; i < reps; i++) {
            for (String s : TestDataSet.wktStrings) {
                com.vividsolutions.jts.geom.Geometry geom = reader.read(s);
                dummy += geom.getCoordinate().x;
            }
        }
        return dummy;
    }

    public double timeFromWKT(int reps) {
        double dummy = 0;
        PGWKTDecoder15 decoder = new PGWKTDecoder15();
        for (int i = 0; i < reps; i++) {
            for (String s : TestDataSet.wktStrings) {
                Geometry geom = decoder.decode(s);
                dummy += geom.getPointN(0).getX();
            }
        }
        return dummy;
    }

    public static void main(String... args) throws Exception {
        Runner.main(WKTSpeedTest.class, args);
    }

}
