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

	final private static List<Encoder> ENCODERS = new ArrayList<>();


	static {
		//Encoders
		ENCODERS.add( new PointEncoder() );
		ENCODERS.add( new LineStringEncoder() );
		ENCODERS.add( new PolygonEncoder() );
		ENCODERS.add( new GeometryCollectionEncoder(OpenGisType.MULTIPOINT) );
		ENCODERS.add( new GeometryCollectionEncoder( OpenGisType.MULTILINESTRING ) );
		ENCODERS.add( new GeometryCollectionEncoder( OpenGisType.MULTIPOLYGON ) );
		ENCODERS.add( new GeometryCollectionEncoder( OpenGisType.GEOMETRYCOLLECTION ) );

	}

	public static Encoder encoderFor(Geometry<?> geom) {
		for ( Encoder encoder : ENCODERS ) {
			if ( encoder.accepts( geom ) ) {
				return encoder;
			}
		}
		throw new IllegalArgumentException( "No encoder for type " + geom.getGeometryType() );
	}

	public static byte[] encode(Geometry<?> geom) {
		Encoder encoder = encoderFor( geom );
		SqlServerGeometry sqlServerGeometry = encoder.encode( geom );
		return SqlServerGeometry.serialize( sqlServerGeometry );
	}

}
