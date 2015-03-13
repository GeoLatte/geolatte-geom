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

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.geolatte.geom.jts.JTS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/8/11
 */
public class MultiPointTest {

    MultiPoint<G2D> pnt1;
    MultiPoint<G2D> pnt2;
    Geographic2DCoordinateReferenceSystem wgs84 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);

    @Before
    public void setUp(){

        this.pnt1 = new MultiPoint<G2D>(createPointsAllDifferent(5, wgs84));
        this.pnt2 = new MultiPoint<G2D>(createPointsNotAllDifferent(5, wgs84));
    }

    @Test
    public void testAsJTS() throws Exception {
        com.vividsolutions.jts.geom.Geometry jtsMP = JTS.to(pnt1);
        assertEquals(jtsMP.getNumGeometries(), pnt1.getNumGeometries());
        for (int i = 0; i < pnt1.getNumGeometries(); i++) {
            assertEquals(jtsMP.getGeometryN(i).getCoordinate().x, pnt1.getGeometryN(i).getPosition().getCoordinate(0), Math.ulp(100));
            assertEquals(jtsMP.getGeometryN(i).getCoordinate().y, pnt1.getGeometryN(i).getPosition().getCoordinate(1), Math.ulp(100));
        }
    }

    @Test
    public void testGetDimension() throws Exception {
        assertEquals(0, pnt1.getDimension());

    }

    @Test
    public void testGetGeometryType() throws Exception {
        Assert.assertEquals(GeometryType.MULTIPOINT, pnt1.getGeometryType());

    }

    @SuppressWarnings("unchecked")
    private Point<G2D>[] createPointsNotAllDifferent(int size, CoordinateReferenceSystem<G2D> crs) {
        if (size < 4 ) throw new IllegalArgumentException("Size must be at least 4");
        Point<G2D>[] points = (Point<G2D>[])new Point[size];
        for (int i = 0; i < size; i++) {
            points[i] = point(crs,g(i,i));
        }
        points[0] = points[size -1];
        return points;
    }

    @SuppressWarnings("unchecked")
    private Point<G2D>[] createPointsAllDifferent(int size, CoordinateReferenceSystem<G2D> crs) {
        Point<G2D>[] points = (Point<G2D>[])new Point[size];
        for (int i = 0; i < size; i++) {
            points[i] = point(crs, g(i, i));
        }
        return points;
    }
}
