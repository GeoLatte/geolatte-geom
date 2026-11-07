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
 * Copyright (C) 2010 - 2013 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.codec.db.sqlserver;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.db.Encoder;
import org.geolatte.geom.jts.JTS;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializes a JTS <code>Geometry</code> to a byte-array.
 *
 * @author Karel Maesen, Geovise BVBA.
 */
public class Encoders {

    final private static List<AbstractSqlServerEncoder> ENCODERS = new ArrayList<AbstractSqlServerEncoder>();


    static {
        //Encoders
        ENCODERS.add(new PointSqlServerEncoder());
        ENCODERS.add(new LineStringSqlServerEncoder());
        ENCODERS.add(new PolygonSqlServerEncoder());
        ENCODERS.add(new GeometryCollectionSqlServerEncoder(OpenGisType.MULTIPOINT));
        ENCODERS.add(new GeometryCollectionSqlServerEncoder(OpenGisType.MULTILINESTRING));
        ENCODERS.add(new GeometryCollectionSqlServerEncoder(OpenGisType.MULTIPOLYGON));
        ENCODERS.add(new GeometryCollectionSqlServerEncoder(OpenGisType.GEOMETRYCOLLECTION));

    }

    public static Encoder<SqlServerGeometry> encoderFor(Geometry<?> geom) {
        for (Encoder<SqlServerGeometry> encoder : ENCODERS) {
            if (encoder.accepts(geom)) {
                return encoder;
            }
        }
        throw new IllegalArgumentException("No encoder for type " + geom.getGeometryType());
    }

    /**
     * Serializes the geometry, marking it as valid without checking its validity.
     *
     * @param geom the geometry to serialize
     * @return the SQL Server serialization of the geometry
     * @see #encode(Geometry, boolean)
     */
    public static byte[] encode(Geometry<?> geom) {
        return encode(geom, false);
    }

    /**
     * Serializes the geometry.
     *
     * <p>SQL Server marks invalid geometries in their serialization. When <code>checkValidity</code> is true, the
     * geometry is only marked as valid if it is valid according to JTS. When false, the geometry is always marked
     * as valid, so the caller must ensure it is.
     *
     * @param geom          the geometry to serialize
     * @param checkValidity whether to check the validity of the geometry
     * @return the SQL Server serialization of the geometry
     */
    public static byte[] encode(Geometry<?> geom, boolean checkValidity) {
        Encoder<SqlServerGeometry> encoder = encoderFor(geom);
        SqlServerGeometry sqlServerGeometry = encoder.encode(geom);
        if (checkValidity && !JTS.to(geom).isValid()) {
            sqlServerGeometry.clearIsValid();
        }
        return SqlServerGeometry.serialize(sqlServerGeometry);
    }

}
