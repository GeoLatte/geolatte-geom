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

import java.sql.Array;
import java.util.Arrays;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 1, 2010
 */
class Ordinates {

	static final String TYPE_NAME = "MDSYS.SDO_ORDINATE_ARRAY";

	private Double[] ordinates;

	public Ordinates(Double[] ordinates) {
		this.ordinates = ordinates;
	}

	public Ordinates(Array array) {
		if ( array == null ) {
			this.ordinates = new Double[] { };
			return;
		}
		try {
			final Number[] ords = (Number[]) array.getArray();
			this.ordinates = new Double[ords.length];
			for ( int i = 0; i < ords.length; i++ ) {
				this.ordinates[i] = ords[i] != null ? ords[i].doubleValue()
						: Double.NaN;
			}
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public Double[] getOrdinateArray() {
		return this.ordinates;
	}

	public Double[] getOrdinatesArray(int startPosition, int endPosition) {
		final Double[] a = new Double[endPosition - startPosition];
		System.arraycopy( this.ordinates, startPosition - 1, a, 0, a.length );
		return a;
	}

	public Double[] getOrdinatesArray(int startPosition) {
		final Double[] a = new Double[this.ordinates.length - ( startPosition - 1 )];
		System.arraycopy( this.ordinates, startPosition - 1, a, 0, a.length );
		return a;
	}

	public boolean isEmpty() {
		return ordinates.length == 0;
	}

	public String toString() {
		return SDOGeometry.arrayToString( this.ordinates );
	}

	public void addOrdinates(Double[] ordinatesToAdd) {
		final Double[] newOrdinates = new Double[this.ordinates.length
				+ ordinatesToAdd.length];
		System.arraycopy(
				this.ordinates, 0, newOrdinates, 0,
				this.ordinates.length
		);
		System.arraycopy(
				ordinatesToAdd, 0, newOrdinates,
				this.ordinates.length, ordinatesToAdd.length
		);
		this.ordinates = newOrdinates;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ordinates ordinates1 = (Ordinates) o;

        if (!Arrays.equals(ordinates, ordinates1.ordinates)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ordinates != null ? Arrays.hashCode(ordinates) : 0;
    }
}
