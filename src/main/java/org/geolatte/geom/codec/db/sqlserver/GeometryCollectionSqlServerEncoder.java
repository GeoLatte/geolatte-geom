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
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.Position;

import java.util.List;

/**
 * <code>Encoder</code> for GeometryCollections.
 *
 * @Author Karel Maesen
 */
class GeometryCollectionSqlServerEncoder extends AbstractSqlServerEncoder {

	private final OpenGisType openGisType;

	GeometryCollectionSqlServerEncoder(OpenGisType openGisType) {
		this.openGisType = openGisType;
	}

	public <P extends Position> boolean accepts(Geometry<P> geom) {
		return this.openGisType.typeOf( geom );
	}

	@Override
	protected  void encode(Geometry<?> geom, int parentShapeIndex, CountingPositionSequenceBuilder<?> coordinates, List<Figure> figures, List<Shape> shapes) {
		if ( geom.isEmpty() ) {
			shapes.add( new Shape( parentShapeIndex, -1, this.openGisType ) );
			return;
		}
		int thisShapeIndex = shapes.size();
		Shape thisShape = createShape( parentShapeIndex, figures );
		shapes.add( thisShape );
		if (! (geom instanceof GeometryCollection)) {
			throw new IllegalArgumentException( "Expect GeometryCollection argument." );
		}
		GeometryCollection gc =  (GeometryCollection) geom;
		for ( int i = 0; i < gc.getNumGeometries(); i++ ) {
			Geometry component = gc.getGeometryN( i );
			encodeComponent( component, thisShapeIndex, coordinates, figures, shapes );
		}
	}

	protected Shape createShape(int parentShapeIndex, List<Figure> figures) {
		Shape thisShape = new Shape( parentShapeIndex, figures.size(), this.openGisType );
		return thisShape;
	}

	protected void encodeComponent(Geometry geom, int thisShapeIndex, CountingPositionSequenceBuilder coordinates, List<Figure> figures, List<Shape> shapes) {
		AbstractSqlServerEncoder encoder = (AbstractSqlServerEncoder) Encoders.encoderFor( geom );
		encoder.encode( geom, thisShapeIndex, coordinates, figures, shapes );
	}
}
