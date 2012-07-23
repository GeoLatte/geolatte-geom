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

import java.util.Iterator;

/**
 * Turns a <code>PointSequence</code> into an <code>Iterable</code> over the
 * <code>LineSegments</code> defined by each consecutive pair of <code>Point</code>s in the <code>PointSequence</code>.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/25/11
 */
public class LineSegments implements Iterable<LineSegment> {

    private final PointSequence points;

    public LineSegments(PointSequence pointSequence) {
        this.points = pointSequence;
    }

    @Override
    public Iterator<LineSegment> iterator() {

        return new Iterator<LineSegment>() {

            final Iterator<Point> iterator;
            Point startPoint;

            {
                iterator = points.iterator();
                if (iterator.hasNext()) {
                    startPoint = iterator.next();
                }
            }

            @Override
            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            @Override
            public LineSegment next() {
                Point endPoint = iterator.next();
                LineSegment ls = new LineSegment(startPoint, endPoint);
                startPoint = endPoint;
                return ls;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
