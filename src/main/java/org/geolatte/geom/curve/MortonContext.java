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
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CrsId;

/**
 * A context for the calculation of Morton codes.
 * <p/>
 * <p>This class holds the the maximum spatial extent and tree-depth of the implied
 * QuadTree-index for a {@code MortonCode}.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/13
 */
public class MortonContext {

    final private static String ERR_MSG_MAX_DEPTH = "Max. depth is limited to " + (Integer.SIZE - 1);
    final private static String ERR_MSG_NULL = "No Null arguments allowed.";
    final private int depth;
    final private Envelope extent;
    final private int numLeaves;
    final private double leafWidth;
    final private double leafHeight;

    /**
     * Constructs a {@code MortonContext} with the specified extent and max. tree-depth
     *
     * @param extent the extent for this instance
     * @param depth  the tree-depth
     */
    public MortonContext(Envelope extent, int depth) {
        if (extent == null) {
            throw new IllegalArgumentException(ERR_MSG_NULL);
        }

        if (depth >= Integer.SIZE) {
            throw new IllegalArgumentException(ERR_MSG_MAX_DEPTH);
        }
        this.depth = depth;
        this.extent = extent;

        this.numLeaves = (int) (Math.pow(2, depth));
        this.leafWidth =  (extent.getMaxX() - extent.getMinX()) / numLeaves;
        this.leafHeight = (extent.getMaxY() - extent.getMinY()) / numLeaves;

    }

    /**
     * Returns the maximum X-coordinate of the extent.
     *
     * @return the maximum X-coordinate of the extent
     */
    public double getMaxX() {
        return extent.getMaxX();
    }

    /**
     * Returns the maximum Y-coordinate of the extent.
     *
     * @return the maximum Y-coordinate of the extent
     */
    public double getMaxY() {
        return extent.getMaxY();
    }

    /**
     * Returns the minimum X-coordinate of the extent
     *
     * @return the minimum X-coordinate of the extent
     */
    public double getMinX() {
        return extent.getMinX();
    }
    /**
     * Returns the minimum Y-coordinate of the extent
     *
     * @return the minimum Y-coordinate of the extent
     */
    public double getMinY() {
        return extent.getMinY();
    }

    /**
     * Returns the maximum tree-depth
     *
     * @return the maximum tree-depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Returns the CrsId of the spatial extent
     *
     * @return the CrsId of the spatial extent
     */
    public CrsId getCrsId() {
        return extent.getCrsId();
    }

    /**
     * Returns the number of leaves in the QuadTree at the tree-depth (lowest-level).
     *
     * @return the number of leaves in the QuadTree at the tree-depth (lowest-level).
     */
    public int getNumberOfLeaves() {
        return this.numLeaves;
    }

    /**
     * Returns the width of the extent of a leaf in the QuadTree at tree-depth (lowest-level)
     *
     * @return the width of the extent of a leaf in the QuadTree at tree-depth (lowest-level)
     */
    public double getLeafWidth(){
        return this.leafWidth;
    }

    /**
     * Returns the height of the extent of a leaf in the QuadTree at tree-depth (lowest-level)
     *
     * @return the height of the extent of a leaf in the QuadTree at tree-depth (lowest-level)
     */
    public double getLeafHeight(){
        return this.leafHeight;
    }

    /**
     * Checks whether the specified {@code Point} is contained in the extent.
     *
     * @param pnt a {@code Point} value
     * @return true if the specified point is contained in the extent, false otherwise
     * @throws IllegalArgumentException if the specified point does not share this extent's {@code CrsId}
     */
    public boolean extentContains(Point pnt) {
        return this.extent.contains(pnt);
    }

    /**
     * Checks whether the specified {@code Envelope} is contained in the extent.
     *
     * @param envelope an {@code Envelope} value
     * @return true if the specified envelope is contained in the extent, false otherwise
     * @throws IllegalArgumentException if the specified envelope does not share this extent's {@code CrsId}
     */
    public boolean extentContains(Envelope envelope) {
        return this.extent.contains(envelope);
    }
}
