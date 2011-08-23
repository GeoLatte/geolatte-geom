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
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import java.util.Arrays;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class PackedPointSequence extends AbstractPointSequence {

    private final double[] coordinates;

    PackedPointSequence(double[] coordinates, DimensionalFlag dimensionalFlag){
        super(dimensionalFlag);
        if (coordinates == null) {
            this.coordinates = new double[0];
        } else {
            this.coordinates = coordinates;
        }
        if ( (this.coordinates.length % getCoordinateDimension()) != 0 )
                throw new IllegalArgumentException(String.format("coordinate array size should be a multiple of %d. Current size = %d", getCoordinateDimension(), this.coordinates.length));
    }

    @Override
    public boolean isEmpty() {
        return this.coordinates.length == 0;
    }


    @Override
    public double getCoordinate(int i, CoordinateAccessor accessor) {
        try {
            int index = getDimensionalFlag().getIndex(accessor);
            return index > -1 ? this.coordinates[i * getCoordinateDimension() + index] : Double.NaN;
        }catch(IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException(String.format("Index %d out of getPointSequenceElementAt 0..%d", i, size()-1));
        }
    }

    @Override
    public int size() {
        return this.coordinates.length/getCoordinateDimension();
    }

    @Override
    public PointSequence clone(){
        return this; //this is valid since a PackedPointSequence is immutable.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PackedPointSequence)) return false;

        PackedPointSequence that = (PackedPointSequence) o;

        if (is3D() != that.is3D()) return false;
        if (isMeasured() != that.isMeasured()) return false;
        return Arrays.equals(coordinates, that.coordinates);

        }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(coordinates);
        result = 31 * result + (isMeasured() ? 1 : 0);
        result = 31 * result + (is3D() ? 1 : 0);
        return result;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < size();i++){
            if (i > 0) builder.append(",");
            addCoordinate(i,builder);
        }
        builder.append("]");
        return builder.toString();
    }

    private void addCoordinate(int index, StringBuilder builder){
        builder.append(getOrdinate(index,0)).append(" ")
            .append(getOrdinate(index,1)).append(" ");
        if(is3D()) builder.append(getOrdinate(index, 2)).append(" ");
        if(isMeasured()) builder.append(getOrdinate(index, 3)).append(" ");
    }

}
