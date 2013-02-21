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
 *
 * <p>This class holds the the maximum spatial extent and tree-depth of the implied
 * QuadTree-index for a <code>MortonCode</code>.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/13
 */
public class MortonContext {

    final private static String ERR_MSG_MAX_DEPTH = "Max. depth is limited to 31.";
    final private static String ERR_MSG_NULL = "No Null arguments allowed.";

    final private int depth;
    final private Envelope extent;

    /**
     * Constructs a <code>MortonContext</code> with the specified extent and max. tree depth
     *
     * @param extent the extent for this instance
     * @param depth the tree-depth
     */
    public MortonContext(Envelope extent, int depth){
        if (extent == null) {
            throw new IllegalArgumentException(ERR_MSG_NULL);
        }

        if (depth >= Integer.SIZE) {
            throw new IllegalArgumentException(ERR_MSG_MAX_DEPTH);
        }
        this.depth = depth;
        this.extent = extent;
    }

    /**
     * Returns the Maximum
     * @return
     */
    public double getMaxX() {
        return extent.getMaxX();
    }

    public double getMaxY() {
        return extent.getMaxY();
    }

    public double getMinX() {
        return extent.getMinX();
    }

    public double getMinY() {
        return extent.getMinY();
    }

    public int getDepth() {
        return depth;
    }

    public CrsId getCrsId() {
        return extent.getCrsId();
    }

    int getMaxGridNum() {
        return (int) Math.pow(2, depth) - 1;
    }

    public boolean extentContains(Point pnt) {
        return this.extent.contains(pnt);
    }

    public boolean extentContains(Envelope envelope) {
        return this.extent.contains(envelope);
    }
}
