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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Karel Maesen, Geovise BVBA
 * creation-date: Jul 1, 2010
 */
class ElemInfo {

    static final String TYPE_NAME = "MDSYS.SDO_ELEM_INFO_ARRAY";
    private BigDecimal[] triplets;

    public ElemInfo(int numTriplets) {
        this.triplets = new BigDecimal[3 * numTriplets];
    }

    ElemInfo(BigDecimal[] elemInfo) {
        this.triplets = elemInfo;
    }

    ElemInfo(Array array) {
        if (array == null) {
            this.triplets = new BigDecimal[]{};
            return;
        }
        try {
            triplets = (BigDecimal[]) array.getArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    BigDecimal[] asRawArray() {
        return this.triplets;
    }

    List<Element> interpret(SDOGType gtype, Double[] ordinates) {
        int numElements = this.triplets.length / 3;
        ElementType[] etypes = new ElementType[numElements];
        //offsets has the start/end ordinate indexes for each element. so
        //element i has ordinates starting from index offsets[i] up to offsets[i+1]
        int[] offsets = new int[numElements + 1];
        int[] interpretations = new int[numElements];
        for (int idx = 0; idx < numElements; idx++) {
            offsets[idx] = this.triplets[3 * idx].intValue() - 1;
            int etype = this.triplets[3 * idx + 1].intValue();
            int interp = this.triplets[3 * idx + 2].intValue();
            etypes[idx] = ElementType.parseType(etype, interp);
            interpretations[idx] = interp;
        }
        //.. and pointer to after last ordinate
        offsets[numElements] = ordinates.length;

        List<Element> elements = new ArrayList<>();
        int i = 0;
        while (i < etypes.length) {
            if (etypes[i].isCompound()) {
                int numSubElems = interpretations[i];
                List<Element> subElems = new ArrayList<>();
                for (int k = 0; k < numSubElems; k++) {
                    // +1 because we need to skip the "header" element
                    int ordinateStartIndex = offsets[i + k + 1];
                    int ordinateEndIndex = offsets[i + k + 2];
                    if (k < numSubElems - 1) {
                        //the ordinate end coordinate of this sub element is the first coordinate of the next subelement
                        ordinateEndIndex += gtype.getDimension();
                    }
                    subElems.add(new SimpleElement(etypes[i + k + 1],
                            Arrays.copyOfRange(ordinates, ordinateStartIndex, ordinateEndIndex)));
                }
                elements.add(new CompoundElement(etypes[i], subElems));
                i += numSubElems + 1;
            } else {
                elements.add(new SimpleElement(etypes[i], Arrays.copyOfRange(ordinates, offsets[i], offsets[i + 1])));
                i += 1;
            }
        }
        return elements;
    }

    /**
     * Returns the number of triplets
     *
     * @return
     */
    int getNumTriplets() {
        return this.triplets.length / 3;
    }

    /**
     * Returns first ordinate of the specified element in ordinates array (1-based)
     *
     * @param i the idx of the element (0-based)
     * @return
     */
    int getOrdinatesOffset(int i) {
        return this.triplets[i * 3].intValue();
    }

    void setOrdinatesOffset(int i, int offset) {
        this.triplets[i * 3] = new BigDecimal(offset);
    }

    void setElement(int i, int ordinatesOffset, ElementType et) {
        if (i > getNumTriplets()) {
            throw new RuntimeException("Attempted to set more elements in ElemInfo Array than capacity.");
        }
        this.triplets[i * 3] = new BigDecimal(ordinatesOffset);
        this.triplets[i * 3 + 1] = new BigDecimal(et.getEType());
        this.triplets[i * 3 + 2] = new BigDecimal(et.getInterpretation());
    }

    void setElement(int i, int ordinatesOffset, ElementType et, int numElements) {
        if (i > getNumTriplets()) {
            throw new RuntimeException("Attempted to set more elements in ElemInfo Array than capacity.");
        }
        this.triplets[i * 3] = new BigDecimal(ordinatesOffset);
        this.triplets[i * 3 + 1] = new BigDecimal(et.getEType());
        this.triplets[i * 3 + 2] = new BigDecimal(numElements);
    }

    public String toString() {
        return SDOGeometry.arrayToString(this.triplets);
    }

    void addElement(BigDecimal[] element) {
        final BigDecimal[] newTriplets = new BigDecimal[this.triplets.length + element.length];
        System.arraycopy(this.triplets, 0, newTriplets, 0, this.triplets.length);
        System.arraycopy(element, 0, newTriplets, this.triplets.length, element.length);
        this.triplets = newTriplets;
    }


    void addElement(ElemInfo element) {
        this.addElement(element.asRawArray());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ElemInfo elemInfo = (ElemInfo) o;

        return Arrays.equals(triplets, elemInfo.triplets);
    }

    @Override
    public int hashCode() {
        return triplets != null ? Arrays.hashCode(triplets) : 0;
    }
}

