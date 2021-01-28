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
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * The HANA EWKT decoder is a variant of the Postgis EWKT decoder. The differences are that it uses a different
 * tokenizer and a different set of keywords.
 * 
 * @author Jonathan Bregler, SAP
 */
class HANAWktDecoder implements WktDecoder {

	@Override
	public <P extends Position> Geometry<P> decode(String wkt, CoordinateReferenceSystem<P> crs) {
		return new HANAWktParser<>(wkt, crs).parse();
	}
}

class HANAWktParser<P extends Position> extends PostgisWktParser<P> {
	private final static HANAWktDialect dialect = new HANAWktDialect();
	public HANAWktParser(String wkt, CoordinateReferenceSystem<P> crs) {
		super(dialect, wkt, crs);
	}
}