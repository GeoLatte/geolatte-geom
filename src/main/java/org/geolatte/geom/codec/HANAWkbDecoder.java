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

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * The HANA EWKB decoder is equivalent to the Postgis EWKB decoder and is there mostly for symmetry reasons.
 * 
 * @author Jonathan Bregler, SAP
 */
class HANAWkbDecoder extends PostgisWkbDecoder {

	private int currentTypeCode;

	@Override
	protected <P extends Position> CoordinateReferenceSystem<P> readCrs(ByteBuffer byteBuffer, int typeCode, CoordinateReferenceSystem<P> crs) {
		boolean hasM = ( this.currentTypeCode & PostgisWkbTypeMasks.M_FLAG ) == PostgisWkbTypeMasks.M_FLAG;
		boolean hasZ = ( this.currentTypeCode & PostgisWkbTypeMasks.Z_FLAG ) == PostgisWkbTypeMasks.Z_FLAG;

		if ( ( this.currentTypeCode & 0xFFFF ) > 3000 ) {
			hasM = true;
			hasZ = true;
		}
		else if ( ( this.currentTypeCode & 0xFFFF ) > 2000 ) {
			hasM = true;
		}
		else if ( ( this.currentTypeCode & 0xFFFF ) > 1000 ) {
			hasZ = true;
		}

		int modifiedTypeCode = this.currentTypeCode;
		if ( hasM ) {
			modifiedTypeCode = modifiedTypeCode | PostgisWkbTypeMasks.M_FLAG;
		}
		if ( hasZ ) {
			modifiedTypeCode = modifiedTypeCode | PostgisWkbTypeMasks.Z_FLAG;
		}
		return super.readCrs( byteBuffer, modifiedTypeCode, crs );
	}

	@Override
	protected int readTypeCode(ByteBuffer byteBuffer) {
		this.currentTypeCode = super.readTypeCode( byteBuffer );
		return (this.currentTypeCode & 0xFFFF) % 100;
	}
}
