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
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class Ellipsoid {

    private final int SRID;
    private final String name;
    private final double semiMajorAxis;
    private final double inverseFlattening;

    public Ellipsoid(int SRID, String name, double semiMajorAxis, double inverseFlattening) {
        this.SRID = SRID;
        this.name = name;
        this.semiMajorAxis = semiMajorAxis;
        this.inverseFlattening = inverseFlattening;
    }

    public int getSRID() {
        return SRID;
    }

    public String getName() {
        return name;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public double getInverseFlattening() {
        return inverseFlattening;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ellipsoid ellipsoid = (Ellipsoid) o;

        if (SRID != ellipsoid.SRID) return false;
        if (Double.compare(ellipsoid.inverseFlattening, inverseFlattening) != 0) return false;
        if (Double.compare(ellipsoid.semiMajorAxis, semiMajorAxis) != 0) return false;
        if (name != null ? !name.equals(ellipsoid.name) : ellipsoid.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = SRID;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = semiMajorAxis != +0.0d ? Double.doubleToLongBits(semiMajorAxis) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = inverseFlattening != +0.0d ? Double.doubleToLongBits(inverseFlattening) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
