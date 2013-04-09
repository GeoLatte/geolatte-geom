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

import org.geolatte.geom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializes a JTS <code>Geometry</code> to a byte-array.
 *
 * @author Karel Maesen, Geovise BVBA.
 */
public class Encoders {

	final private static List<Encoder<? extends Geometry>> ENCODERS = new ArrayList<Encoder<? extends Geometry>>();


	static {
		//Encoders
		ENCODERS.add( new PointEncoder() );
		ENCODERS.add( new LineStringEncoder() );
		ENCODERS.add( new PolygonEncoder() );
		ENCODERS.add( new GeometryCollectionEncoder<MultiPoint>(OpenGisType.MULTIPOINT) );
		ENCODERS.add( new GeometryCollectionEncoder<MultiLineString>( OpenGisType.MULTILINESTRING ) );
		ENCODERS.add( new GeometryCollectionEncoder<MultiPolygon>( OpenGisType.MULTIPOLYGON ) );
		ENCODERS.add( new GeometryCollectionEncoder<GeometryCollection>( OpenGisType.GEOMETRYCOLLECTION ) );

	}

	public static Encoder<? extends Geometry> encoderFor(Geometry geom) {
		for ( Encoder<? extends Geometry> encoder : ENCODERS ) {
			if ( encoder.accepts( geom ) ) {
				return encoder;
			}
		}
		throw new IllegalArgumentException( "No encoder for type " + geom.getGeometryType() );
	}

	public static <T extends Geometry> byte[] encode(T geom) {
		Encoder<T> encoder = (Encoder<T>) encoderFor( geom );
		SqlServerGeometry sqlServerGeometry = encoder.encode( geom );
		return SqlServerGeometry.serialize( sqlServerGeometry );
	}

}
