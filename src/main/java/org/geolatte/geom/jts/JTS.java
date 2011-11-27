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

package org.geolatte.geom.jts;

import com.vividsolutions.jts.geom.*;
import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.PointSequence;
import org.geolatte.geom.PointSequenceBuilder;
import org.geolatte.geom.PointSequenceBuilderFactory;
import org.geolatte.geom.crs.CrsId;

/**
 * @author Karel Maesen, Geovise BVBA, 2011 (original code)
 * @author Yves Vandewoude, Qmino bvba, 2011 (bugfixes)
 */
public class JTS {

    private static final GeometryFactory jtsGeometryFactory;

    static {
        jtsGeometryFactory = new GeometryFactory(new PointSequenceCoordinateSequenceFactory());
    }

    public static GeometryFactory geometryFactory() {
        return jtsGeometryFactory;
    }

    /**
     * Primary Factory method that converts a JTS geometry into an equivalent geolatte geometry
     * @param jtsGeometry the jts geometry to convert
     * @return an equivalent geolatte geometry
     */
    public static org.geolatte.geom.Geometry from(com.vividsolutions.jts.geom.Geometry jtsGeometry) {
        return from(jtsGeometry,  CrsId.valueOf(jtsGeometry.getSRID()));
    }

    /**
     * Factory method that converts a JTS geometry into an equivalent geolatte geometry and allows the caller to
     * specify the srid value of the resulting geolatte geometry.
     *
     * @param jtsGeometry the jtsGeometry
     * @param crsId
     * @return A geolatte geometry that corresponds with the given JTS geometry
     */
    public static org.geolatte.geom.Geometry from(Geometry jtsGeometry, CrsId crsId) {
        if (jtsGeometry instanceof Point) {
            return from((Point) jtsGeometry, crsId);
        } else if (jtsGeometry instanceof LineString) {
            return from((LineString) jtsGeometry, crsId);
        } else if (jtsGeometry instanceof Polygon) {
            return from((Polygon) jtsGeometry, crsId);
        } else if (jtsGeometry instanceof MultiPoint) {
            return from((MultiPoint) jtsGeometry, crsId);
        } else if (jtsGeometry instanceof MultiLineString) {
            return from((MultiLineString) jtsGeometry, crsId);
        } else if (jtsGeometry instanceof MultiPolygon) {
            return from((MultiPolygon) jtsGeometry, crsId);
        } else if (jtsGeometry instanceof GeometryCollection) {
            return from((GeometryCollection) jtsGeometry, crsId);
        } else {
            throw new JTSConversionException();
        }
    }

    /**
     * Primary factory method that converts a geolatte geometry into an equivalent jts geometry
     * @param geometry the geolatte geometry to start from
     * @return the equivalent JTS geometry
     */
    public static com.vividsolutions.jts.geom.Geometry to(org.geolatte.geom.Geometry geometry) {
        if (geometry instanceof org.geolatte.geom.Point) {
            return to((org.geolatte.geom.Point) geometry);
        } else if (geometry instanceof org.geolatte.geom.LineString) {
            return to((org.geolatte.geom.LineString) geometry);
        } else if (geometry instanceof org.geolatte.geom.MultiPoint) {
            return to((org.geolatte.geom.MultiPoint) geometry);
        } else if (geometry instanceof org.geolatte.geom.Polygon) {
            return to((org.geolatte.geom.Polygon) geometry);
        } else if (geometry instanceof org.geolatte.geom.MultiLineString) {
            return to((org.geolatte.geom.MultiLineString) geometry);
        } else if (geometry instanceof org.geolatte.geom.MultiPolygon) {
            return to((org.geolatte.geom.MultiPolygon) geometry);
        } else if (geometry instanceof org.geolatte.geom.GeometryCollection) {
            return to((org.geolatte.geom.GeometryCollection) geometry);
        } else {
            throw new JTSConversionException();
        }
    }


    ///
    ///  Helpermethods: jts --> geolatte
    ///

    /*
     * Converts a jts multipolygon into a geolatte multipolygon
     */
    private static org.geolatte.geom.MultiPolygon from(MultiPolygon jtsGeometry, CrsId crsId) {
        org.geolatte.geom.Polygon[] polygons = new org.geolatte.geom.Polygon[jtsGeometry.getNumGeometries()];
        for (int i =0; i< jtsGeometry.getNumGeometries(); i++) {
            polygons[i] = from((Polygon) jtsGeometry.getGeometryN(i), crsId);
        }
        return org.geolatte.geom.MultiPolygon.create(polygons, crsId);
    }

    /*
     * Converts a jts polygon into a geolatte polygon
     */
    private static org.geolatte.geom.Polygon from(Polygon jtsGeometry, CrsId crsId) {
        org.geolatte.geom.LinearRing[] rings = new org.geolatte.geom.LinearRing[jtsGeometry.getNumInteriorRing() + 1];
        org.geolatte.geom.LineString extRing = from(jtsGeometry.getExteriorRing(), crsId);
        rings[0] = org.geolatte.geom.LinearRing.create(extRing.getPoints(), extRing.getCrsId());
        for (int i = 1; i < rings.length; i++) {
            org.geolatte.geom.LineString intRing = from(jtsGeometry.getInteriorRingN(i - 1), crsId);
            rings[i] = org.geolatte.geom.LinearRing.create(intRing.getPoints(), extRing.getCrsId());
        }
        return org.geolatte.geom.Polygon.create(rings, CrsId.valueOf(jtsGeometry.getSRID()));
    }

    /*
     * Converts a jts multilinestring into a geolatte multilinestring
     */
    private static org.geolatte.geom.MultiLineString from(MultiLineString jtsGeometry, CrsId crsId) {
        org.geolatte.geom.LineString[] linestrings = new org.geolatte.geom.LineString[jtsGeometry.getNumGeometries()];
        for (int i = 0; i < linestrings.length; i++) {
            linestrings[i] = from((LineString) jtsGeometry.getGeometryN(i), crsId);
        }
        return org.geolatte.geom.MultiLineString.create(linestrings, CrsId.valueOf(jtsGeometry.getSRID()));
    }

    /*
     * Converts a jts geometrycollection into a geolatte geometrycollection
     */
    private static org.geolatte.geom.GeometryCollection from(GeometryCollection jtsGeometry, CrsId crsId) {
        org.geolatte.geom.Geometry[] geoms = new org.geolatte.geom.Geometry[jtsGeometry.getNumGeometries()];
        for (int i=0; i < jtsGeometry.getNumGeometries(); i++) {
            geoms[i] = from(jtsGeometry.getGeometryN(i), crsId);
        }
        return org.geolatte.geom.GeometryCollection.create(geoms, crsId);
    }

    /*
     * Converts a jts linestring into a geolatte linestring
     */
    private static org.geolatte.geom.LineString from(LineString jtsLineString, CrsId crsId) {
        CoordinateSequence cs = jtsLineString.getCoordinateSequence();
        return org.geolatte.geom.LineString.create(toPointSequence(cs), crsId);

    }

    /*
     * Converts a jts multipoint into a geolatte multipoint
     */
    private static org.geolatte.geom.MultiPoint from(MultiPoint jtsMultiPoint, CrsId crsId) {
        if (jtsMultiPoint == null || jtsMultiPoint.getNumGeometries() == 0)
            return org.geolatte.geom.MultiPoint.createEmpty();
        org.geolatte.geom.Point[] points = new org.geolatte.geom.Point[jtsMultiPoint.getNumGeometries()];
        for (int i = 0; i < points.length; i++) {
            points[i] = from((Point) jtsMultiPoint.getGeometryN(i), crsId);
        }
        return org.geolatte.geom.MultiPoint.create(points, crsId);
    }

    /*
     * Converts a jts point into a geolatte point
     */
    private static org.geolatte.geom.Point from(com.vividsolutions.jts.geom.Point jtsPoint, CrsId crsId) {
        CoordinateSequence cs = jtsPoint.getCoordinateSequence();
        return org.geolatte.geom.Point.create(toPointSequence(cs), crsId);
    }

    ///
    ///  Helpermethods: geolatte --> jts
    ///

    private static Polygon to(org.geolatte.geom.Polygon polygon) {
        LinearRing shell = to(polygon.getExteriorRing());
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int i = 0; i < holes.length; i++) {
            holes[i] = to(polygon.getInteriorRingN(i));
        }
        Polygon pg = geometryFactory().createPolygon(shell, holes);
        pg.setSRID(polygon.getSRID());
        return pg;
    }

    private static Point to(org.geolatte.geom.Point point) {
        Point pnt = geometryFactory().createPoint(sequenceOf(point));
        pnt.setSRID(point.getSRID());
        return pnt;
    }

    private static LineString to(org.geolatte.geom.LineString lineString) {
        LineString ls = geometryFactory().createLineString(sequenceOf(lineString));
        ls.setSRID(lineString.getSRID());
        return ls;
    }

    private static LinearRing to(org.geolatte.geom.LinearRing linearRing) {
        LinearRing lr = geometryFactory().createLinearRing(sequenceOf(linearRing));
        lr.setSRID(linearRing.getSRID());
        return lr;
    }

    private static MultiPoint to(org.geolatte.geom.MultiPoint multiPoint) {
        MultiPoint mp = geometryFactory().createMultiPoint(sequenceOf(multiPoint));
        mp.setSRID(multiPoint.getSRID());
        for (int i =0;i <mp.getNumGeometries(); i++) {
            mp.getGeometryN(i).setSRID(multiPoint.getSRID());
        }
        return mp;
    }

    private static MultiLineString to(org.geolatte.geom.MultiLineString multiLineString) {
        LineString[] lineStrings = new LineString[multiLineString.getNumGeometries()];
        for (int i=0; i < multiLineString.getNumGeometries(); i++) {
            lineStrings[i] = to(multiLineString.getGeometryN(i));
        }
        MultiLineString mls = geometryFactory().createMultiLineString(lineStrings);
        mls.setSRID(multiLineString.getSRID());
        return mls;
    }

    private static MultiPolygon to(org.geolatte.geom.MultiPolygon multiPolygon) {
        Polygon[] polygons = new Polygon[multiPolygon.getNumGeometries()];
        for (int i=0; i < multiPolygon.getNumGeometries(); i++) {
            polygons[i] = to(multiPolygon.getGeometryN(i));
        }
        MultiPolygon mp = geometryFactory().createMultiPolygon(polygons);
        mp.setSRID(multiPolygon.getSRID());
        return mp;
    }

    private static GeometryCollection to(org.geolatte.geom.GeometryCollection collection) {
        Geometry[] geoms = new Geometry[collection.getNumGeometries()];
        for (int i=0; i < collection.getNumGeometries(); i++) {
            geoms[i] = to(collection.getGeometryN(i));
        }
        GeometryCollection gc = geometryFactory().createGeometryCollection(geoms);
        gc.setSRID(collection.getSRID());
        return gc;
    }

    /*
     * Helpermethod that transforms a coordinatesequence into a pointsequence.
     */
    private static PointSequence toPointSequence(CoordinateSequence cs) {
        if (cs instanceof PointSequence) {
            return (PointSequence) cs;
        }
        double[] coord;
        DimensionalFlag df;
        // TODO: A problem remains when the first point has a z value and the others don't
        if (Double.isNaN(cs.getCoordinate(0).z)) {
            df = DimensionalFlag.XY;
            coord = new double[2];
        } else {
            df = DimensionalFlag.XYZ;
            coord = new double[3];
        }
        PointSequenceBuilder builder = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(cs.size(), df);
        for (int i = 0; i < cs.size(); i++) {
            for (int ci = 0; ci < coord.length; ci++) {
                coord[ci] = cs.getOrdinate(i, ci);
            }
            builder.add(coord);
        }
        return builder.toPointSequence();
    }

    private static CoordinateSequence sequenceOf(org.geolatte.geom.Geometry geometry) {
        //TODO - when not Geometry instances, create a new PointSequence from the Geometry's Points.
        if (geometry == null) {
            throw new JTSConversionException("Can't convert null geometries.");
        } else {
            return (CoordinateSequence) geometry.getPoints();
        }
    }
}


