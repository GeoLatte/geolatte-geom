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
 * Copyright (C) 2010 - 2013 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.codec.sqlserver;


import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

abstract class AbstractDecoder implements Decoder {

	public <P extends Position> Geometry<P> decode(SqlServerGeometry<P> nativeGeom) {
		if ( !accepts( nativeGeom ) ) {
			throw new IllegalArgumentException( getClass().getSimpleName() + " received object of type " + nativeGeom.openGisType() );
		}
		if ( nativeGeom.isEmpty() ) {
			Geometry<P> nullGeom = createNullGeometry(nativeGeom.getCoordinateReferenceSystem());
			return nullGeom;
		}
		return createGeometry( nativeGeom );
	}

	public boolean accepts(OpenGisType type) {
		return type == getOpenGisType();
	}

	public <P extends Position> boolean accepts(SqlServerGeometry<P> nativeGeom) {
		return accepts( nativeGeom.openGisType() );
	}

	protected abstract OpenGisType getOpenGisType();

	protected abstract <P extends Position> Geometry<P> createNullGeometry(CoordinateReferenceSystem<P> crs);

	protected abstract <P extends Position> Geometry<P> createGeometry(SqlServerGeometry<P> nativeGeom);

	protected abstract <P extends Position> Geometry<P> createGeometry(SqlServerGeometry<P> nativeGeom, int shapeIndex);

}
