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

import org.geolatte.geom.LineString;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

class LineStringDecoder extends AbstractDecoder {


	@Override
	protected OpenGisType getOpenGisType() {
		return OpenGisType.LINESTRING;
	}

	protected <P extends Position> LineString<P> createNullGeometry(CoordinateReferenceSystem<P> crs) {
		return new LineString<P>(crs);
	}

	protected <P extends Position>  LineString<P> createGeometry(SqlServerGeometry<P> nativeGeom) {
		return createLineString( nativeGeom, new IndexRange( 0, nativeGeom.getNumPoints() ) );
	}

	@Override
	protected <P extends Position>  LineString<P> createGeometry(SqlServerGeometry<P> nativeGeom, int shapeIndex) {
		if ( nativeGeom.isEmptyShape( shapeIndex ) ) {
			return createNullGeometry(nativeGeom.getCoordinateReferenceSystem());
		}
		int figureOffset = nativeGeom.getFiguresForShape( shapeIndex ).start;
		IndexRange pntIndexRange = nativeGeom.getPointsForFigure( figureOffset );
		return createLineString( nativeGeom, pntIndexRange );
	}

	protected <P extends Position>  LineString<P> createLineString(SqlServerGeometry<P> nativeGeom, IndexRange pntIndexRange) {
		PositionSequence<P> coordinates = nativeGeom.coordinateRange( pntIndexRange );
        CoordinateReferenceSystem<P> coordinateReferenceSystem = nativeGeom.getCoordinateReferenceSystem();
        return new LineString<P>(coordinates, coordinateReferenceSystem);
    }
}
