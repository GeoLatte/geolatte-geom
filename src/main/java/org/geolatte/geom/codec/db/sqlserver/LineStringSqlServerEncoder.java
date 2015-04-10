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

import java.util.List;


class LineStringSqlServerEncoder extends AbstractSqlServerEncoder {

	@Override
	protected  void encode(Geometry<?> geom, int parentShapeIndex, CountingPositionSequenceBuilder<?> coordinates, List<Figure> figures, List<Shape> shapes) {
		if ( !( geom instanceof LineString ) ) {
			throw new IllegalArgumentException( "Require LineString geometry" );
		}
		if ( geom.isEmpty() ) {
			shapes.add( new Shape( parentShapeIndex, -1, OpenGisType.LINESTRING ) );
			return;
		}
		int figureOffset = figures.size();
        int pointOffset = coordinates.getNumAdded();
        double[] c = new double[coordinates.getCoordinateDimension()];
        for ( Position pos : geom.getPositions() ) {
			coordinates.add( pos.toArray(c) );
		}
		figures.add( new Figure( FigureAttribute.Stroke, pointOffset ) );
		shapes.add( new Shape( parentShapeIndex, figureOffset, OpenGisType.LINESTRING ) );
	}

	@Override
	protected void encodePoints(SqlServerGeometry nativeGeom, PositionSequence<?> coordinates) {
		super.encodePoints( nativeGeom, coordinates );
		if ( coordinates.size() == 2 ) {
			nativeGeom.setIsSingleLineSegment();
		}
	}

	public boolean accepts(Geometry geom) {
		return geom instanceof LineString;
	}


}
