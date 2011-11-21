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
public class Ellipsoid extends CrsIdentifiable {

    private final double semiMajorAxis;
    private final double inverseFlattening;

    public Ellipsoid(CrsId crsId, String name, double semiMajorAxis, double inverseFlattening) {
        super(crsId, name);
        this.semiMajorAxis = semiMajorAxis;
        this.inverseFlattening = inverseFlattening;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public double getInverseFlattening() {
        return inverseFlattening;
    }

    // shouldn't equals/hashcode look at crsId uniquely??


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Ellipsoid ellipsoid = (Ellipsoid) o;

        if (Double.compare(ellipsoid.inverseFlattening, inverseFlattening) != 0) return false;
        if (Double.compare(ellipsoid.semiMajorAxis, semiMajorAxis) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = semiMajorAxis != +0.0d ? Double.doubleToLongBits(semiMajorAxis) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = inverseFlattening != +0.0d ? Double.doubleToLongBits(inverseFlattening) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
