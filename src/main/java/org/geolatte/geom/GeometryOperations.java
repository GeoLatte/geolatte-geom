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

/**
 * A factory for <code>GeometryOperation</code>s.
 *
 * <p>The semantics of the operations is as specified in
 * <a href="http://portal.opengeospatial.org/files/?artifact_id=25355">Simple Feature Access - Part 1: common architecture</a>
 * </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *
 */
public interface GeometryOperations {

    GeometryOperation<Boolean> createIsSimpleOp(final Geometry geometry);

    GeometryOperation<Geometry> createBoundaryOp(final Geometry geometry);

    GeometryOperation<Envelope> createEnvelopeOp(final Geometry geometry);

    GeometryOperation<Boolean> createIntersectsOp(final Geometry geometry, final Geometry other);

    GeometryOperation<Boolean> createTouchesOp(final Geometry geometry, final Geometry other);

    GeometryOperation<Boolean> createCrossesOp(final Geometry geometry, final Geometry other);

    GeometryOperation<Boolean> createContainsOp(final Geometry geometry, final Geometry other);

    GeometryOperation<Boolean> createOverlapsOp(final Geometry geometry, final Geometry other);

    GeometryOperation<Boolean> createRelateOp(final Geometry geometry, final Geometry other, final String matrix);

    GeometryOperation<Geometry> createLocateAlongOp(final Geometry geometry, final double mValue);

    GeometryOperation<Geometry> createLocateBetweenOp(final Geometry geometry, final double startMeasure, final double endMeasure);

    GeometryOperation<Double> createDistanceOp(final Geometry geometry, final Geometry other);

    GeometryOperation<Geometry> createBufferOp(final Geometry geometry, final double distance);

    GeometryOperation<Geometry> createConvexHullOp(final Geometry geometry);

    GeometryOperation<Geometry> createIntersectionOp(final Geometry geometry, final Geometry other);

    GeometryOperation<Geometry> createUnionOp(final Geometry geometry, final Geometry other);

    GeometryOperation<Geometry> createDifferenceOp(final Geometry geometry, final Geometry other);

    GeometryOperation<Geometry> createSymDifferenceOp(final Geometry geometry, final Geometry other);

    GeometryOperation<String> createToWktOp(final Geometry geometry);

    GeometryOperation<ByteBuffer> createToWkbOp(final Geometry geometry);
}
