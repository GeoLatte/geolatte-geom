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

import org.geolatte.geom.C3D;

/**
 * A geo-centric <code>CoordinateReferenceSystem</code>.
 *
 * <p>A geo-centric coordinate system is defined as "a 3D coordinate system, with its origin at the centre of the Earth. The X axis points towards the
 * prime meridian. The Y axis points East or West. The Z axis points North or South. By default the
 * Z axis will point North, and the Y axis will point East (e.g. a right handed system), but you should
 * check the axes for non-default values" (see [CTS-1.00], p. 62.). </p>
 *
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/5/11
 */
public class GeocentricCartesianCoordinateReferenceSystem extends SingleCoordinateReferenceSystem<C3D>  {

    private final Datum datum;
    private final PrimeMeridian primePeridian;

    /**
     * Constructs a <code>CoordinateReferenceSystem</code>.
     *
     * @param crsId the <code>CrsId</code> that identifies this <code>CoordinateReferenceSystem</code> uniquely
     * @param name  the commonly used name for this <code>CoordinateReferenceSystem</code>
     * @param crs  the 3D-coordinate system for this
     * @throws IllegalArgumentException if less than two {@link CoordinateSystemAxis CoordinateSystemAxes} are passed.
     */
    public GeocentricCartesianCoordinateReferenceSystem(CrsId crsId, String name, Datum datum, PrimeMeridian primeMeridian, CartesianCoordinateSystem3D crs) {
        super(crsId, name, crs);
        this.primePeridian = primeMeridian;
        this.datum = datum;
    }

    public Datum getDatum() {
        return this.datum;
    }

    public PrimeMeridian getPrimeMeridian(){
        return this.primePeridian;
    }

}
