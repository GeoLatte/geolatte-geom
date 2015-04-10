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

/**
 * <code>Encoder</code> for Polygons.
 *
 * @uthor Karel Maesen, Geovise BVBA
 */
class PolygonSqlServerEncoder extends AbstractSqlServerEncoder {

	public boolean accepts(Geometry geom) {
		return geom instanceof Polygon;
	}

	@Override
	protected void encode(Geometry<?> geom, int parentShapeIndex, CountingPositionSequenceBuilder<?> coordinates, List<Figure> figures, List<Shape> shapes) {
		if ( !( geom instanceof Polygon ) ) {
			throw new IllegalArgumentException( "Polygon geometry expected." );
		}
		if ( geom.isEmpty() ) {
			shapes.add( new Shape( parentShapeIndex, -1, OpenGisType.POLYGON ) );
			return;
		}
		Polygon polygon = (Polygon) geom;
		int figureOffset = figures.size();
		shapes.add( new Shape( parentShapeIndex, figureOffset, OpenGisType.POLYGON ) );

		int pointOffset = coordinates.getNumAdded();
		addExteriorRing( polygon, coordinates, figures );
		addInteriorRings( polygon, coordinates, figures );

	}


	private void addInteriorRings(Polygon<?> geom, CountingPositionSequenceBuilder<?> coordinates, List<Figure> figures) {
		for ( int idx = 0; idx < geom.getNumInteriorRing(); idx++ ) {
			addInteriorRing( geom.getInteriorRingN( idx ), coordinates, figures );
		}
	}

	private void addInteriorRing(LineString<?> ring, CountingPositionSequenceBuilder<?> coordinates, List<Figure> figures) {
		int pointOffset = coordinates.getNumAdded();
		addPoints( ring, coordinates );
		Figure figure = new Figure( FigureAttribute.InteriorRing, pointOffset );
		figures.add( figure );

	}

	private void addPoints(LineString<?> ring, CountingPositionSequenceBuilder<?> coordinates) {
        double[] c = new double[coordinates.getCoordinateDimension()];
		for ( Position p : ring.getPositions() ) {
			coordinates.add( p.toArray(c) );
		}
	}

	private void addExteriorRing(Polygon geom, CountingPositionSequenceBuilder coordinates, List<Figure> figures) {
		LineString shell = geom.getExteriorRing();
		int offset = coordinates.getNumAdded();
		addPoints( shell, coordinates );
		Figure exterior = new Figure( FigureAttribute.ExteriorRing, offset );
		figures.add( exterior );
	}


}
