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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A Coordinate Reference System.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public abstract class CoordinateReferenceSystem<P extends Position> extends CrsIdentifiable {

    private final CoordinateSystem<P> coordinateSystem;

    /**
     * Constructs a <code>CoordinateReferenceSystem</code>.
     *
     * @param crsId the {@link CrsId} that identifies this <code>CoordinateReferenceSystem</code> uniquely
     * @param name  the commonly used name for this <code>CoordinateReferenceSystem</code>
     * @param coordinateSystem the coordinate system to use
     * @throws IllegalArgumentException if less than two {@link CoordinateSystemAxis CoordinateSystemAxes} are passed.
     */
    public CoordinateReferenceSystem(CrsId crsId, String name, CoordinateSystem<P> coordinateSystem) {
        super(crsId, name);
        if (coordinateSystem == null)
            throw new IllegalArgumentException("No null arguments allowed");
        this.coordinateSystem = coordinateSystem;
    }


    /**
     * Returns the type token for the type of <code>Position</code>s referenced in this system.
     *
     * @return a Class object
     */
    public Class<P> getPositionClass() {
        return this.coordinateSystem.getPositionClass();
    }


    /**
     * Returns the <code>CoordinateSystem</code> associated with this <code>CoordinateReferenceSystem</code>.
     *
     * @return Returns the <code>CoordinateSystem</code> associated with this <code>CoordinateReferenceSystem</code>.
     */
    public CoordinateSystem<P> getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Returns the coordinate dimension, i.e. the number of axes in this coordinate reference system.
     *
     * @return the coordinate dimension
     */
    public int getCoordinateDimension() {
        return getCoordinateSystem().getCoordinateDimension();
    }

    /**
     * Return the {@link CoordinateSystemAxis CoordinateSystemAxes} associated with this <code>CoordinateReferenceSystem</code>.
     *
     * @return an array of {@link CoordinateSystemAxis CoordinateSystemAxes}.
     */
    public CoordinateSystemAxis getAxis(int idx) {
        return coordinateSystem.getAxis(idx);
    }

    /**
     * Returns the index of the specified axis in this {@code CoordinateReferenceSystem}, or
     * -1 if it is not an axis of this system.
     *
     * @param axis the axis to look up
     * @return the index of the specified axis in this {@code CoordinateReferenceSystem}
     */
    public int getAxisIndex(CoordinateSystemAxis axis) {
        return getCoordinateSystem().getAxisIndex(axis);
    }


    public boolean isCompound() {
        return false;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CoordinateReferenceSystem that = (CoordinateReferenceSystem) o;

        if (!coordinateSystem.equals(that.coordinateSystem)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + coordinateSystem.hashCode();
        return result;
    }
}
