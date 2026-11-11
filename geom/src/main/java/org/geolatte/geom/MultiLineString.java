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

package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A {@code GeometryCollection} of {@code LineString}s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/8/11
 */
public class MultiLineString<P extends Position> extends AbstractGeometryCollection<P, LineString<P>> implements Linear {


    /**
     * Constructs a <code>MultiLineString</code> from the specified <code>LineString</code>s
     *
     * @param lineStrings the element <code>LineString</code>s for the constructed <code>MultiLineString</code>
     */
//    @SafeVarargs
    public MultiLineString(LineString<P>... lineStrings) {
        super(lineStrings);
    }

    public MultiLineString(CoordinateReferenceSystem<P> crs) {
        super(crs);
    }



    @Override
    public P getStartPosition() {
        return getPositionN(0);
    }

    @Override
    public P getEndPosition() {
        return getPositionN(getNumPositions() - 1);
    }

    /**
     * Checks whether this <code>MultiLineString</code> is closed, i.e. whether all of its elements are closed.
     *
     * <p>As specified for <code>ST_IsClosed</code> on <code>ST_MultiCurve</code> in ISO/IEC 13249-3 (SQL/MM Spatial),
     * Z and M coordinate values are ignored, and an empty <code>MultiLineString</code> is not closed.</p>
     *
     * @return true if this <code>MultiLineString</code> is non-empty and all of its elements are closed.
     */
    public boolean isClosed() {
        if (isEmpty()) return false;
        for (LineString<P> element : this) {
            if (!element.isClosed()) return false;
        }
        return true;
    }

    /**
     * Checks whether this <code>MultiLineString</code> is closed, taking Z coordinate values into account.
     *
     * <p>As specified for <code>ST_3DIsClosed</code> on <code>ST_MultiCurve</code> in ISO/IEC 13249-3 (SQL/MM
     * Spatial), all elements must be closed in 3D (see {@link LineString#is3DClosed()}). M coordinate values are
     * ignored, and an empty <code>MultiLineString</code> is not closed.</p>
     *
     * @return true if this <code>MultiLineString</code> is non-empty and all of its elements are closed in 3D.
     */
    public boolean is3DClosed() {
        if (isEmpty()) return false;
        for (LineString<P> element : this) {
            if (!element.is3DClosed()) return false;
        }
        return true;
    }

    @Override
    public int getDimension() {
        return isEmpty() ? -1 : 1;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTILINESTRING;
    }

    @Override
    public Class<? extends Geometry> getComponentType() {
        return LineString.class;
    }

    @SuppressWarnings("unchecked")
    public <Q extends Position> MultiLineString<Q> as(Class<Q> castToType){
        checkCast(castToType);
        return (MultiLineString<Q>)this;
    }

}
