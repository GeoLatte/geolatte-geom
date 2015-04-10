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

/**
 * Abstract base class for classes the are identified by a <code>CrsId</code> (e.g. by EPSG-code).
 * <p/>
 * <h3>Warning: identity of <code>CrsIdentifiable</code>s:</h3>
 * <p>In theory the <code>CrsId</code> should identify uniquely the CoordinateReferenceSystem-related objects
 * (whether CRS, datum, projection, etc.). In practice this is not always the case. Users, for example, can add their own
 * definitions for som <code>Projection</code>s or <code>Ellipsoid</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/21/11
 */
abstract public class CrsIdentifiable {

    private final CrsId crsId;
    private final String name;

    /**
     * Constructs an instance.
     *
     * @param crsId the code
     * @param name
     */
    protected CrsIdentifiable(CrsId crsId, String name) {
        if (crsId == null || name == null) throw new IllegalArgumentException(("No null values allowed"));
        this.crsId = crsId;
        this.name = name;
    }


    /**
     * Returns the identifier for this <code>CoordinateReferenceSystem</code>.
     *
     * @return
     */
    public CrsId getCrsId() {
        return crsId;
    }

    /**
     * Returns the name of this <code>CoordinateReferenceSystem</code>.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrsIdentifiable that = (CrsIdentifiable) o;

        if (crsId != null ? !crsId.equals(that.crsId) : that.crsId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = crsId != null ? crsId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
