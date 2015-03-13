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

/**
 * A visitor for <code>Geometry</code>s.
 *
 * @see org.geolatte.geom.Geometry#accept(GeometryVisitor)
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/22/11
 */
public interface GeometryVisitor<P extends Position> {

    /**
     * Visits a <code>Point</code>
     * @param point
     */
    public void visit(Point<P> point);

    /**
     * Visits a <code>LineString</code>.
     *
     * @param lineString
     */
    public void  visit(LineString<P> lineString);

    /**
     * Visits a <code>Polygon</code>.
     *
     * @param polygon
     */
    public void visit(Polygon<P> polygon);

    /**
     * Visits a <code>GeometryCollection</code>.
     *
     * <p>The collection will pass the visitor to it
     * constituent parts. </p>
     *
     * @param collection
     */
    public <G extends Geometry<P>> void visit(GeometryCollection<P,G> collection);

}
