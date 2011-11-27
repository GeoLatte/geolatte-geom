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
    public static final Polygon EMPTY = new Polygon(new LinearRing[0], CrsId.UNDEFINED);

    public static Polygon create(LinearRing[] rings, CrsId crsId) {
        return new Polygon(rings, crsId);
    }

    public static Polygon create(PointSequence points, CrsId crsId){
        return new Polygon(new LinearRing[]{LinearRing.create(points, crsId)}, crsId);
    }

    public static Polygon createEmpty() {
        return EMPTY;
    }

    protected Polygon(LinearRing[] rings, CrsId SRID, GeometryOperations geometryOperations) {
        super(SRID, geometryOperations);
        checkRings(rings);
        points = createAndCheckPointSequence(rings);
        this.rings = rings;
    }

    protected Polygon(LinearRing[] rings, CrsId crsId) {
        this(rings, crsId, null);
    }

    private void checkRings(LineString[] holes) {
        for (LineString hole : holes){
            checkLinearRing(hole);
        }
    }

    private void checkLinearRing(LineString ring) {
        if (ring == null || ring.getGeometryType() != GeometryType.LINEAR_RING) throw new IllegalArgumentException("Invalid linear ring specified.");
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
                MultiLineString.create(rings, rings[0].getCrsId());
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
