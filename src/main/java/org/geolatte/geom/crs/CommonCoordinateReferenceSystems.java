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
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/31/14
 */
public class CommonCoordinateReferenceSystems {

    public static CoordinateReferenceSystem<P2D> mkGenericProjected(LengthUnit unit) {
        return new CoordinateReferenceSystem<P2D>(CrsId.UNDEFINED, "Generic 2D Projected", P2D.class,
                new CoordinateSystemAxis("X", CoordinateSystemAxisDirection.EAST, unit),
                new CoordinateSystemAxis("Y", CoordinateSystemAxisDirection.NORTH, unit)
        );
    }

    public static CoordinateReferenceSystem<G2D> mkGenericGeographic(LengthUnit unit) {
        return new CoordinateReferenceSystem<G2D>(CrsId.UNDEFINED, "Generic 2D Projected", G2D.class,
                new CoordinateSystemAxis("Lat", CoordinateSystemAxisDirection.NORTH, unit),
                new CoordinateSystemAxis("Lon", CoordinateSystemAxisDirection.EAST, unit)
        );
    }

    final public static CoordinateReferenceSystem<P2D> PROJECTED_2D_METER = CommonCoordinateReferenceSystems.mkGenericProjected(LengthUnit.METER);
    final public static CoordinateReferenceSystem<P2DM> PROJECTED_2DM_METER = PROJECTED_2D_METER.addMeasureAxis(LengthUnit.METER);
    final public static CoordinateReferenceSystem<P3D> PROJECTED_3D_METER = PROJECTED_2D_METER.addVerticalAxis(LengthUnit.METER);
    final public static CoordinateReferenceSystem<P3DM> PROJECTED_3DM_METER = PROJECTED_3D_METER.addMeasureAxis(LengthUnit.METER);

    public static CoordinateReferenceSystem<G2D> WGS84 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);

}
