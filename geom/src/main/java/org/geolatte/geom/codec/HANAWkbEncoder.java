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

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * The HANA EWKB representation differs from the Postgis EWKB representation in
 * that HANA always requires an SRID to be written, even if its not specified or 0.
 * 
 * @author Jonathan Bregler, SAP
 */
class HANAWkbEncoder extends PostgisWkbEncoder {

	@Override
	protected <P extends Position> int calculateSize(Geometry<P> geom, boolean includeSrid) {
		int size = super.calculateSize(geom, includeSrid);
		// HANA always expects the SRID, the Postgis encoder doesn't write it unless it's > 0 
		return (includeSrid && geom.getSRID() <= 0) ? size + 4 : size;
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
			int typeCode = getGeometryType(geometry);
			CoordinateReferenceSystem<P> crs = geometry.getCoordinateReferenceSystem();
			if (!this.hasWrittenSrid) {
				typeCode |= PostgisWkbTypeMasks.SRID_FLAG;
			}
			if (hasMeasureAxis(crs)) {
				typeCode |= PostgisWkbTypeMasks.M_FLAG;
			}
			if (hasVerticalAxis(crs)) {
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

	}

}
