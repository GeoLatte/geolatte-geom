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
 * An ellipsoid.
 *
 * <p>An ellipsoid is an approximation of the Earth's surface as a squashed sphere. A common
 * synonym is "spheroid". (See [CTS-1.00], p. 22-23.) </p>
 *
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class Ellipsoid extends CrsIdentifiable {

    private final double semiMajorAxis;
    private final double inverseFlattening;

    /** constructs an instance
     *
     * @param crsId the identifier for this <code>Ellipsoid</code>
     * @param name the commonly used name for this <code>Ellipsoid</code>
     * @param semiMajorAxis the semi-major axis
     * @param inverseFlattening
     */
    public Ellipsoid(CrsId crsId, String name, double semiMajorAxis, double inverseFlattening) {
        super(crsId, name);
        this.semiMajorAxis = semiMajorAxis;
        this.inverseFlattening = inverseFlattening;
    }

    /**
     * Returns the semi-major axis of this <code>Ellipsoid.</code>
     *
     * <p> The semi-major axis is the equatorial radius in meters. [CTS-1.00], p. 61</p>
     *
     * @return the semi-major axis.
     */
    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    /**
     * Returns the inverse flattening.
     *
     * <p>The inverse flattening is related to the equatorial/polar radius by the formula ivf=re/(re-rp). For perfect spheres,
     * this formula breaks down, and a special IVF value of zero is used. [CTS-1.00], p.61</p>
     *
     * @return
     */
    public double getInverseFlattening() {
        return inverseFlattening;
    }

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
