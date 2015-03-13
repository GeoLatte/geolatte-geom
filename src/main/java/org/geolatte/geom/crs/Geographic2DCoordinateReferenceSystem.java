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

import org.geolatte.geom.*;

/**
 * A geographic <code>CoordinateReferenceSystem</code>.
 * <p/>
 * <p>A <code>GeographicCoordinateReferenceSystem</code> is defined as a coordinate system based on
 * latitude and longitude. Some geographic coordinate systems are Lat/Lon, and some are Lon/Lat.
 * You can find out which this is by examining the {@link CoordinateSystemAxis CoordinateSystemAxes}.
 * You should also check the angular units, since not all geographic coordinate systems use degrees
 * (see [CTS-1.00], p. 63).</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class Geographic2DCoordinateReferenceSystem extends GeographicCoordinateReferenceSystem<G2D> {

    /**
     * Constructs a <code>CoordinateReferenceSystem</code>.
     *
     * @param crsId the <code>CrsId</code> that identifies this system uniquely
     * @param name  the commonly used name for this system
     * @param crs  the {@link org.geolatte.geom.crs.EllipsoidalCoordinateSystem2D} for this system
     * @throws IllegalArgumentException if less than two {@link CoordinateSystemAxis CoordinateSystemAxes} are passed.
     */
    public Geographic2DCoordinateReferenceSystem(CrsId crsId, String name, EllipsoidalCoordinateSystem2D crs) {
        super(crsId, name, crs);
    }

    @Override
    public EllipsoidalCoordinateSystem2D getCoordinateSystem() {
        return (EllipsoidalCoordinateSystem2D)super.getCoordinateSystem();
    }

}
