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
 * Enumerates the components of the coordinates of a point.
 *
 * <p>The X- and Y-components correspond to the first, resp. second <code>CoordinateSystemAxis</code>
 * of the associated <code>CoordinateSystem</code>. These two components are always present.</p>
 *
 * <p>The Z- and M-components are optional. The Z-component is, if present, always the third component;
 * if present, the M-component is either the third or fourth component.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/30/11
 */
public enum CoordinateComponent {
        X, Y, Z, M
}
