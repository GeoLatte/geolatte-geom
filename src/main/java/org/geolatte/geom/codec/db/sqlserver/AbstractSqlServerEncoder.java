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
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.codec.db.Encoder;
import org.geolatte.geom.crs.CoordinateReferenceSystems;

import java.util.ArrayList;
import java.util.List;


abstract class AbstractSqlServerEncoder implements Encoder<SqlServerGeometry> {

    @SuppressWarnings("unchecked")
    public <P extends Position, G extends Geometry<P>> SqlServerGeometry encode(G geom) {
		SqlServerGeometry nativeGeom = new SqlServerGeometry();

		if( geom.getSRID() > 0 ) {
			nativeGeom.setCoordinateReferenceSystem(geom.getCoordinateReferenceSystem());
		}

		nativeGeom.setIsValid();

		if ( CoordinateReferenceSystems.hasMeasureAxis(geom.getCoordinateReferenceSystem()) ) {
			nativeGeom.setHasMValues();
		}

		if ( CoordinateReferenceSystems.hasVerticalAxis(geom.getCoordinateReferenceSystem()) ) {
			nativeGeom.setHasZValues();
		}

        CountingPositionSequenceBuilder<?> coordinates = new CountingPositionSequenceBuilder(geom.getCoordinateReferenceSystem());
		List<Figure> figures = new ArrayList<Figure>();
		List<Shape> shapes = new ArrayList<Shape>();

		encode( geom, -1, coordinates, figures, shapes );
		encodePoints( nativeGeom, coordinates.toPositionSequence() );
		encodeFigures( nativeGeom, figures );
		encodeShapes( nativeGeom, shapes );
		return nativeGeom;
	}

	/**
	 * Appends the points, figures, shapes to the resp. lists
	 *
	 * @param geom geometry to serialization
	 * @param parentShapeIndex index of the parent Shape for the geometry
	 * @param coordinates coordinate list to append to
	 * @param figures figure list to append to
	 * @param shapes shape list to append to
	 */
	protected abstract void encode(Geometry<?> geom, int parentShapeIndex,
                                   CountingPositionSequenceBuilder<?> coordinates,
                                   List<Figure> figures, List<Shape> shapes);


	protected void encodeShapes(SqlServerGeometry nativeGeom, List<Shape> shapes) {
		nativeGeom.setNumberOfShapes( shapes.size() );
		for ( int i = 0; i < shapes.size(); i++ ) {
			nativeGeom.setShape( i, shapes.get( i ) );
		}
	}

	protected void encodeFigures(SqlServerGeometry nativeGeom, List<Figure> figures) {
		nativeGeom.setNumberOfFigures( figures.size() );
		for ( int i = 0; i < figures.size(); i++ ) {
			nativeGeom.setFigure( i, figures.get( i ) );
		}
	}


	protected void encodePoints(SqlServerGeometry nativeGeom, PositionSequence<?> coordinates) {
		nativeGeom.setNumberOfPoints( coordinates.size() );
		nativeGeom.allocateMValueArray();
		nativeGeom.allocateZValueArray();
		for ( int i = 0; i < coordinates.size(); i++ ) {
			setCoordinate( nativeGeom, i, coordinates);
		}
	}

	protected void setCoordinate(SqlServerGeometry nativeGeom, int idx, PositionSequence<?> coordinates) {
		nativeGeom.setCoordinate( idx, coordinates );
	}

}
