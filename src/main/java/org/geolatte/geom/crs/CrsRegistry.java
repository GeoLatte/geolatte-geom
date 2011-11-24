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

package org.geolatte.geom.crs;

import org.geolatte.geom.codec.CrsWktDecoder;
import org.geolatte.geom.codec.WktParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * A repository for <code>CoordinateReferenceSystem</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CrsRegistry {

    private static Logger LOGGER = LoggerFactory.getLogger(CrsRegistry.class);
    private static Map<Integer, CoordinateReferenceSystem> crsMap = new HashMap<Integer, CoordinateReferenceSystem>(4000);
    private static final String DELIM = "\\|";

    static {
        try {
            loadCRS();
        } catch (IOException e) {
            new RuntimeException("Can't read spatial ref system definitions.");
        }
    }

    private static void loadCRS() throws IOException {
        BufferedReader reader = createReader();
        try {
            String line = reader.readLine();
            CrsWktDecoder decoder = new CrsWktDecoder();
            while (line != null) {
                addDefinition(line, decoder);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
    }

    private static BufferedReader createReader() {
        InputStream in = CrsRegistry.class.getClassLoader().getResourceAsStream("spatial_ref_sys.txt");
        if (in == null) {
            throw new IllegalStateException("Can't find spatial_ref_sys definitions.");
        }
        return new BufferedReader(new InputStreamReader(in));
    }

    private static void addDefinition(String line, CrsWktDecoder decoder) {
        String[] tokens = line.split(DELIM);
        if (!"EPSG".equals(tokens[0])) return;
        Integer srid = Integer.valueOf(tokens[1]);
        try {
            CoordinateReferenceSystem crs = decoder.decode(tokens[2]);
            crsMap.put(srid, crs);
        } catch (WktParseException e) {
            LOGGER.warn(String.format("Can't parse srid %d (%s). \n%s", srid,tokens[2], e.getMessage()));
        }

    }

    /**
     * returns the <code>CoordinateReferenceSystem</code> for the specified EPSG code.
     *
     * @param epsgCode the EPSG code
     * @return the <code>CoordinateReferenceSystem</code> corresponding to the specified EPSG code, or null if
     *  no such system is registered.
     */
    public static CoordinateReferenceSystem getEPSG(int epsgCode) {
        return crsMap.get(epsgCode);
    }

    //TODO implement method lower
//    public static CoordinateReferenceSystem get(CrsId srcId){
//
//    }
}
