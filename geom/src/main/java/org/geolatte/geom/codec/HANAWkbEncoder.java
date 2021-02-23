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

import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasMeasureAxis;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasVerticalAxis;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * The HANA EWKB representation differs from the Postgis EWKB representation in
 * that HANA always requires an SRID to be written, even if its not specified or 0.
 * 
 * @author Jonathan Bregler, SAP
 */
class HANAWkbEncoder extends PostgisWkb2Encoder {

	@Override
	protected <P extends Position> int calculateSize(Geometry<P> geom, boolean includeSrid) {
		int size = 1 + ByteBuffer.UINT_SIZE; //size for order byte + type field
		if (includeSrid) {
			size += 4;
		}

		if (geom.isEmpty()) return size + ByteBuffer.UINT_SIZE;
		if (geom instanceof AbstractGeometryCollection) {
			size += sizeOfGeometryCollection((AbstractGeometryCollection<P, ?>) geom);
		} else if (geom instanceof Polygon) {
			size += getPolygonSize((Polygon<P>) geom);
		} else if (geom instanceof Point) {
			size += getPointByteSize(geom);
		} else {
			size += ByteBuffer.UINT_SIZE; //to hold number of points
			size += getPointByteSize(geom) * geom.getNumPositions();
		}
		return size;
	}

	@Override
	protected <P extends Position> WkbVisitor<P> newWkbVisitor(ByteBuffer output, Geometry<P> geom) {
		return new HANAWkbVisitor<P>(output);
	}

	static private class HANAWkbVisitor<P extends Position> extends WkbVisitor<P> {

		private boolean hasWrittenSrid = false;

		HANAWkbVisitor(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		@Override
		protected void writeTypeCodeAndSrid(Geometry<P> geometry, ByteBuffer output) {
			int typeCode = geometryTypeCode(geometry);
			CoordinateReferenceSystem<P> crs = geometry.getCoordinateReferenceSystem();
			if (!this.hasWrittenSrid) {
				typeCode |= PostgisWkbTypeMasks.SRID_FLAG;
			}
			if (geometry.hasM()) {
				typeCode |= PostgisWkbTypeMasks.M_FLAG;
			}
			if (geometry.hasZ()) {
				typeCode |= PostgisWkbTypeMasks.Z_FLAG;
			}
			output.putUInt(typeCode);
			if (!this.hasWrittenSrid) {
				int srid = geometry.getSRID();
				// Write the SRID, the HANA default SRID is 0
				output.putInt(srid < 0 ? 0 : srid);
				this.hasWrittenSrid = true;
			}
		}

		protected int geometryTypeCode(Geometry<P> geometry) {
			//empty geometries have the same representation as an empty geometry collection
			if (geometry.isEmpty() && geometry.getGeometryType() == GeometryType.POINT) {
				return WkbGeometryType.MULTI_POINT.getTypeCode();
			}
			WkbGeometryType type = WkbGeometryType.forClass(geometry.getClass());
			if (type == null) {
				throw new UnsupportedConversionException(
						String.format(
								"Can't convert geometries of type %s",
								geometry.getClass().getCanonicalName()
						)
				);
			}
			return type.getTypeCode();
		}
	}


}
