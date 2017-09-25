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

package org.geolatte.geom.codec.db.sqlserver;


import org.geolatte.geom.*;

/**
 * The type of geometry.
 *
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
public enum OpenGisType {
	POINT( (byte) 1, Point.class ),
	LINESTRING( (byte) 2, LineString.class ),
	POLYGON( (byte) 3, Polygon.class ),
	MULTIPOINT( (byte) 4, MultiPoint.class ),
	MULTILINESTRING( (byte) 5, MultiLineString.class ),
	MULTIPOLYGON( (byte) 6, MultiPolygon.class ),
	GEOMETRYCOLLECTION( (byte) 7, GeometryCollection.class ),
	INVALID_TYPE( (byte) 0, null );

	final byte byteValue;
	final Class<? extends Geometry> geomClass;

	OpenGisType(byte v, Class<? extends Geometry> geomClass) {
		this.byteValue = v;
		this.geomClass = geomClass;
	}

	boolean typeOf(Object o) {
		return geomClass.isAssignableFrom( o.getClass() );
	}

	static OpenGisType valueOf(byte b) {
		for ( OpenGisType t : values() ) {
			if ( t.byteValue == b ) {
				return t;
			}
		}
		return INVALID_TYPE;
	}

}
