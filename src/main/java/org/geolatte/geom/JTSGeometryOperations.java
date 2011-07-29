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

import com.vividsolutions.jts.operation.BoundaryOp;
import com.vividsolutions.jts.operation.IsSimpleOp;
import org.geolatte.geom.codec.Bytes;
import org.geolatte.geom.jts.JTS;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 5/3/11
 */
public class JTSGeometryOperations implements GeometryOperations {


    @Override
    public GeometryOperation<Boolean> createIsSimpleOp(Geometry geometry) {
        final IsSimpleOp isSimpleOp = new IsSimpleOp(JTS.to(geometry));
        return new GeometryOperation<Boolean>(){
            @Override
            public Boolean execute() {
                return isSimpleOp.isSimple();
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createBoundaryOp(Geometry geometry) {
        final BoundaryOp boundaryOp = new BoundaryOp(JTS.to(geometry));
        final int SRID = geometry.getSRID();
        return new GeometryOperation<Geometry>(){
            @Override
            public Geometry execute() {
                return JTS.from(boundaryOp.getBoundary(), SRID);
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createEnvelopeOp(Geometry geometry) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Boolean> createDisjointOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Boolean> createIntersectsOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Boolean> createTouchesOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Boolean> createCrossesOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Boolean> createWithinOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Boolean> createContainsOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Boolean> createOverlaps(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Boolean> createRelateOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Geometry> createLocateAlongOp(Geometry geometry, double mValue) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Geometry> createLocateBetween(Geometry geometry, double mStart, double mEnd) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Double> createDistanceOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Geometry> createBufferOp(Geometry geometry, double distance) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Geometry> createConvexHull(Geometry geometry) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Geometry> createIntersectionOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Geometry> createUnionOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Geometry> createDifferenceOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Geometry> createSymDifferenceOp(Geometry geometry, Geometry other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<String> createToWKTOp(Geometry geometry) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Bytes> createToWKBOp(Geometry geometry) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
