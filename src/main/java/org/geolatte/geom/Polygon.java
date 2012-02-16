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

import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.jts.JTS;

import java.util.Iterator;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/14/11
 */
public class Polygon extends Geometry implements Iterable<LinearRing>{


    private final PointSequence points;
    private final LinearRing[] rings;
    public static final Polygon EMPTY = new Polygon(new LinearRing[0]);

    public static Polygon createEmpty() {
        return EMPTY;
    }

    public Polygon(PointSequence pointSequence, CrsId crsId, GeometryOperations ops){
        this(new LinearRing[]{new LinearRing(pointSequence, crsId, ops)});
    }

    public Polygon(PointSequence pointSequence, CrsId crsId){
        this(new LinearRing[]{new LinearRing(pointSequence, crsId, null)});
    }

    public Polygon(LinearRing[] rings) {
        super(getCrsId(rings), getGeometryOperations(rings));
        checkRings(rings);
        points = createAndCheckPointSequence(rings);
        this.rings = rings;
    }


    private void checkRings(LinearRing[] rings) {
        for (LinearRing ring : rings){
            checkLinearRing(ring);
        }
    }

    private void checkLinearRing(LinearRing ring) {
        if (ring == null) throw new IllegalArgumentException("NULL linear ring is not valid.");
        if (ring.isEmpty()) throw new IllegalArgumentException("Empty linear ring is not valid.");
    }

    @Override
    public PointSequence getPoints() {
        return this.points;
    }

    public LinearRing getExteriorRing() {
        return this.isEmpty() ?
                LinearRing.EMPTY :
                this.rings[0];
    }

    public int getNumInteriorRing() {
        return this.isEmpty() ?
                0 :
                this.rings.length - 1;
    }

    public LinearRing getInteriorRingN(int i) {
        return this.rings[i+1];
    }

    public double getArea() {
        return JTS.to(this).getArea();
    }

    public Point getCentroid() {
        return (Point) JTS.from(JTS.to(this).getCentroid());
    }

    public Point getPointOnSurface() {
        return getPointN(0);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POLYGON;
    }

    @Override
    public MultiLineString getBoundary() {

        return isEmpty()?
                MultiLineString.EMPTY :
                new MultiLineString(rings);
    }


    public Iterator<LinearRing> iterator(){
        return new Iterator<LinearRing>(){
            private int index = 0;
            @Override
            public boolean hasNext() {
                return index < rings.length;
            }

            @Override
            public LinearRing next() {
                return rings[index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void accept(GeometryVisitor visitor) {
        visitor.visit(this);
    }
}
