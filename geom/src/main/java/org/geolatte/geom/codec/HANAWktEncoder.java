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
 * Copyright (C) 2010 - 2017 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */
package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CrsId;

/**
 * The HANA EWKT representation differs from the Postgis EWKT representation in that HANA always requires an SRID to be
 * written, even if its not specified or 0. Also, HANA uses a slightly different set of keywords.
 * 
 * @author Jonathan Bregler, SAP
 */
class HANAWktEncoder extends PostgisWktEncoder {

	private final static HANAWktVariant WKT_WORDS = new HANAWktVariant();

	@Override
	public <P extends Position> String encode(Geometry<P> geometry) {
		String wkt = super.encode( geometry );
		if ( wkt == null ) {
			return null;
		}

		if ( !wkt.startsWith( "SRID=" ) ) {
			StringBuilder sb = new StringBuilder( wkt.length() + 16 );
			sb.append( "SRID=" );
			// Write the SRID, the HANA default SRID is 0
			if ( geometry.getSRID() == CrsId.UNDEFINED.getCode() ) {
				sb.append( 0 );
			}
			else {
				sb.append( geometry.getSRID() );
			}
			sb.append( ";" );
			sb.append( wkt );
			wkt = sb.toString();
		}
		return wkt;
	}

	@Override
	protected PostgisWktVariant getWktWords() {
		return WKT_WORDS;
	}

}
