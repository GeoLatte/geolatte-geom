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

import org.geolatte.geom.C2D;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.CrsWktDecoder;
import org.geolatte.geom.codec.WktDecodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A repository for <code>CoordinateReferenceSystem</code>s.
 * <p/>
 * <p>Currently, the registry is limited to EPSG-defined coordinate reference systems.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CrsRegistry {

    final private static Logger LOGGER = LoggerFactory.getLogger(CrsRegistry.class);
    final private static ConcurrentHashMap<Integer, CoordinateReferenceSystem<? extends Position>> crsMap = new ConcurrentHashMap<Integer, CoordinateReferenceSystem<? extends Position>>(4000);
    final private static Map<Integer, CrsId> crsIdMap = new HashMap<Integer, CrsId>(4000);
    final private static String DELIM = "\\|";

    static {
        try {
            loadCRS();
        } catch (IOException e) {
            throw new RuntimeException("Can't read spatial ref system definitions.");
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
        if (!"EPSG".equals(tokens[0])) {
            LOGGER.debug(String.format("Non-EPSG CRS ignored: %s", tokens[2]));
            return;
        }
        Integer srid = Integer.valueOf(tokens[1]);
        try {
            CoordinateReferenceSystem crs = decoder.decode(tokens[2], srid);
            crsMap.put(srid, crs);
            crsIdMap.put(srid, crs.getCrsId());
        } catch (WktDecodeException e) {
            LOGGER.warn(String.format("Can't parse srid %d (%s). \n%s", srid, tokens[2], e.getMessage()));
        } catch (InconsistentCoordinateSystemException e) {
            LOGGER.warn(String.format("Can't parse srid %d (%s) -- inconsistent coordinate system. \n%s", srid, tokens[2], e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.warn(String.format("Can't parse srid %d (%s) -- inconsistent coordinate system. \n%s", srid, tokens[2], e.getMessage()));
        }
    }

    /**
     * returns the <code>CoordinateReferenceSystem</code> for the specified EPSG code.
     *
     * @param epsgCode the EPSG code
     * @param fallback the coordinate
     * @return the <code>CoordinateReferenceSystem</code> corresponding to the specified EPSG code, or null if
     * no such system is registered.
     */
    public static CoordinateReferenceSystem<?> getCoordinateReferenceSystemForEPSG(int epsgCode,
                                                                                   CoordinateReferenceSystem<?>
                                                                                           fallback) {
        CoordinateReferenceSystem<?> crs = crsMap.get(epsgCode);
        return crs != null ? crs : fallback;
    }

    /**
     * Returns the registered coordinate reference system, or when unavailable in the registry, create a new Projected 2D system and register
     * this on-the-fly.
     *
     * @param epsgCode the code to look up
     * @return a CoordinateReferenceSystem with the specified epsg code
     */
    public static CoordinateReferenceSystem<?> ifAbsentReturnProjected2D(int epsgCode) {
        return crsMap.computeIfAbsent(epsgCode, key -> CoordinateReferenceSystems.mkProjected(key, LinearUnit.METER));
    }

    /**
     * Returns the registered coordinate reference system, or when unavailable in the registry, create a new Geographic 2D system and register
     * this on-the-fly.
     *
     * @param epsgCode the code to look up
     * @return a CoordinateReferenceSystem with the specified epsg code
     */
    public static CoordinateReferenceSystem<?> ifAbsentReturnGeographic2D(int epsgCode) {
        return crsMap.computeIfAbsent(epsgCode, key -> CoordinateReferenceSystems.mkGeographic(key, AngularUnit.RADIAN));
    }


    /**
     * Registers a {@code CoordinateReferenceSystem} in the registry under the specified (pseudo-)EPSG code.
     *
     * @param crs      the {@code CoordinateReferenceSystem} to register
     */
    public static void registerCoordinateReferenceSystem(CoordinateReferenceSystem<?> crs) {
        crsMap.put(crs.getCrsId().getCode(), crs);
    }

    /**
     * Determine if the registry contains the {@code CoordinateReferenceSystem} identified by its SRID
     *
     * @param epsgCode the SRID to look up
     * @return true iff the registry has a corresponding {@code CoordinateReferenceSystem}
     */
    public static boolean hasCoordinateReferenceSystemForEPSG(int epsgCode) {
        return crsMap.containsKey(epsgCode);
    }

    public static Geographic2DCoordinateReferenceSystem getGeographicCoordinateReferenceSystemForEPSG(int epsgCode) {
        CoordinateReferenceSystem<? extends Position> crs = crsMap.get(epsgCode);
        if (crs == null) return null;
        if (crs.getPositionClass().equals(G2D.class)) {
            return (Geographic2DCoordinateReferenceSystem) crs;
        }
        throw new RuntimeException(String.format("EPSG code %d doesn't refer to geographic projection system", epsgCode));
    }

    public static ProjectedCoordinateReferenceSystem getProjectedCoordinateReferenceSystemForEPSG(int epsgCode) {
        CoordinateReferenceSystem<? extends Position> crs = crsMap.get(epsgCode);
        if (crs.getPositionClass().equals(C2D.class)) {
            return (ProjectedCoordinateReferenceSystem) crs;
        }
        throw new RuntimeException(String.format("EPSG code %d doesn't refer to geographic projection system", epsgCode));
    }


    /**
     * Returns the {@code CrsId} for the specified EPSG Code.
     *
     * @param epsgCode the EPSG code
     * @return the <code>CrsId</code> corresponding to the specified EPSG code, or null if
     * no such system is registered.
     */
    public static CrsId getCrsIdForEPSG(int epsgCode) {
        return crsIdMap.get(epsgCode);
    }

}
