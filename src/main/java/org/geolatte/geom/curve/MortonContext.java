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

import org.geolatte.geom.Point;

/**
 * A context for the calculation of Morton codes.
 *
 * <p>This class holds the the maximum spatial extent and the depth of the implied
 * QuadTree-index for a <code>MortonCode</code>. </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/13
 */
public class MortonContext {

    final private double maxX;
    final private double maxY;
    final private double minX;
    final private double minY;
    final private int depth;

    public MortonContext(Point lowerLeft, Point upperRight, int depth){
        this.minX = lowerLeft.getX();
        this.minY = lowerLeft.getY();
        this.maxX = upperRight.getX();
        this.maxY = upperRight.getY();
        this.depth = depth;
    }

    /**
     * Returns the Maximum
     * @return
     */
    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public int getDepth() {
        return depth;
    }

}
