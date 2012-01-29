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

/**
 * A Factory for <code>Geometry</code>s.
 * <p/>
 * <p>This factory is provided as a convenience. It ensures that a set of <code>Geometry</code>s are
 * created with the same spatial reference system and <code>GeometryOperations</code> implementation.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/27/11
 */
public class GeometryFactory {

    final private CrsId crsId;
    final private GeometryOperations geometryOperations;

    /**
     * Creates a <code>GeometryFactory</code> that creates <code>Geometry</code>s
     * with the specified <code>CrsId</code> and <code>GeometryOperations</code>.
     *
     * @param crsId              the spatial reference identifier
     * @param geometryOperations the <code>GeometryOperations</code> implementation
     */
    public GeometryFactory(CrsId crsId, GeometryOperations geometryOperations) {
        this.crsId = (crsId == null ? CrsId.UNDEFINED : crsId);
        this.geometryOperations = (geometryOperations == null ? new JTSGeometryOperations() : geometryOperations);
    }

    /**
     * Creates a <code>GeometryFactory</code> that creates <code>Geometry</code>s
     * with the specified <code>CrsId</code> and the default <code>GeometryOperations</code>
     * implementation.
     *
     * @param crsId the spatial reference identifier
     */
    public GeometryFactory(CrsId crsId) {
        this(crsId, null);
    }

    /**
     * Creates a <code>GeometryFactory</code> that creates <code>Geometry</code>s
     * with the default <code>CrsId</code> (i.e. UNDEFINED) and the default <code>GeometryOperations</code>
     * implementation.
     */
    public GeometryFactory() {
        this(null, null);
    }

    /**
     * Creates a <code>Point</code>.
     *
     * @param points the coordinates of the point
     * @return the <code>Point</code> with the specified coordinates
     */
    public Point createPoint(PointSequence points) {
        return new Point(points, this.crsId, this.geometryOperations);
    }

    /**
     * Creates a <code>LineString</code>
     *
     * @param points the points of the linestring
     * @return
     */
    public LineString createLineString(PointSequence points) {
        return LineString.create(points, crsId, geometryOperations);
    }

    /**
     * Creates a <code>LinearRing</code>
     *
     * @param points the points of the ring
     * @return a <code>LinearRing</code> with the specified points.
     * @throws IllegalArgumentException if the input <code>PointSequence</code> does not form a closed ring.
     */
    public LinearRing createLinearRing(PointSequence points) {
        return LinearRing.create(points, crsId, geometryOperations);
    }

    /**
     * Creates a <code>Polygon</code> without any holes.
     *
     * @param points the points of the outer ring.
     * @return
     */
    public Polygon createPolygon(PointSequence points) {
        return Polygon.create(points, this.crsId, this.geometryOperations);
    }

    /**
     * Creates a  <code>Polygon</code> .
     * <p/>
     * <p>The first <code>LinearRing</code> in the rings parameter is the outer
     * ring (or shell) of this <code>Polygon</code>. The other rings determine
     * holes within it.</p>
     *
     * @param rings the <code>LinearRings</code> that defined this <code>Polygon</code>.
     * @return
     */
    public Polygon createPolygon(LinearRing[] rings) {
        return Polygon.create(rings, this.crsId, this.geometryOperations);
    }

    /**
     * Creates a <code>GeometryCollection</code> from the specified <code>Geometry</code>s.
     *
     * @param geometries the constituent <code>Geometry</code>s for the new <code>GeometryCollection</code>.     *
     * @return
     */
    public GeometryCollection createGeometryCollection(Geometry[] geometries) {
        return GeometryCollection.create(geometries, crsId, geometryOperations);
    }

    /**
     * Creates a <code>MultiPoint</code> from the specified <code>Point</code>s.
     *
     * @param points the constituent <code>Point</code>s of the new <code>MultiPoint</code>.
     * @return
     */
    public MultiPoint createMultiPoint(Point[] points) {
        return MultiPoint.create(points, crsId, geometryOperations);
    }

    /**
     * Creates a <code>MultiLineString</code> from the specified <code>LineString</code>s.
     *
     * @param lineStrings the constituent <code>LineString</code>s of the new <code>MultiLineString</code>.
     * @return
     */
    public MultiLineString createMultiLineString(LineString[] lineStrings) {
        return MultiLineString.create(lineStrings, crsId, geometryOperations);
    }

    /**
     * Creates a <code>MultiPolygon</code> from the specified <code>Polygon</code>s.
     *
     * @param polygons the constituent <code>Polygon</code>s of the new <code>MultiPolygon</code>.
     * @return
     */
    public MultiPolygon createMultiPolygon(Polygon[] polygons) {
        return MultiPolygon.create(polygons, crsId, geometryOperations);
    }




}
