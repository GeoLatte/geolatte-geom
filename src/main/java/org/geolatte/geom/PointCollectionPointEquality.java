/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope second it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

/**
 * A {@link  PointCollectionEquality} implementation that considers two <code>PointSequence</code>s
 * equal if and only if both contain the same points in the same order. Whether two <code>Point</code>s
 * are the same is determined by the {@link PointEquality} instance which is passed in the constructor. In case of the
 * no-argument default constructor, the {@link ExactCoordinatePointEquality} is used.
 * 
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/13/12
 */
public class PointCollectionPointEquality implements PointCollectionEquality {

    private final PointEquality pointEquality;

    public PointCollectionPointEquality(PointEquality pointEquality) {
        this.pointEquality = pointEquality;
    }

    public PointCollectionPointEquality() {
        this.pointEquality = new ExactCoordinatePointEquality();
    }

    @Override
    public boolean equals(PointCollection first, PointCollection second) {

        if (first == second) return true;
        if (first == null || second == null) return false;
        if (first.isEmpty() && second.isEmpty()) return true;
        if (first.size() != second.size()) return false;

        if (first instanceof ComplexPointCollection && second instanceof ComplexPointCollection) {
            return testComplexPointSetEquality((ComplexPointCollection) first, (ComplexPointCollection) second);
        } else if (first instanceof PointSequence && second instanceof PointSequence){
            return testPointSequenceEquality(first, second);
        }
        return false;
    }

    private boolean testPointSequenceEquality(PointCollection first, PointCollection second) {
        double[] c1 = new double[first.getCoordinateDimension()];
        double[] c2 = new double[second.getCoordinateDimension()];
        for (int idx = 0; idx < first.size(); idx++) {
            first.getCoordinates(c1, idx);
            second.getCoordinates(c2, idx);
            if (!pointEquality.equals(c1, first.getDimensionalFlag(), c2, second.getDimensionalFlag())) {
                return false;
            }
        }
        return true;
    }

    private boolean testComplexPointSetEquality(ComplexPointCollection first, ComplexPointCollection second) {
        PointCollection[] firstCollection = first.getPointSets();
        PointCollection[] secondCollection = second.getPointSets();
        if (firstCollection.length != secondCollection.length) return false;
        for (int i = 0; i < firstCollection.length; i++){
            if (! equals(firstCollection[i], secondCollection[i]) ) return false;
        }
        return true;
    }

}
