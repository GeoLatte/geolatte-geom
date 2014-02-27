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
 * Copyright (C) 2010 - 2014 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.crs;

/**
 * Maps coordinates in the order defined by the CoordinateReferenceSystem's axes to
 * the normalized order. The normalized order is:
 *
 * <ul>
 * <li>EAST or SOUTH</li>
 * <li>NORTH or WEST</li>
 * <li>UP or DOWN (if present)</li>
 * <li>OTHER or UNKNOWN (if present)</li>
 * </ul>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/3/14
 */
public class NormalizedOrder {

    // maps normalized
    final private int[] normalized2crs;
    final private int[] crs2normalized;
    final private int normalVertical;
    final private int normalMeasure;
    final private int coordDim;

    protected NormalizedOrder(CoordinateSystem cs) {
        this.coordDim = cs.getCoordinateDimension();
        normalized2crs = new int[cs.getCoordinateDimension()];
        crs2normalized = new int[cs.getCoordinateDimension()];
        int numOther = 2;
        int nv = -1;
        int nm = -1;
        if (cs.getVerticalAxis() != null) {
            nv = 2;
            numOther = 3;
        }
        if (cs.getMeasureAxes().length > 0 ) {
            nm = numOther;
        }
        normalVertical = nv;
        normalMeasure = nm;
        for (int idx = 0; idx < cs.getCoordinateDimension(); idx++) {
            int normalOrder = getAxisNormalOrder(cs, idx, numOther);
            if (normalOrder >= cs.getCoordinateDimension()) {
                throw new InconsistentCoordinateSystemException("Coordinate Axis mapped outside of coordinate dimension");
            }
            if (normalOrder == numOther) {
                crs2normalized[idx] = numOther;
                normalized2crs[numOther] = idx;
                numOther++;
            } else {
                normalized2crs[normalOrder] = idx;
                crs2normalized[idx] = normalOrder;
            }
        }
    }



//TODO where to move the R copy(P, rcs) method?
//    /**
//     * Creates a copy of the specified source {@code Position} to a {@code Position} in the target reference system.
//     *
//     * Source coordinate values are copied to corresponding normalized position in the returned {@code Position}. If there
//     * are no corresponding source coordinate values, Double.NaN is substituted; if there are not destination corresponding
//     * coordinates the source coordinates are ignored.
//     *
//     * This method does not check if the source and target Position types are in any sens compatible.
//     *
//     * @param src the source {@code Position}
//     * @param destCrs the {@code CoordinateReferenceSystem} of the return {@code Position}
//     * @param <R> the {@code Position} subtype of the return value
//     * @return a {@code Position}
//     */
//   public <R extends Position<R>> R copy(P src, CoordinateReferenceSystem<R> destCrs) {
//
//   }

   public double[] crsDefinedToNormalized(double... coords) {
       return mapped(crs2normalized, coords);

   }

   public double[] normalizedToCrsDefined(double... coords) {
       return mapped(normalized2crs, coords);
   }

   public int getNormalizedPosition(int axisIdx) {
       return crs2normalized[axisIdx];
   }

    /**
     * Returns the index for the Vertical axis in the (normal order) coordinate array, or -1
     * if no vertical axis
     *
     * @return Returns the index for the Vertical axis in the (normal order) coordinate array, or -1
     * if no vertical axis
     */
   public int getNormalVertical() {
       return normalVertical;
   }

    /**
     * Returns the index for the (first) Measure axis in the (normal order) coordinate array, or -1
     * if no Measure axis
     *
     * @return Returns the index for the Measure axis in the (normal order) coordinate array, or -1
     * if no vertical axis
     */
   public int getNormalMeasure() {
       return normalMeasure;
   }

   private double[] mapped(int[] map, double[] coords){
         //TODO .. or rather accept arrays smaller than coord. dimension and fill in rest with NaN??
       if (coords.length != coordDim) {
           throw new IllegalArgumentException("Require array of size " + coordDim);
       }
       double[] res = new double[coordDim];
       for(int i = 0; i < coordDim; i++){
            res[map[i]] = coords[i];
       }
       return res;
   }

   private int getAxisNormalOrder(CoordinateSystem cs, int idx, int numOther) {
       CoordinateSystemAxis axis = cs.getAxis(idx);
       switch (axis.getAxisDirection()) {
           case EAST:
           case SOUTH:
           case GeocentricX:
               return 0;
           case NORTH:
           case WEST:
           case GeocentricY:
               return 1;
           case UP:
           case DOWN:
           case GeocentricZ:
               return 2;
           default:
               return numOther;

       }
   }

}
