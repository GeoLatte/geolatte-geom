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
public abstract class CoordinateReferenceSystem {

    private final int SRID;
    private final String name;
    private final CoordinateSystem coordinateSystem;

    /**
     * Constructs a <code>CoordinateReferenceSystem</code>.
     *
     * @param SRID the SRID that identifies this
     * @param name
     * @param axes
     */
    CoordinateReferenceSystem(int SRID, String name, CoordinateSystemAxis... axes){
        this.SRID = SRID;
        this.name = name;
        this.coordinateSystem = new CoordinateSystem(axes);
    }

    //TODO - replace with CsrId (see GEOM-5)
    public int getSRID() {
        return SRID;
    }

    /**
     * Returns the name of this <code>CoordinateReferenceSystem</code>.
     *
     * @return the name
     */
    public String getName() {
        return name;
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
     * Return the <code>CoordinateSystemAxis</code>es associated with this <code>CoordinateRefereeceSystem</code>.
     *
     * @return an array of <code>CoordinateSystemAxis</code>es.
     *
     */
    public CoordinateSystemAxis[] getAxes(){
        return coordinateSystem.getAxes();
    }
}
