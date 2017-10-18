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

package org.geolatte.geom.codec;

import org.geolatte.geom.GeometryType;

/**
 * The HANA <code>WktGeometryToken</code> for the type of geometry.
 *
 * @author Jonathan Bregler, SAP
 */
class HANAWktGeometryToken extends WktGeometryToken {

	private final boolean is3D;

	public HANAWktGeometryToken(String word, GeometryType tag, boolean measured, boolean is3D) {
		super( word, tag, measured );
		this.is3D = is3D;
	}

	public boolean is3D() {
		return is3D;
	}

	public String toString() {
		return getType() + ( is3D() || isMeasured() ? " " : "" ) + ( is3D() ? "Z" : "" ) + ( isMeasured() ? "M" : "" );
	}
}
