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

import org.geolatte.geom.Geometry;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for encoding/decoding WKT geometry representations.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class Wkt {

    public enum Dialect {
        //the PostGIS EWKT dialect (versions 1.0 to 1.5).
        POSTGIS_EWKT_1
    }

    private static final Dialect DEFAULT_DIALECT = Dialect.POSTGIS_EWKT_1;

    private static final Map<Dialect, Class<? extends WktDecoder>> DECODERS = new HashMap<Dialect, Class<? extends WktDecoder>>();
    private static final Map<Dialect, Class<? extends WktEncoder>> ENCODERS = new HashMap<Dialect, Class<? extends WktEncoder>>();

    static {
        DECODERS.put(Dialect.POSTGIS_EWKT_1, PostgisWktDecoder.class);
        ENCODERS.put(Dialect.POSTGIS_EWKT_1, PostgisWktEncoder.class);
    }


    /**
     * Decodes the specified WKT String to a <code>Geometry</code>.
     * <p>This method uses the default WKT dialect (Postgis v1.5 EWKT)</p>
     *
     * @param wkt the WKT string to decode
     * @return The decoded Geometry
     */
    public static Geometry fromWkt(String wkt) {
        WktDecoder<Geometry> decoder = newWktDecoder(DEFAULT_DIALECT);
        return decoder.decode(wkt);
    }

    /**
     * Encodes a <code>Geometry</code> to a WKT representation.
     * <p>This method uses the default WKT dialect (Postgis v1.5 EWKT)</p>
     *
     * @param geometry the <code>Geometry</code> to encode
     * @return the WKT representation of the given geometry
     */
    public static String toWkt(Geometry geometry) {
        WktEncoder encoder = newWktEncoder(DEFAULT_DIALECT);
        return encoder.encode(geometry);
    }

    /**
     * Creates a <code>WktDecoder</code> for the specified WKT <code>Dialect</code>.
     *
     * @param dialect the WKT dialect
     * @return an <code>WktDecoder</code> that supports the specified dialect
     */
    public static WktDecoder<Geometry> newWktDecoder(Dialect dialect) {
        Class<? extends WktDecoder> decoderClass = DECODERS.get(dialect);
        assert (decoderClass != null) : "A variant declared, but no encoder/decoder registered.";
        return createInstance(decoderClass);
    }

    /**
     * Creates a <code>WktEncoder</code> for the specified WKT <code>Dialect</code>.
     *
     * @param dialect the WKT dialect
     * @return an <code>WktEncoder</code> that supports the specified dialect
     */
    public static WktEncoder<Geometry> newWktEncoder(Dialect dialect) {
        Class<? extends WktEncoder> decoderClass = ENCODERS.get(dialect);
        assert (decoderClass != null) : "A variant declared, but no encoder/decoder registered.";
        return createInstance(decoderClass);
    }

    private static <T> T createInstance(Class<? extends T> codecClass) {
        if (codecClass == null) {
            return null;
        }
        try {
            return codecClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
