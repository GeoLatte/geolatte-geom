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
import org.geolatte.geom.C2D;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A context for the calculation of Morton codes.
 * <p/>
 * <p>This class holds the the maximum spatial extent and tree-depth of the implied
 * QuadTree-index for a {@code MortonCode}.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/13
 */
public class MortonContext<P extends C2D> {

    final private static String ERR_MSG_MAX_DEPTH = "Max. depth is limited to " + (Integer.SIZE - 1);
    final private static String ERR_MSG_NULL = "No Null arguments allowed.";
    final private int depth;
    final private Envelope<P> extent;
    final private int numOfDivisionsAlongAxis;
    final private double leafWidth;
    final private double leafHeight;

    /**
     * Constructs a {@code MortonContext} with the specified extent and max. tree-depth
     *
     * @param extent the extent for this instance
     * @param depth  the tree-depth
     */
    public MortonContext(Envelope<P> extent, int depth) {
        if (extent == null) {
            throw new IllegalArgumentException(ERR_MSG_NULL);
        }

        if (depth >= Integer.SIZE) {
            throw new IllegalArgumentException(ERR_MSG_MAX_DEPTH);
        }
        this.depth = depth;
        this.extent = extent;

        this.numOfDivisionsAlongAxis = (int) (Math.pow(2, depth));
        this.leafWidth =  (extent.extentAlongDimension(0)) / numOfDivisionsAlongAxis;
        this.leafHeight = (extent.extentAlongDimension(1)) / numOfDivisionsAlongAxis;

    }

    /**
     * Returns the maximum X-coordinate of the extent.
     *
     * @return the maximum X-coordinate of the extent
     */
    public double getMaxX() {
        return extent.upperRight().getCoordinate(0);
    }

    /**
     * Returns the maximum Y-coordinate of the extent.
     *
     * @return the maximum Y-coordinate of the extent
     */
    public double getMaxY() {
        return extent.upperRight().getCoordinate(1);
    }

    /**
     * Returns the minimum X-coordinate of the extent
     *
     * @return the minimum X-coordinate of the extent
     */
    public double getMinX() {
        return extent.lowerLeft().getCoordinate(0);
    }
    /**
     * Returns the minimum Y-coordinate of the extent
     *
     * @return the minimum Y-coordinate of the extent
     */
    public double getMinY() {
        return extent.lowerLeft().getCoordinate(1);
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
    public CoordinateReferenceSystem<?> getCoordinateReferenceSystem() {
        return extent.getCoordinateReferenceSystem();
    }

    /**
     * Returns the number of subdivisions along an (X- or Y-)axis.
     *
     * <p>The leaves of a Quadtree form a grid-cell structure that completely
     * covers the extent. This method returns the number of subdivisions along
     * a single axis. It is the square root of the total number of leaves.</p>
     *
     * @return the number of subdivisions along an (X- or Y-)axis.
     */
    int getNumberOfDivisionsAlongAxis() {
        return this.numOfDivisionsAlongAxis;
    }

    /**
     * Returns the width of the extent of a leaf in the QuadTree at tree-depth (lowest-level)
     *
     * @return the width of the extent of a leaf in the QuadTree at tree-depth (lowest-level)
     */
    double getLeafWidth(){
        return this.leafWidth;
    }

    /**
     * Returns the height of the extent of a leaf in the QuadTree at tree-depth (lowest-level)
     *
     * @return the height of the extent of a leaf in the QuadTree at tree-depth (lowest-level)
     */
    double getLeafHeight(){
        return this.leafHeight;
    }

    /**
     * Checks whether the specified {@code Point} is contained in the extent.
     *
     * @param pos a {@code Point} value
     * @return true if the specified point is contained in the extent, false otherwise
     * @throws IllegalArgumentException if the specified point does not share this extent's {@code CrsId}
     */
    public boolean extentContains(P pos) {
        return this.extent.contains(pos);
    }

    /**
     * Checks whether the specified {@code Envelope} is contained in the extent.
     *
     * @param envelope an {@code Envelope} value
     * @return true if the specified envelope is contained in the extent, false otherwise
     * @throws IllegalArgumentException if the specified envelope does not share this extent's {@code CrsId}
     */
    public boolean extentContains(Envelope<P> envelope) {
        return this.extent.contains(envelope);
    }

    /**
     * Returns the spatial extent of this instance
     * @return an {@code Envelope} representing the spatial extent of this instance
     */
    public Envelope<P> getExtent() {
        return extent;
    }
}
