/*
 * This file is part of Hibernate Spatial, an extension to the
 *  hibernate ORM solution for spatial (geographic) data.
 *
 *  Copyright © 2007-2012 Geovise BVBA
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.geolatte.geom.codec.db.oracle;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jun 30, 2010
 */
class SDOGType {

	private int dimension = 2;

	private int lrsDimension;

	private TypeGeometry typeGeometry = TypeGeometry.UNKNOWN_GEOMETRY;

	static SDOGType derive(ElementType elementType, SDOGeometry origGeom) {
		switch (elementType) {
			case POINT:
			case ORIENTATION:
				return new SDOGType(
						origGeom.getDimension(), origGeom
						.getLRSDimension(), TypeGeometry.POINT
				);
			case POINT_CLUSTER:
				return new SDOGType(
						origGeom.getDimension(), origGeom
						.getLRSDimension(), TypeGeometry.MULTIPOINT
				);
			case LINE_ARC_SEGMENTS:
			case LINE_STRAITH_SEGMENTS:
			case COMPOUND_LINE:
				return new SDOGType(
						origGeom.getDimension(), origGeom
						.getLRSDimension(), TypeGeometry.LINE
				);
			case COMPOUND_EXTERIOR_RING:
			case EXTERIOR_RING_ARC_SEGMENTS:
			case EXTERIOR_RING_CIRCLE:
			case EXTERIOR_RING_RECT:
			case EXTERIOR_RING_STRAIGHT_SEGMENTS:
				return new SDOGType(
						origGeom.getDimension(), origGeom
						.getLRSDimension(), TypeGeometry.POLYGON
				);
			default:
				return null;
		}
	}


	public SDOGType(int dimension, int lrsDimension,
					TypeGeometry typeGeometry) {
		setDimension( dimension );
		setLrsDimension( lrsDimension );
		setTypeGeometry( typeGeometry );
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		if ( dimension < 2 || dimension > 4 ) {
			throw new IllegalArgumentException(
					"Dimension can only be 2,3 or 4."
			);
		}
		this.dimension = dimension;
	}

	public TypeGeometry getTypeGeometry() {
		return typeGeometry;
	}

	public void setTypeGeometry(TypeGeometry typeGeometry) {

		this.typeGeometry = typeGeometry;
	}

	public int getLRSDimension() {
		if ( this.lrsDimension > 0 ) {
			return this.lrsDimension;
		}
		else if ( this.lrsDimension == 0 && this.dimension == 4 ) {
			return 4;
		}
		return 0;
	}

	public int getZDimension() {
        if (getLRSDimension() == 3 && this.dimension == 3){
            return -1;
        } else if( getLRSDimension() == 3 && this.dimension == 4 ) {
            return 4;
        } else if (getLRSDimension() == 4 && this.dimension == 4) {
            return 3;
        } else {
            return this.dimension > 2 ? 3 : -1;
        }
	}

	public boolean isLRSGeometry() {
		return getLRSDimension() > 0;
	}

	public void setLrsDimension(int lrsDimension) {
		if ( lrsDimension != 0 && lrsDimension > this.dimension ) {
			throw new IllegalArgumentException(
					"lrsDimension must be 0 or lower or equal to dimenstion."
			);
		}
		this.lrsDimension = lrsDimension;
	}

	public int intValue() {
		int v = this.dimension * 1000;
		v += lrsDimension * 100;
		v += typeGeometry.intValue();
		return v;
	}

	public static SDOGType parse(int v) {
		final int dim = v / 1000;
		v -= dim * 1000;
		final int lrsDim = v / 100;
		v -= lrsDim * 100;
		final TypeGeometry typeGeometry = TypeGeometry.parse( v );
		return new SDOGType( dim, lrsDim, typeGeometry );
	}

	public static SDOGType parse(Object datum) {

		try {
			final int v = ( (Number) datum ).intValue();
			return parse( v );
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}

	}

	public String toString() {
		return Integer.toString( this.intValue() );
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SDOGType sdogType = (SDOGType) o;

        if (dimension != sdogType.dimension) return false;
        if (lrsDimension != sdogType.lrsDimension) return false;
        if (typeGeometry != sdogType.typeGeometry) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dimension;
        result = 31 * result + lrsDimension;
        result = 31 * result + (typeGeometry != null ? typeGeometry.hashCode() : 0);
        return result;
    }
}
