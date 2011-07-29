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

import org.geolatte.geom.CoordinateAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A mapping between the coordinate accessor methods in <code>PointSequence</code>s and <code>CoordinateSystemAxis</code>
 *
 * <p>An instance of a <code>AccessortoAxisMap</code> should be associated with no more than one <code>Coordinatesystem</code>.
 *
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/30/11
 */
public class AccessorToAxisMap {

    private final static int X_ACCESSOR_INDEX =0;
    private final static int Y_ACCESSOR_INDEX =1;
    private final static int Z_ACCESSOR_INDEX =2;
    private final static int M_ACCESSOR_INDEX =3;
    private final static int LAT_ACCESSOR_INDEX =4;
    private final static int LON_ACCESSOR_INDEX =5;



    private final Map<CoordinateAccessor, CoordinateSystemAxis[]> map;

    private int[] indexes = new int[]{-1,-1,-1,-1,-1,-1};


    /**
     * Creates the default mapping which maps getX() to the X-, Latitude- or Easting-axis.
     *
     * @return
     */
    public static AccessorToAxisMap createDefault(){
        return new XLatitudeEastingAccessorMapping();
    }

    public AccessorToAxisMap(Map<CoordinateAccessor, CoordinateSystemAxis[]> map) {
        this.map = new HashMap<CoordinateAccessor, CoordinateSystemAxis[]>(map);
        checkConsistency();
    }

    /**
     * Checks the consistency of the mapping between accessors and axes.
     *
     * TODO -- implement this function
     */
    protected void checkConsistency() {
    }

    public CoordinateSystemAxis[] getCoordinateSystemAxis(CoordinateAccessor accessor) {
        CoordinateSystemAxis[] coordinateSystemAxis = this.map.get(accessor);
        if (coordinateSystemAxis != null) return coordinateSystemAxis;
        throw new NoSuchElementException("Accessor not defined in this AxisToAccesssorMap.");
    }

       public int getIndex(CoordinateAccessor accessor){
        switch (accessor){
            case X:
                return indexes[X_ACCESSOR_INDEX];
            case Y:
                return indexes[Y_ACCESSOR_INDEX];
            case Z:
                return indexes[Z_ACCESSOR_INDEX];
            case M:
                return indexes[M_ACCESSOR_INDEX];
            case Lat:
                return indexes[LAT_ACCESSOR_INDEX];
            case Lon:
                return indexes[LON_ACCESSOR_INDEX];
        }
        return -1;
    }

    /**
     * Determines the coordinate for the specified accessor.
     *
     * @return the index for the coordinate that will be used by the coordinate accessor method indicated by the accessor parameter, or -1 if the coordinate is not present
     */
    protected int initialize(CoordinateSystemAxis[] axes) {
        for (CoordinateAccessor accessor : map.keySet()) {
            setAccessorIndex(accessor, axes);
        }
        return -1;

    }

    private void setAccessorIndex(CoordinateAccessor accessor, CoordinateSystemAxis[] axes) {
        for (int i = 0; i < axes.length; i++) {
            for (CoordinateSystemAxis candidate : map.get(accessor)){
                if (axes[i] == candidate) {
                    setIndex(accessor, i);
                }
            }
        }
    }

    private void setIndex(CoordinateAccessor accessor, int index) {
        switch (accessor){
            case X:
                indexes[X_ACCESSOR_INDEX] = index;
                return;
            case Y:
                indexes[Y_ACCESSOR_INDEX] = index;
                return;
            case Z:
                indexes[Z_ACCESSOR_INDEX] = index;
                return;
            case M:
                indexes[M_ACCESSOR_INDEX] = index;
                return;
            case Lat:
                indexes[LAT_ACCESSOR_INDEX] = index;
                return;
            case Lon:
                indexes[LON_ACCESSOR_INDEX] = index;
                return;
        }
    }


}
