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
 * Copyright (C) 2010 - 2014 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.crs;

import org.geolatte.geom.*;

/**
 * Common coordinate reference systems.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/31/14
 */
public class CommonCoordinateReferenceSystems {

    /**
     * Creates a generic projected coordinate reference system using the specified units of length for coordinates.
     *
     * A generic system is one without a precisely defined Coordinate Reference System
     *
     * @param unit the length unit to use for the planar coordinates.
     * @return a {@code CoordinateReferenceSystem} with the specified length units
     */
    public static CoordinateReferenceSystem<P2D> mkGenericProjected(Unit unit) {
        return new CoordinateReferenceSystem<P2D>(CrsId.UNDEFINED, "Generic 2D Projected", P2D.class,
                new CoordinateSystemAxis("X", CoordinateSystemAxisDirection.EAST, unit),
                new CoordinateSystemAxis("Y", CoordinateSystemAxisDirection.NORTH, unit)
        );
    }

    /**
     * Creates a generic geographic coordinate reference system using the specified units of length for coordinates.
     *
     * A generic system is one without a precisely defined Coordinate Reference System
     *
     * @param unit the length unit to use for the planar coordinates.
     * @return a {@code CoordinateReferenceSystem}
     */
    public static CoordinateReferenceSystem<G2D> mkGenericGeographic(Unit unit) {
        return new CoordinateReferenceSystem<G2D>(CrsId.UNDEFINED, "Generic 2D Projected", G2D.class,
                new CoordinateSystemAxis("Lat", CoordinateSystemAxisDirection.NORTH, unit),
                new CoordinateSystemAxis("Lon", CoordinateSystemAxisDirection.EAST, unit)
        );
    }

    /**
     * A generic projected 2D {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CoordinateReferenceSystem<P2D> PROJECTED_2D_METER = CommonCoordinateReferenceSystems.mkGenericProjected(Unit.METER);

    /**
     * A generic projected 2DM {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CoordinateReferenceSystem<P2DM> PROJECTED_2DM_METER = PROJECTED_2D_METER.addMeasureAxis(Unit.METER);

    /**
     * A generic projected 3D {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CoordinateReferenceSystem<P3D> PROJECTED_3D_METER = PROJECTED_2D_METER.addVerticalAxis(Unit.METER);

    /**
     * A generic projected 3DM {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CoordinateReferenceSystem<P3DM> PROJECTED_3DM_METER = PROJECTED_3D_METER.addMeasureAxis(Unit.METER);

    /**
     * The WGS 84 {@code GeographicCoordinateReferenceSystem}
     */
    public static GeographicCoordinateReferenceSystem WGS84 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);

}
