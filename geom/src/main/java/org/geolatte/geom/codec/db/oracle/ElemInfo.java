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

import java.math.BigDecimal;
import java.sql.Array;
import java.util.Arrays;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 1, 2010
 */
class ElemInfo {

	static final String TYPE_NAME = "MDSYS.SDO_ELEM_INFO_ARRAY";
	private BigDecimal[] triplets;

	public ElemInfo(int size) {
		this.triplets = new BigDecimal[3 * size];
	}

	public ElemInfo(BigDecimal[] elemInfo) {
		this.triplets = elemInfo;
	}

	public ElemInfo(Array array) {
		if ( array == null ) {
			this.triplets = new BigDecimal[] { };
			return;
		}
		try {
			triplets = (BigDecimal[]) array.getArray();
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public BigDecimal[] getElements() {
		return this.triplets;
	}

	public ElemInfoTriplet[] interpret() {
		ElemInfoTriplet[] result = new ElemInfoTriplet[getSize()];
		for(int idx = 0; idx < getSize(); idx++){
			BigDecimal[] triplet = new BigDecimal[3];
			System.arraycopy(this.triplets, 3 * idx, triplet, 0, 3);
			result[idx] = ElemInfoTriplet.parse(triplet);
		}
		return result;
	}

	/**
	 * Returns the number of elements
	 * @return
	 */
	public int getSize() {
		return this.triplets.length / 3;
	}

	/**
	 * Returns first ordinate of the specified element in ordinates array (1-based)
	 *
	 * @param i the idx of the element (0-based)
	 * @return
	 */
	public int getOrdinatesOffset(int i) {
		return this.triplets[i * 3].intValue();
	}

	public void setOrdinatesOffset(int i, int offset) {
		this.triplets[i * 3] = new BigDecimal( offset );
	}

	public ElementType getElementType(int i) {
		final int etype = this.triplets[i * 3 + 1].intValue();
		final int interp = this.triplets[i * 3 + 2].intValue();
		return ElementType.parseType( etype, interp );
	}

	public boolean isCompound(int i) {
		return getElementType( i ).isCompound();
	}

	public int getNumCompounds(int i) {
		if ( getElementType( i ).isCompound() ) {
			return this.triplets[i * 3 + 2].intValue();
		}
		else {
			return 1;
		}
	}

	public void setElement(int i, int ordinatesOffset, ElementType et, int numCompounds) {
		if ( i > getSize() ) {
			throw new RuntimeException(
					"Attempted to set more elements in ElemInfo Array than capacity."
			);
		}
		this.triplets[i * 3] = new BigDecimal( ordinatesOffset );
		this.triplets[i * 3 + 1] = new BigDecimal( et.getEType() );
		this.triplets[i * 3 + 2] = et.isCompound() ?
				new BigDecimal( numCompounds ) :
				new BigDecimal(et.getInterpretation());
	}

	public String toString() {
		return SDOGeometry.arrayToString( this.triplets );
	}

	public void addElement(BigDecimal[] element) {
		final BigDecimal[] newTriplets = new BigDecimal[this.triplets.length + element.length];
		System.arraycopy(
				this.triplets, 0, newTriplets, 0,
				this.triplets.length
		);
		System.arraycopy(
				element, 0, newTriplets, this.triplets.length,
				element.length
		);
		this.triplets = newTriplets;
	}

	public void addElement(ElemInfo element) {
		this.addElement( element.getElements() );
	}

	public BigDecimal[] getElement(int i) {
		BigDecimal[] ea = null;
		if ( this.getElementType( i ).isCompound() ) {
			final int numCompounds = this.getNumCompounds( i );
			ea = new BigDecimal[numCompounds + 1];
		}
		else {
			ea = new BigDecimal[3];
		}
		System.arraycopy( this.triplets, 3 * i, ea, 0, ea.length );
		return ea;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ElemInfo elemInfo = (ElemInfo) o;

        if (!Arrays.equals(triplets, elemInfo.triplets)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return triplets != null ? Arrays.hashCode(triplets) : 0;
    }
}

