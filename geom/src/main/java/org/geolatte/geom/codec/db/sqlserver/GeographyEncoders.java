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

/**
 * Serializes a <code>Geometry</code> to a SQL Server GEOGRAPHY value.
 *
 * <p>A GEOGRAPHY value has the same serialization as a GEOMETRY value, except that it stores its points as
 * (latitude, longitude) pairs. Use {@link Encoders} for GEOMETRY values.
 *
 * <p>SQL Server determines the interior of a polygon from the orientation of its rings: exterior rings must be
 * counter-clockwise and interior rings clockwise. The rings are serialized as given, so the caller must ensure
 * this orientation.
 */
public class GeographyEncoders {

    /**
     * The SRID used for geometries without SRID.
     */
    public static final int DEFAULT_SRID = 4326;

    static final int MIN_SRID = 4120;
    static final int MAX_SRID = 4999;

    /**
     * Serializes the geometry as a GEOGRAPHY value.
     *
     * <p>A geometry without SRID is serialized with SRID {@value #DEFAULT_SRID}. As SQL Server does for version 1 of
     * the GEOGRAPHY serialization, the value is always marked as valid.
     *
     * @param geom the geometry to serialize
     * @return the SQL Server serialization of the geometry
     * @throws IllegalArgumentException if the SRID of the geometry is not in the range 4120 - 4999
     */
    public static byte[] encode(Geometry<?> geom) {
        int srid = geom.getSRID() <= 0 ? DEFAULT_SRID : geom.getSRID();
        if (srid < MIN_SRID || srid > MAX_SRID) {
            throw new IllegalArgumentException(String.format(
                    "SRID %d not allowed for SQL Server geography; it must be in the range %d - %d.",
                    srid, MIN_SRID, MAX_SRID));
        }
        SqlServerGeometry sqlServerGeometry = Encoders.encoderFor(geom).encode(geom);
        sqlServerGeometry.setSrid(srid);
        return SqlServerGeometry.serialize(sqlServerGeometry, true);
    }

}
