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
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

/**
 * A <code>GeometryEquality</code> that considers two <code>Geometry</code>s to be equal iff one of these conditions is
 * satisfied:
 * <ul>
 *     <li> both have the same type, coordinate reference system and consist of the same <code>Point</code>s.</li>
 *     <li> both are empty</li>
 * </ul>
 *
 * <p>Point equality is determined by the {@link PositionEquality} instance that is passed to the constructor. In
 * case of the default no-args constructor, {@link ExactPositionEquality} is used.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/25/12
 */
public class GeometryPointEquality implements GeometryEquality {


    private final PositionSequenceEquality pointSeqEq;

    /**
     * Constructs an instance that uses the specified <code>PositionEquality</code> to determine
     * whether the <code>Geometry</code>s have the same <code>Positions</code>s.
     *
     * @param pointEq
     */
    public GeometryPointEquality(PositionEquality pointEq) {
        this.pointSeqEq = new PositionSequencePositionEquality(pointEq);
    }

/**
     * Constructs an instance that uses {@code ExactPositionEquality} to determine
     * whether the <code>Geometry</code>s have the same <code>Positions</code>s.
     */
    public GeometryPointEquality() {
        ExactPositionEquality pointEquality = new ExactPositionEquality();
        this.pointSeqEq = new PositionSequencePositionEquality(pointEquality);
    }




    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends Position> boolean equals(Geometry<P> first, Geometry<P> second) {
        if (first == second) return true;
        if (first == null || second == null) return false;
        if (first.isEmpty() && second.isEmpty()) return true;
        if (first.isEmpty() || second.isEmpty()) return false;
        if (! first.getCoordinateReferenceSystem().equals(second.getCoordinateReferenceSystem())) return false;
        if (first.getGeometryType() != second.getGeometryType()) return false;
        if (first instanceof GeometryCollection) {
            assert(second instanceof GeometryCollection);
            return equals((GeometryCollection<?,?>)first, (GeometryCollection<?,?>)second);
        }
        if (first instanceof Polygon) {
            assert(second instanceof Polygon);
            return equals((Polygon<P>)first, (Polygon<P>)second);
        }
        return pointSeqEq.equals(first.getPositions(), second.getPositions());
    }

    private <P extends Position> boolean equals (Polygon<P> first, Polygon<P> second){
        if (first.getNumInteriorRing() != second.getNumInteriorRing()) return false;
        if (!equals(first.getExteriorRing(), second.getExteriorRing())) return false;
        for (int i = 0; i < first.getNumInteriorRing(); i++) {
            if (!equals(first.getInteriorRingN(i), second.getInteriorRingN(i))) return false;
        }
        return true;
    }

    private boolean equals(GeometryCollection first, GeometryCollection second) {
        if (first.getNumGeometries() != second.getNumGeometries()) return false;
        for (int i = 0; i < first.getNumGeometries(); i++) {
            if (! equals(first.getGeometryN(i), second.getGeometryN(i))) return false;
        }
        return true;
    }
}
