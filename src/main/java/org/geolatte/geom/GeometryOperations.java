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

import org.geolatte.geom.codec.Bytes;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 5/3/11
 */
public interface GeometryOperations {

    GeometryOperation<Boolean> createIsSimpleOp(Geometry geometry);

    GeometryOperation<Geometry> createBoundaryOp(Geometry geometry);

    GeometryOperation<Geometry> createEnvelopeOp(Geometry geometry);

    GeometryOperation<Boolean> createDisjointOp(Geometry geometry, Geometry other);

    GeometryOperation<Boolean> createIntersectsOp(Geometry geometry, Geometry other);

    GeometryOperation<Boolean> createTouchesOp(Geometry geometry, Geometry other);

    GeometryOperation<Boolean> createCrossesOp(Geometry geometry, Geometry other);

    GeometryOperation<Boolean> createWithinOp(Geometry geometry, Geometry other);

    GeometryOperation<Boolean> createContainsOp(Geometry geometry, Geometry other);

    GeometryOperation<Boolean> createOverlaps(Geometry geometry, Geometry other);

    GeometryOperation<Boolean> createRelateOp(Geometry geometry, Geometry other);

    GeometryOperation<Geometry> createLocateAlongOp(Geometry geometry, double mValue);

    GeometryOperation<Geometry> createLocateBetween(Geometry geometry, double mStart, double mEnd);

    GeometryOperation<Double> createDistanceOp(Geometry geometry, Geometry other);

    GeometryOperation<Geometry> createBufferOp(Geometry geometry, double distance);

    GeometryOperation<Geometry> createConvexHull(Geometry geometry);

    GeometryOperation<Geometry> createIntersectionOp(Geometry geometry, Geometry other);

    GeometryOperation<Geometry> createUnionOp(Geometry geometry, Geometry other);

    GeometryOperation<Geometry> createDifferenceOp(Geometry geometry, Geometry other);

    GeometryOperation<Geometry> createSymDifferenceOp(Geometry geometry, Geometry other);

    GeometryOperation<String> createToWKTOp(Geometry geometry);

    GeometryOperation<Bytes> createToWKBOp(Geometry geometry);
}
