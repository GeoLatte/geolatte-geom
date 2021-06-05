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

import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;

import static org.geolatte.geom.GeometryOperations.projectedGeometryOperations;
import static org.geolatte.geom.jts.JTS.to;
import static org.junit.Assert.assertTrue;

public class ContainsTest {

    @Test
    public void containsTest() {
        Geometry<G2D> geometry = Wkt.fromWkt(
            "GEOMETRYCOLLECTION (POLYGON ((3.1906056404113774 51.30992397332947, 3.1891036033630376 51.314712289838546, 3.1850695610046387 51.31436357777655, 3.1848120689392094 51.308743583244755, 3.1906056404113774 51.30992397332947)))",
            CoordinateReferenceSystems.WGS84
        );
        Point<G2D> point = new Point<G2D>(new G2D(3.186939, 51.309727), CoordinateReferenceSystems.WGS84);

        Geometry<C2D> projectedGeometry = JTS.from(JTS.to(geometry), CoordinateReferenceSystems.WEB_MERCATOR);
        Point<C2D> projectedPoint = (Point<C2D>) JTS.from(to(point), CoordinateReferenceSystems.WEB_MERCATOR);
        boolean containsPoint = projectedGeometryOperations().contains(projectedGeometry, projectedPoint);
        assertTrue("Should contain the point", containsPoint);
    }

}
