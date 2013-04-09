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

package org.geolatte.geom.support;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CrsId;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 10/13/12
 */
public class PostgisJDBCWithSRIDTestInputs extends WktWkbCodecTestBase {

    final CrsId crsId = CrsId.valueOf(4326);

    public PostgisJDBCWithSRIDTestInputs(){
        PostgisJDBCUnitTestInputs base = new PostgisJDBCUnitTestInputs();
        for (Integer testCase : base.getCases()) {
            addCase(testCase,
                    "SRID=4326;" + base.getWKT(testCase),
                    toSRIDPrefixedWKB(base, testCase),
                    addCrsId(base, testCase),
                    false
            );
        }
    }

    private Geometry addCrsId(PostgisJDBCUnitTestInputs base, Integer testCase) {
        Geometry geom = base.getExpected(testCase);
        if (geom.isEmpty()) return geom;
        return addCrsId(geom);

    }

    private Geometry addCrsId(Geometry geom) {
        if (geom instanceof Point) {
            Point pnt = (Point) geom;
            return new Point(forceCrsId(pnt.getPoints()));
        } else if (geom instanceof LinearRing) {
            LinearRing lr = (LinearRing)geom;
            return new LinearRing(forceCrsId(lr.getPoints()));
        } else if (geom instanceof LineString) {
            LineString ls = (LineString)geom;
            return new LineString(forceCrsId(ls.getPoints()));
        } else if (geom instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint)geom;
            Point[] points = new Point[mp.getNumGeometries()];
            addCrsId(points, mp);
            return new MultiPoint(points);
        } else if (geom instanceof Polygon) {
            Polygon pg = (Polygon)geom;
            LinearRing[] rings = new LinearRing[pg.getNumInteriorRing() + 1];
            addCrsId(rings, pg);
            return new Polygon(rings);
        } else if (geom instanceof MultiPolygon) {
            MultiPolygon mpg = (MultiPolygon)geom;
            Polygon[] polygons = new Polygon[mpg.getNumGeometries()];
            addCrsId(polygons, mpg);
            return new MultiPolygon(polygons);
        } else if (geom instanceof MultiLineString) {
            MultiLineString mls = (MultiLineString)geom;
            LineString[] ls = new LineString[mls.getNumGeometries()];
            addCrsId(ls, mls);
            return new MultiLineString(ls);
        }  else if (geom instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection)geom;
            Geometry[] parts = new Geometry[gc.getNumGeometries()];
            addCrsId(parts, gc);
            return new GeometryCollection(parts);
        }
        return geom;
    }

    private PointSequence forceCrsId(PointSequence pointSequence) {
        PointSequenceBuilder builder = PointSequenceBuilders.fixedSized(pointSequence.size(), pointSequence.getDimensionalFlag(), crsId);
        for (Point pnt : pointSequence) {
            builder.add(pnt);
        }
        return builder.toPointSequence();
    }

    private void addCrsId(Geometry[] result, GeometryCollection source) {
        for (int i = 0; i < result.length; i++) {
            result[i] = addCrsId(source.getGeometryN(i));
        }
    }

    private void addCrsId(Geometry[] result, Polygon source) {
        result[0] = addCrsId(source.getExteriorRing());
        for (int i = 1; i < result.length; i++) {
            result[i] = addCrsId(source.getInteriorRingN(i - 1));
        }
    }

    private String toSRIDPrefixedWKB(PostgisJDBCUnitTestInputs base, Integer testCase) {
        if (base.getExpected(testCase).isEmpty()) {
            return base.getWKBHexString(testCase);
        }
        String hexBase = base.getWKBHexString(testCase);
        ByteBuffer inBuffer = ByteBuffer.from(hexBase);
        //get the relevant parts
        inBuffer.setByteOrder(ByteOrder.NDR);
        byte bo = inBuffer.get();
        int type = inBuffer.getInt();
        byte[] bytes = inBuffer.toByteArray();

        //calculate the output size
        int outputSize = 4 + bytes.length;
        ByteBuffer outBuffer = ByteBuffer.allocate(outputSize);

        outBuffer.setByteOrder(ByteOrder.NDR);
        outBuffer.put(bo);
        type |= 0x20000000; // OR with the SRID-flag
        outBuffer.putInt(type);
        //write the srid
        outBuffer.putInt(4326);
        for (int i = 5; i < bytes.length; i++) {
            outBuffer.put(bytes[i]);
        }
        return outBuffer.toString();
    }

}
