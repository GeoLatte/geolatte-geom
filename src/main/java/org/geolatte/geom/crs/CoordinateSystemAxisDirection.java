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
 * Enumerates the possible values of direction for a <code>CoordinateSystemAxis</code>.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/29/11
 */
public enum CoordinateSystemAxisDirection {

    //TODO add enum value MEASURE

    NORTH(Type.BASE),
    EAST(Type.BASE),
    SOUTH(Type.BASE),
    WEST(Type.BASE),
    UP(Type.VERTICAL),
    DOWN(Type.VERTICAL),
    OTHER(Type.MEASURE),
    UNKNOWN(Type.MEASURE),
    GeocentricX(Type.BASE),
    GeocentricY(Type.BASE),
    GeocentricZ(Type.BASE);

    public  static enum Type {
        VERTICAL,
        MEASURE,
        BASE        //axis of a project plane, or geodetic
    }

    public final Type type;

    private CoordinateSystemAxisDirection(Type type) {
        this.type = type;
    }



    public boolean isMeasureAxisDirection() {
        return type == Type.MEASURE;
    }

    public boolean isVerticalAxisDirection() {
        return type == Type.VERTICAL;
    }


}

