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
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates encoders/decoders for WKT geometry representations.
 * <p/>
 * <p>Note that the <coder>WktEncoder</coder>/<code>WktDecoder</code> instances returned by the factory
 * methods are not thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class Wkt {

    public enum Dialect {
        //the PostGIS EWKT dialect (versions 1.0 to 1.5).
        POSTGIS_EWKT_1,
        MYSQL_WKT,
        HANA_EWKT,
        DB2_WKT
    }

    private static final Dialect DEFAULT_DIALECT = Dialect.POSTGIS_EWKT_1;

    private static final Map<Dialect, Class<? extends WktDecoder>> DECODERS = new HashMap<Dialect, Class<? extends WktDecoder>>();
    private static final Map<Dialect, Class<? extends WktEncoder>> ENCODERS = new HashMap<Dialect, Class<? extends WktEncoder>>();

    static {
        DECODERS.put(Dialect.POSTGIS_EWKT_1, PostgisWktDecoder.class);
        DECODERS.put(Dialect.MYSQL_WKT, PostgisWktDecoder.class); // use also the PostgisWktDecoder since it can handle everything from Mysql
        DECODERS.put(Dialect.HANA_EWKT, HANAWktDecoder.class);
        DECODERS.put(Dialect.DB2_WKT, Db2Decoder.class);
        ENCODERS.put(Dialect.POSTGIS_EWKT_1, PostgisWktEncoder.class);
        ENCODERS.put(Dialect.MYSQL_WKT, PostgisWktEncoder.class); // this is temporary, not everything it produces can be understood by MySQL
        ENCODERS.put(Dialect.HANA_EWKT, HANAWktEncoder.class);
        ENCODERS.put(Dialect.DB2_WKT, Db2WktEncoder.class);
    }


    /**
     * Decodes the specified WKT String to a <code>Geometry</code>.
     * <p>This method uses the default WKT dialect (Postgis v1.5 EWKT)</p>
     *
     * @param wkt the WKT string to decode
     * @return The decoded Geometry
     */
    public static <P extends Position> Geometry<P> fromWkt(String wkt, CoordinateReferenceSystem<P> crs) {
        WktDecoder decoder = newDecoder();
        return decoder.decode(wkt,crs);
    }

    public static Geometry<?> fromWkt(String wkt) {
        WktDecoder decoder = newDecoder();
        return decoder.decode(wkt);
    }

    /**
     * Encodes a <code>Geometry</code> to a WKT representation.
     * <p>This method uses the default WKT dialect (Postgis v1.5 EWKT)</p>
     *
     * @param geometry the <code>Geometry</code> to encode
     * @return the WKT representation of the given geometry
     */
    public static String toWkt(Geometry<?> geometry) {
        WktEncoder encoder = newEncoder();
        return encoder.encode(geometry);
    }

    /**
     * Creates a <code>WktDecoder</code> for the specified WKT <code>Dialect</code>.
     *
     * @param dialect the WKT dialect
     * @return an <code>WktDecoder</code> that supports the specified dialect
     */
    public static WktDecoder newDecoder(Dialect dialect) {
        Class<? extends WktDecoder> decoderClass = DECODERS.get(dialect);
        assert (decoderClass != null) : "A variant declared, but no encoder/decoder registered.";
        return createInstance(decoderClass);
    }

    /**
    * Creates a <code>WktDecoder</code> for the default dialect (Postgis 1.x EWKT).
     * @return an <code>WktDecoder</code> that supports the default dialect
     * @return
     */
    public static WktDecoder newDecoder() {
        return newDecoder(DEFAULT_DIALECT);
    }

    /**
     * Creates a <code>WktEncoder</code> for the specified WKT <code>Dialect</code>.
     *
     * @param dialect the WKT dialect
     * @return an <code>WktEncoder</code> that supports the specified dialect
     */
    public static WktEncoder newEncoder(Dialect dialect) {
        Class<? extends WktEncoder> decoderClass = ENCODERS.get(dialect);
        assert (decoderClass != null) : "A variant declared, but no encoder/decoder registered.";
        return createInstance(decoderClass);
    }

    /**
     * Creates a <code>WktEncoder</code> for the default dialect (Postgis 1.x EWKT).
     * @return an <code>WktEncoder</code> that supports the default dialect
     */
    public static WktEncoder newEncoder() {
        return newEncoder(DEFAULT_DIALECT);
    }

    private static <T> T createInstance(Class<? extends T> codecClass) {
        if (codecClass == null) {
            throw new IllegalArgumentException("Null WKT codec class is not allowed.");
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
