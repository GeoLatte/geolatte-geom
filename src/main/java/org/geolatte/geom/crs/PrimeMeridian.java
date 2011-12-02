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
 * A prime meridian.
 *
 * <p>A prime meridian is defined by [CTS-1.00] as follows: "[The prime meridian] defines the meridian used to take
 * longitude measurements from.  The units of the longitude must be inferred from the context. If [this object] occurs
 * inside a [geographic coordinate reference system], then the longitude units will match those of
 * the geographic coordinate system. If [it] occurs inside a [geo-centric coordinate reference system], then the units
 * will be in degrees. The longitude value defines the angle of the prime meridian relative to the Greenwich Meridian.
 * A positive value indicates the prime meridian is East of Greenwich, and a negative value indicates
 * the prime meridian is West of Greenwich </p>
 *
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/4/11
 */
public class PrimeMeridian extends CrsIdentifiable {

    private final double longitude;

    /**
     * Constructs an instance
     * @param crsId the identifier for the new prime meridian
     * @param name the common name  for the new prime meridian
     * @param longitude the angle of the new prime meridian relative to Greenwich Meridian
     */
    public PrimeMeridian(CrsId crsId, String name, double longitude) {
        super(crsId, name);
        this.longitude = longitude;
    }

    /**
     * Returns the angle of this meridian relative to Greenwich Meridian.
     *
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PrimeMeridian that = (PrimeMeridian) o;

        if (Double.compare(that.longitude, longitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = longitude != +0.0d ? Double.doubleToLongBits(longitude) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
