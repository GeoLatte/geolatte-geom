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

package org.geolatte.geom;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 7/16/14
 */
public class PositionTypeDescriptor<P extends Position> {

    final private Class<P> positionClass;

    final private int coordinateDimension;

    final private int verticalComponentIndex;

    final private int measureComponentIndex;

    public PositionTypeDescriptor(Class<P> positionClass, int coordinateDimension,
                                  int verticalComponentIndex, int measureComponentIndex) {
        this.positionClass = positionClass;
        this.coordinateDimension = coordinateDimension;
        this.verticalComponentIndex = verticalComponentIndex;
        this.measureComponentIndex = measureComponentIndex;
    }

    public Class<P> getPositionClass() {
        return positionClass;
    }

    public int getCoordinateDimension() {
        return coordinateDimension;
    }

    public int getVerticalComponentIndex() {
        return verticalComponentIndex;
    }

    public int getMeasureComponentIndex() {
        return measureComponentIndex;
    }

    public boolean hasMeasureComponent() {
        return getMeasureComponentIndex() > -1;
    }

    public boolean hasVerticalComponent(){
        return getVerticalComponentIndex() > -1;
    }
}
