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
 * A Coordinate Reference System.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public abstract class CoordinateReferenceSystem extends CrsIdentifiable {

    private final CoordinateSystem coordinateSystem;

    /**
     * Constructs a <code>CoordinateReferenceSystem</code>.
     *
     * @param crsId the {@link CrsId} that identifies this <code>CoordinateReferenceSystem</code> uniquely
     * @param name the commonly used name for this <code>CoordinateReferenceSystem</code>
     * @param axes the {@link CoordinateSystemAxis CoordinateSystemAxes} for this <code>CoordinateReferenceSystem</code>
     * @throws IllegalArgumentException if less than two {@link CoordinateSystemAxis CoordinateSystemAxes} are passed.
     */
    public CoordinateReferenceSystem(CrsId crsId, String name, CoordinateSystemAxis... axes) {
        super(crsId, name);
        this.coordinateSystem = new CoordinateSystem(axes);
    }


    /**
     * Returns the <code>CoordinateSystem</code> associated with this <code>CoordinateReferenceSystem</code>.
     *
     * @return
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Return the {@link CoordinateSystemAxis CoordinateSystemAxes} associated with this <code>CoordinateRefereeceSystem</code>.
     *
     * @return an array of {@link CoordinateSystemAxis CoordinateSystemAxes}.
     *
     */
    public CoordinateSystemAxis[] getAxes(){
        return coordinateSystem.getAxes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CoordinateReferenceSystem that = (CoordinateReferenceSystem) o;

        return !(coordinateSystem != null ? !coordinateSystem.equals(that.coordinateSystem) : that.coordinateSystem != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (coordinateSystem != null ? coordinateSystem.hashCode() : 0);
        return result;
    }
}
