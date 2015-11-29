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

import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.db.Decoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Decodes SQL Server Geometry objects to JTS <code>Geometry</code>s.
 *
 * @author Karel Maesen, Geovise BVBA.
 */
public class Decoders {

	final private static List<AbstractSqlServerDecoder> DECODERS = new ArrayList<AbstractSqlServerDecoder>();

	static {
		//Decoders
		DECODERS.add( new PointSqlServerDecoder() );
		DECODERS.add( new LineStringSqlServerDecoder() );
		DECODERS.add( new PolygonSqlServerDecoder() );
		DECODERS.add( new MultiLineStringSqlServerDecoder(  ) );
		DECODERS.add( new MultiPolygonSqlServerDecoder(  ) );
		DECODERS.add( new MultiPointSqlServerDecoder( ) );
		DECODERS.add( new GeometryCollectionSqlServerDecoder(  ) );
	}


	private static Decoder decoderFor(SqlServerGeometry object) {
		for ( Decoder decoder : DECODERS ) {
			if ( decoder.accepts( object ) ) {
				return decoder;
			}
		}
		throw new IllegalArgumentException( "No decoder for type " + object.openGisType() );
	}

	/**
	 * Decodes the SQL Server Geometry object to its JTS Geometry instance
	 *
	 * @param raw
	 *
	 * @return
	 */
	public static Geometry decode(byte[] raw) {
		SqlServerGeometry sqlServerGeom = SqlServerGeometry.deserialize( raw );
		Decoder decoder = decoderFor( sqlServerGeom );
		return decoder.decode( sqlServerGeom );
	}

	/**
	 * Returns the decoder capable of decoding an object of the specified OpenGisType
	 *
	 * @param type OpenGisType for which a decoder is returned
	 *
	 * @return
	 */
	public static Decoder decoderFor(OpenGisType type) {
		for ( AbstractSqlServerDecoder decoder : DECODERS ) {
			if ( decoder.accepts( type ) ) {
				return decoder;
			}
		}
		throw new IllegalArgumentException( "No decoder for type " + type );
	}

}
