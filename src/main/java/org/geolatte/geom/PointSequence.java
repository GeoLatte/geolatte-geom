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
 * A <code>PointCollection</code> that consists of a sequence of <code>Point</code>s.
 *
 * <p>A <code>PointSequence</code> is typically used to store the <code>Point</code>s (vertices) that define a
 * curve (a 1-dimensional geometric primitive), with the subtype of the curve specifying the form of interpolation between
 * consecutive <code>Point</code>s. (E.g.a <code>LineString</code> uses linear interpolation between <code>Point</code>s.)</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public interface PointSequence extends PointCollection, Iterable<Point> {

    PointSequence clone();


}
