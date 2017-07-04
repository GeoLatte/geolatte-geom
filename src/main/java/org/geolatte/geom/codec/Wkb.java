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

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates encoders/decoders for WKB geometry representations.
 *
 * <p>Note that the <coder>WkbEncoder</coder>/<code>WkbDecoder</code> instances returned by the factory
 * methods are not thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 29, 2010
 */
public class Wkb {

    public enum Dialect {
        //the PostGIS EWKB dialect (versions 1.0 to 1.6).
        POSTGIS_EWKB_1,
        MYSQL_WKB,
        HANA_EWKB
    }

    private static final Dialect DEFAULT_DIALECT = Dialect.POSTGIS_EWKB_1;

    private static final Map<Dialect, Class<? extends WkbDecoder>> DECODERS = new HashMap<Dialect, Class<? extends WkbDecoder>>();
    private static final Map<Dialect, Class<? extends WkbEncoder>> ENCODERS = new HashMap<Dialect, Class<? extends WkbEncoder>>();

    static {
        DECODERS.put(Dialect.POSTGIS_EWKB_1, PostgisWkbDecoder.class);
        DECODERS.put(Dialect.MYSQL_WKB, MySqlWkbDecoder.class);
        DECODERS.put(Dialect.HANA_EWKB, HANAWkbDecoder.class);
        ENCODERS.put(Dialect.POSTGIS_EWKB_1, PostgisWkbEncoder.class);
        ENCODERS.put(Dialect.MYSQL_WKB, MySqlWkbEncoder.class);
        ENCODERS.put(Dialect.HANA_EWKB, HANAWkbEncoder.class);
    }


    /**
     * Encodes a <code>Geometry</code> into a WKB representation using the NDR (little-endian)  byte-order.
     *
     * <p>This methods uses the default WKB dialect (Postgis v1.5 EWKB ).</p>
     *
     * @param geometry The <code>Geometry</code> to be encoded as WKB.
     * @return A buffer of bytes that contains the WKB-encoded <code>Geometry</code>.
     */
    public static ByteBuffer toWkb(Geometry geometry) {
        return toWkb(geometry, ByteOrder.NDR);
    }

    /**
     * Encodes a <code>Geometry</code> into a WKB representation using the specified byte-order.
     * <p>This methods uses the default WKB dialect (Postgis v1.5 EWKB ).</p>
     *
     * @param geometry  The <code>Geometry</code> to be encoded as WKB.
     * @param byteOrder The WKB byte order, either {@link ByteOrder#XDR XDR} or {@link ByteOrder#NDR NDR}
     * @return A buffer of bytes that contains the WKB-encoded <code>Geometry</code>.
     */
    public static ByteBuffer toWkb(Geometry<?> geometry, ByteOrder byteOrder) {
        WkbEncoder encoder = newEncoder(DEFAULT_DIALECT);
        return encoder.encode(geometry, byteOrder);
    }

    /**
     * Decodes a WKB representation in a <code>ByteBuffer</code> to a <code>Geometry</code>.
     * <p>This methods uses the default WKB dialect (Postgis v1.5 EWKB ).</p>
     *
     * @param byteBuffer A buffer of bytes that contains a WKB-encoded <code>Geometry</code>.
     * @return The <code>Geometry</code> that is encoded in the WKB.
     */
    public static Geometry<?> fromWkb(ByteBuffer byteBuffer) {
        WkbDecoder decoder = newDecoder(DEFAULT_DIALECT);
        return decoder.decode(byteBuffer);
    }

    /**
     * Creates a <code>WkbDecoder</code> for the specified WKB <code>Dialect</code>.
     *
     * @param dialect the WKB dialect
     * @return an <code>WkbDecoder</code> that supports the specified dialect
     */
    public static WkbDecoder newDecoder(Dialect dialect) {
        Class<? extends WkbDecoder> decoderClass = DECODERS.get(dialect);
        assert (decoderClass != null) : "A variant declared, but no encoder/decoder registered.";
        return createInstance(decoderClass);
    }

    /**
     * Creates a <code>WkbDecoder</code> for the default WKB <code>Dialect</code>.
     *
     * @return an <code>WkbDecoder</code> that supports the specified dialect
     */
    public static WkbDecoder newDecoder() {
        Class<? extends WkbDecoder> decoderClass = DECODERS.get(DEFAULT_DIALECT);
        assert (decoderClass != null) : "A variant declared, but no encoder/decoder registered.";
        return createInstance(decoderClass);
    }


    /**
     * Creates a <code>WkbEncoder</code> for the specified WKB <code>Dialect</code>.
     *
     * @param dialect the WKB dialect
     * @return an <code>WkbEncoder</code> that supports the specified dialect
     */
    public static WkbEncoder newEncoder(Dialect dialect) {
        Class<? extends WkbEncoder> decoderClass = ENCODERS.get(dialect);
        assert (decoderClass != null) : "A variant declared, but no encoder/decoder registered.";
        return createInstance(decoderClass);
    }

    /**
     * Creates a <code>WkbEncoder</code> for the default WKB <code>Dialect</code>.
     *
     * @return an <code>WkbEncoder</code> that supports the specified dialect
     */
    public static WkbEncoder newEncoder() {
        Class<? extends WkbEncoder> decoderClass = ENCODERS.get(DEFAULT_DIALECT);
        assert (decoderClass != null) : "A variant declared, but no encoder/decoder registered.";
        return createInstance(decoderClass);
    }

    private static <T> T createInstance(Class<? extends T> codecClass) {
        if (codecClass == null) {
            throw new IllegalArgumentException("Null WKB codec class argument not allowed.");
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
