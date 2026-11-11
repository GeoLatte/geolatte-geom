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
 * Copyright (C) 2010 - 2026 and Ownership of code is shared by:
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.cga.Relate;

/**
 * The default implementation of {@code ProjectedGeometryOperations}.
 *
 * <p>The spatial predicates ({@code intersects}, {@code disjoint}, {@code touches}, {@code crosses},
 * {@code within}, {@code contains}, {@code overlaps}, {@code covers}, {@code coveredBy}, {@code equalsTopologically}
 * and {@code relates}) are computed exactly with {@link Relate}: the result is exact for the given coordinates, and
 * geometry collections have union semantics. The other operations are delegated to JTS, as in
 * {@link JTSGeometryOperations}.</p>
 *
 * <p>As in {@link JTSGeometryOperations}, a predicate with an empty operand is false, except {@code disjoint},
 * which is the negation of {@code intersects}, and {@code equalsTopologically}, which is true for two empty
 * geometries. The operands must have the same coordinate reference system.</p>
 */
public class DefaultProjectedGeometryOperations extends JTSGeometryOperations {

    private static void checkCompatibleCRS(Geometry<?> geometry, Geometry<?> other) {
        if (!geometry.getCoordinateReferenceSystem().equals(other.getCoordinateReferenceSystem())) {
            throw new IllegalArgumentException("Geometries have different CRS's");
        }
    }

    // checks the operands; true if the predicate is to be computed, false if an operand is empty
    private static boolean nonEmpty(Geometry<?> geometry, Geometry<?> other) {
        if (geometry.isEmpty() || other.isEmpty()) return false;
        checkCompatibleCRS(geometry, other);
        return true;
    }

    @Override
    public <P extends C2D> boolean intersects(final Geometry<P> geometry, final Geometry<P> other) {
        return nonEmpty(geometry, other) && Relate.intersects(geometry, other);
    }

    @Override
    public <P extends C2D> boolean disjoint(final Geometry<P> geometry, final Geometry<P> other) {
        return !intersects(geometry, other);
    }

    @Override
    public <P extends C2D> boolean touches(final Geometry<P> geometry, final Geometry<P> other) {
        return nonEmpty(geometry, other) && Relate.touches(geometry, other);
    }

    @Override
    public <P extends C2D> boolean crosses(final Geometry<P> geometry, final Geometry<P> other) {
        return nonEmpty(geometry, other) && Relate.crosses(geometry, other);
    }

    @Override
    public <P extends C2D> boolean within(final Geometry<P> geometry, final Geometry<P> other) {
        return nonEmpty(geometry, other) && Relate.within(geometry, other);
    }

    @Override
    public <P extends C2D> boolean contains(final Geometry<P> geometry, final Geometry<P> other) {
        return nonEmpty(geometry, other) && Relate.contains(geometry, other);
    }

    @Override
    public <P extends C2D> boolean overlaps(final Geometry<P> geometry, final Geometry<P> other) {
        return nonEmpty(geometry, other) && Relate.overlaps(geometry, other);
    }

    @Override
    public <P extends C2D> boolean covers(final Geometry<P> geometry, final Geometry<P> other) {
        return nonEmpty(geometry, other) && Relate.covers(geometry, other);
    }

    @Override
    public <P extends C2D> boolean coveredBy(final Geometry<P> geometry, final Geometry<P> other) {
        return nonEmpty(geometry, other) && Relate.coveredBy(geometry, other);
    }

    @Override
    public <P extends C2D> boolean equalsTopologically(final Geometry<P> geometry, final Geometry<P> other) {
        if (geometry.isEmpty() || other.isEmpty()) return geometry.isEmpty() && other.isEmpty();
        checkCompatibleCRS(geometry, other);
        return Relate.equalsTopologically(geometry, other);
    }

    @Override
    public <P extends C2D> boolean relates(final Geometry<P> geometry, final Geometry<P> other, final String matrix) {
        return nonEmpty(geometry, other) && Relate.relate(geometry, other, matrix);
    }
}
