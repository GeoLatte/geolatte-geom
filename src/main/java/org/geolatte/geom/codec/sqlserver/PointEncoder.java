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

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;

import java.util.List;


/**
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
class PointEncoder extends AbstractEncoder<Point> {

	/**
	 * Encodes a point as an <code>SQLGeometryV1</code> object.
	 * <p/>
	 * This is a specific implementation because points don't explicitly serialize figure and shape components.
	 *
	 * @param geom Geometry to serialize
	 *
	 * @return
	 */
	@Override
	public SqlServerGeometry encode(Point geom) {

		SqlServerGeometry sqlServerGeom = new SqlServerGeometry();
		int srid = geom.getSRID();
		sqlServerGeom.setSrid( srid < 0 ? 0 : srid );
		sqlServerGeom.setIsValid();

		if ( geom.isEmpty() ) {
			sqlServerGeom.setNumberOfPoints( 0 );
			sqlServerGeom.setNumberOfFigures( 0 );
			sqlServerGeom.setNumberOfShapes( 1 );
			sqlServerGeom.setShape( 0, new Shape( -1, -1, OpenGisType.POINT ) );
			return sqlServerGeom;
		}

		sqlServerGeom.setIsSinglePoint();
		sqlServerGeom.setNumberOfPoints( 1 );
		if ( geom.is3D() ) {
			sqlServerGeom.setHasZValues();
			sqlServerGeom.allocateZValueArray();
		}
		if ( geom.isMeasured() ) {
			sqlServerGeom.setHasMValues();
			sqlServerGeom.allocateMValueArray();
		}
		sqlServerGeom.setCoordinate( 0, geom.getPoints() );
		return sqlServerGeom;
	}

    @Override
    protected void encode(Geometry geom, int parentIdx, CountingPointSequenceBuilder coordinates, List<Figure> figures, List<Shape> shapes) {
		if ( !( geom instanceof Point ) ) {
			throw new IllegalArgumentException( "Require Point geometry" );
		}
		if ( geom.isEmpty() ) {
			shapes.add( new Shape( parentIdx, -1, OpenGisType.POINT ) );
			return;
		}
		int pntOffset = coordinates.getNumAdded();
		int figureOffset = figures.size();
		coordinates.add( geom.getPointN(0) );
		Figure figure = new Figure( FigureAttribute.Stroke, pntOffset );
		figures.add( figure );
		Shape shape = new Shape( parentIdx, figureOffset, OpenGisType.POINT );
		shapes.add( shape );
	}

	public boolean accepts(Geometry geom) {
		return geom instanceof Point;
	}
}
