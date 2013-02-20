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
 * Copyright (C) 2010 - 2013 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.curve;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;

/**
 * Calculates the Morton code (Morton-order or Z-order) of Geometries
 *
 * <p>Morton codes are here considered as labels of a QuadTree-index that is implied by the MortonContext, i.e. by
 * a spatial extent and depth of the tree (the number of recursive subdivisions in the QuadTree).</p>
 *
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/13
 */
public class MortonCode {

    private final MortonContext mortonContext;
    /**
     * The width of the lowest-level leaves of the quadtree implied by the MortonContext
     */
    private final double baseX;
    /**
     * The height of the lowest-level leaves of the quadtree implied by the MortonContext
     */
    private final double baseY;

    public MortonCode(MortonContext mortonContext) {
        this.mortonContext = mortonContext;
        baseX = (mortonContext.getMaxX() - mortonContext.getMinX()) / Math.pow(2, mortonContext.getDepth());
        baseY = (mortonContext.getMaxY() - mortonContext.getMinY()) / Math.pow(2, mortonContext.getDepth());
    }

    public String ofGeometry(Geometry geometry) {
        return ofEnvelope(geometry.getEnvelope());
    }

    public String ofEnvelope(Envelope envelope) {

        // recalculate the X,Y coordinates to grid-cell coordinates. These are
        // the row,column-indices of
        // the lowest-level grid that is formed by the Quadtree with a depth
        // of levelCount.

        int colMin = (int) Math.floor((envelope.getMinX() - mortonContext.getMinX()) / baseX);
        int rowMin = (int) Math.floor((envelope.getMinY() - mortonContext.getMinY()) / baseY);
        int colMax = (int) Math.floor((envelope.getMaxX() - mortonContext.getMinX()) / baseX);
        int rowMax = (int) Math.floor((envelope.getMaxY() - mortonContext.getMinY()) / baseY);
        int[] cols = {colMin, colMax};
        int[] rows = {rowMin, rowMax};

        long[] interLeaved = {0L, 0L}; // array of bit arrays.
        // interLeaved[i] is treated as a
        // bit array


        // interleave the binary representation of the M/N cell-coordinates
        for (int i = 0; i < 2; i++) {
            int level = mortonContext.getDepth();
            while (level > 0) {
                interLeaved[i] = interLeaved[i] << 1;
                interLeaved[i] = interLeaved[i] | (cols[i] % 2);
                interLeaved[i] = interLeaved[i] << 1;
                interLeaved[i] = interLeaved[i] | (rows[i] % 2);
                rows[i] = rows[i] / 2;
                cols[i] = cols[i] / 2;
                level--;
            }
        }
        // re-interpret the bit arrays as sequences of base-4 numerals.
        // return only the common part of the sequences. That is the
        // mortoncode for the whole feature.
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < mortonContext.getDepth(); i++) {
            int cel1 = (int) (interLeaved[0] & 3);
            int cel2 = (int) (interLeaved[1] & 3);
            if (cel1 == cel2) {
                stb.append(cel1);
            } else {
                break;
            }
            interLeaved[0] = interLeaved[0] >> 2;
            interLeaved[1] = interLeaved[1] >> 2;
        }
        return stb.toString();
    }

    public String ofPoint(Point point) {
        int col = (int) Math.floor((point.getX() - mortonContext.getMinX()) / baseX);
        int row = (int) Math.floor((point.getY() - mortonContext.getMinY()) / baseY);
        long interLeaved = 0L;
        int level = mortonContext.getDepth();
        while (level > 0) {
            interLeaved = interLeaved << 1;
            interLeaved = interLeaved | (col % 2);
            interLeaved = interLeaved << 1;
            interLeaved = interLeaved | (row % 2);
            row = row / 2;
            col = col / 2;
            level--;
        }
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < mortonContext.getDepth(); i++) {
            int cell = (int) (interLeaved & 3);
            stb.append(cell);
        }
        return stb.toString();
    }


    //TODO -- interleave as seperate function....

//    private long interleave(int a, int b){
//
//    }
}
