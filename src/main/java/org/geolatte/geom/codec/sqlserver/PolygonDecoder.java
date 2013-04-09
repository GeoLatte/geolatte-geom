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

import org.geolatte.geom.LinearRing;
import org.geolatte.geom.PointSequence;
import org.geolatte.geom.Polygon;

/**
 * @author Karel Maesen, Geovise BVBA
 */
class PolygonDecoder extends AbstractDecoder<Polygon> {

	@Override
	protected OpenGisType getOpenGisType() {
		return OpenGisType.POLYGON;
	}

	protected Polygon createNullGeometry() {
        return Polygon.createEmpty();
    }

	protected Polygon createGeometry(SqlServerGeometry nativeGeom) {
		return createGeometry( nativeGeom, 0 );
	}

	protected Polygon createGeometry(SqlServerGeometry nativeGeom, int shapeIndex) {
		if ( nativeGeom.isEmptyShape( shapeIndex ) ) {
			return createNullGeometry();
		}
		//polygons consist of one exterior ring figure, and several interior ones.
		IndexRange figureRange = nativeGeom.getFiguresForShape( shapeIndex );
		LinearRing[] rings = new LinearRing[figureRange.length()];
        //the rings should contain all inner rings from index 1 to index length - 1
        // index = 0 should be reserved for the shell.
		for ( int figureIdx = figureRange.start, i = 1; figureIdx < figureRange.end; figureIdx++ ) {
			IndexRange pntIndexRange = nativeGeom.getPointsForFigure( figureIdx );
			if ( nativeGeom.isFigureInteriorRing( figureIdx ) ) {
				rings[i++] = toLinearRing( nativeGeom, pntIndexRange );
			}
			else {
				rings[0] = toLinearRing( nativeGeom, pntIndexRange );
			}
		}
        return new Polygon(rings);
	}

	private LinearRing toLinearRing(SqlServerGeometry nativeGeom, IndexRange range) {
        PointSequence pointSequence = nativeGeom.coordinateRange(range);
        return new LinearRing(pointSequence);
	}

}
