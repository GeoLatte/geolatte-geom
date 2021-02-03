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

import org.geolatte.geom.codec.db.Decoder;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 1, 2010
 */
enum TypeGeometry {

	UNKNOWN_GEOMETRY(0) {
		@Override
		Decoder createDecoder() {
			throw new UnsupportedOperationException();
		}
	},
	POINT(1) {
		@Override
		Decoder createDecoder() {
			return new PointSdoDecoder();
		}
	},
	LINE(2) {
		@Override
		Decoder createDecoder() {
			return new LineStringSdoDecoder();
		}
	},
	POLYGON(3) {
		@Override
		Decoder createDecoder() {
			return new PolygonSdoDecoder();
		}
	},
	COLLECTION(4) {
		@Override
		Decoder createDecoder() {
			return new GeometryCollectionSdoDecoder();
		}
	},
	MULTIPOINT(5) {
		@Override
		Decoder createDecoder() {
			return new MultiPointSdoDecoder();
		}
	},
	MULTILINE(6) {
		@Override
		Decoder createDecoder() {
			return new MultiLineSdoDecoder();
		}
	},
	MULTIPOLYGON(7) {
		@Override
		Decoder createDecoder() {
			return new MultiPolygonSdoDecoder();
		}
	},
	SOLID(8) {
		@Override
		Decoder createDecoder() {
			throw new UnsupportedOperationException();
		}
	},
	MULTISOLID(9) {
		@Override
		Decoder createDecoder() {
			throw new UnsupportedOperationException();
		}
	};

	private int gtype;

	TypeGeometry(int gtype) {
		this.gtype = gtype;
	}

	int intValue() {
		return this.gtype;
	}

	static TypeGeometry parse(int v) {
		for ( TypeGeometry gt : values() ) {
			if ( gt.intValue() == v ) {
				return gt;
			}
		}
		throw new RuntimeException(
				"Value " + v
						+ " isn't a valid TypeGeometry value"
		);
	}

	abstract Decoder createDecoder();
}
