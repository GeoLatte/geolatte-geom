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

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Karel Maesen, Geovise BVBA, 2011 (original code)
 * @author Yves Vandewoude, Qmino bvba, 2011 (bugfixes)
 */
public class JTS {

    private static final PointSequenceCoordinateSequenceFactory pscsFactory = new
            PointSequenceCoordinateSequenceFactory();

    private static final Map<Class<? extends Geometry>, Class<? extends org.geolatte.geom.Geometry>> JTS2GLClassMap =
            new HashMap<Class<? extends Geometry>, Class<? extends org.geolatte.geom.Geometry>>();

    private static final ConcurrentHashMap<Integer, GeometryFactory> geometryFactories = new ConcurrentHashMap<>();

    static {

        //define the class mapping JTS -> Geolatte
        JTS2GLClassMap.put(Point.class, org.geolatte.geom.Point.class);
        JTS2GLClassMap.put(LineString.class, org.geolatte.geom.LineString.class);
        JTS2GLClassMap.put(LinearRing.class, org.geolatte.geom.LinearRing.class);
        JTS2GLClassMap.put(Polygon.class, org.geolatte.geom.Polygon.class);
        JTS2GLClassMap.put(GeometryCollection.class, org.geolatte.geom.GeometryCollection.class);
        JTS2GLClassMap.put(MultiPoint.class, org.geolatte.geom.MultiPoint.class);
        JTS2GLClassMap.put(MultiLineString.class, org.geolatte.geom.MultiLineString.class);
        JTS2GLClassMap.put(MultiPolygon.class, org.geolatte.geom.MultiPolygon.class);

    }

    /**
     * Returns the JTS Geometry class that corresponds to the specified Geolatte Geometry class.
     * <p>Geometry classes correspond iff they are of the same Geometry type in the SFS or SFA geometry model.</p>
     *
     * @param geometryClass the JTS Geometry class
     * @return the corresponding o.g.geom class
     * @throws IllegalArgumentException when the geometryClass parameter is null.
     * @throws NoSuchElementException   when no corresponding class can be found.
     */
    public static Class<? extends Geometry> getCorrespondingJTSClass(Class<? extends org.geolatte.geom.Geometry>
                                                                             geometryClass) {
        if (geometryClass == null) throw new IllegalArgumentException("Null argument not allowed.");
        for (Map.Entry<Class<? extends Geometry>, Class<? extends org.geolatte.geom.Geometry>> entry : JTS2GLClassMap
                .entrySet()) {
            if (entry.getValue() == geometryClass) {
                return entry.getKey();
            }
        }
        throw new NoSuchElementException(String.format("No mapping for class %s exists in JTS geom.", geometryClass
                .getCanonicalName()));
    }

    /**
     * Returns the Geolatte Geometry class that corresponds to the specified JTS class.
     * <p>Geometry classes correspond iff they are of the same Geometry type in the SFS or SFA geometry model.</p>
     *
     * @param jtsGeometryClass the Geolatte Geometry class
     * @return the corresponding o.g.geom class
     * @throws IllegalArgumentException when the jtsGeometryClass parameter is null.
     * @throws NoSuchElementException   when no corresponding class can be found.
     */
    static Class<? extends org.geolatte.geom.Geometry> getCorrespondingGeolatteClass(Class<? extends Geometry>
                                                                                             jtsGeometryClass) {
        if (jtsGeometryClass == null) throw new IllegalArgumentException("Null argument not allowed.");
        Class<? extends org.geolatte.geom.Geometry> corresponding = JTS2GLClassMap.get(jtsGeometryClass);
        if (corresponding == null) {
            throw new NoSuchElementException(String.format("No mapping for class %s exists in JTS geom.",
                    jtsGeometryClass.getCanonicalName()));
        }
        return corresponding;
    }


    /**
     * Primary Factory method that converts a JTS geometry into an equivalent geolatte geometry
     *
     * @param jtsGeometry the jts geometry to convert
     * @return an equivalent geolatte geometry
     * @throws IllegalArgumentException when a null object is passed
     */
    public static org.geolatte.geom.Geometry<?> from(org.locationtech.jts.geom.Geometry jtsGeometry) {
        if (jtsGeometry == null) {
            throw new IllegalArgumentException("Null object passed.");
        }
        Coordinate testCo = jtsGeometry.getCoordinate();
        boolean is3D = !(testCo == null || Double.isNaN(testCo.z));
        CoordinateReferenceSystem<?> crs = CrsRegistry.ifAbsentReturnProjected2D(jtsGeometry.getSRID());
        if (is3D) {
            crs = CoordinateReferenceSystems.addVerticalSystem(crs, LinearUnit.METER);
        }

        // to translate measure, add Measure as LinearSystem
        boolean hasM = testCo instanceof DimensionalCoordinate
                && !Double.isNaN(((DimensionalCoordinate)testCo).m);
        if (hasM) {
            crs = CoordinateReferenceSystems.addLinearSystem(crs, LinearUnit.METER);
        }

        return from(jtsGeometry, crs);
    }

    /**
     * Factory method that converts a JTS geometry into an equivalent geolatte geometry and allows the caller to
     * specify the CoordinateReferenceSystem of the resulting geolatte geometry.
     *
     * @param jtsGeometry the jtsGeometry
     * @param crs         the CoordinateReferenceSystem
     * @return A geolatte geometry that corresponds with the given JTS geometry
     * @throws IllegalArgumentException when a null object is passed
     */
    public static <P extends Position> org.geolatte.geom.Geometry<P> from(Geometry jtsGeometry,
                                                                          CoordinateReferenceSystem<P> crs) {
        if (jtsGeometry == null) {
            throw new IllegalArgumentException("Null object passed.");
        }
        if (jtsGeometry instanceof Point) {
            return from((Point) jtsGeometry, crs);
        } else if (jtsGeometry instanceof LineString) {
            return from((LineString) jtsGeometry, crs);
        } else if (jtsGeometry instanceof Polygon) {
            return from((Polygon) jtsGeometry, crs);
        } else if (jtsGeometry instanceof MultiPoint) {
            return from((MultiPoint) jtsGeometry, crs);
        } else if (jtsGeometry instanceof MultiLineString) {
            return from((MultiLineString) jtsGeometry, crs);
        } else if (jtsGeometry instanceof MultiPolygon) {
            return from((MultiPolygon) jtsGeometry, crs);
        } else if (jtsGeometry instanceof GeometryCollection) {
            return from((GeometryCollection) jtsGeometry, crs);
        } else {
            throw new JTSConversionException();
        }
    }

    /**
     * Primary factory method that converts a geolatte geometry into an equivalent jts geometry
     *
     * @param geometry the geolatte geometry to start from
     * @param gFact the GeometryFactory to use for creating the JTS Geometry
     * @return the equivalent JTS geometry
     * @throws IllegalArgumentException when a null object is passed
     */
    public static <P extends Position> org.locationtech.jts.geom.Geometry to(org.geolatte.geom.Geometry<P> geometry, GeometryFactory gFact) {
        if (geometry == null || gFact == null) {
            throw new IllegalArgumentException("Null object passed.");
        }
        if (geometry instanceof org.geolatte.geom.Point) {
            return to((org.geolatte.geom.Point<P>) geometry, gFact);
        } else if (geometry instanceof org.geolatte.geom.LineString) {
            return to((org.geolatte.geom.LineString<P>) geometry, gFact);
        } else if (geometry instanceof org.geolatte.geom.MultiPoint) {
            return to((org.geolatte.geom.MultiPoint<P>) geometry, gFact);
        } else if (geometry instanceof org.geolatte.geom.Polygon) {
            return to((org.geolatte.geom.Polygon<P>) geometry, gFact);
        } else if (geometry instanceof org.geolatte.geom.MultiLineString) {
            return to((org.geolatte.geom.MultiLineString<P>) geometry, gFact);
        } else if (geometry instanceof org.geolatte.geom.MultiPolygon) {
            return to((org.geolatte.geom.MultiPolygon<P>) geometry, gFact);
        } else if (geometry instanceof org.geolatte.geom.GeometryCollection) {
            return to((org.geolatte.geom.GeometryCollection<P, org.geolatte.geom.Geometry<P>>) geometry, gFact);
        } else {
            throw new JTSConversionException();
        }
    }

    public static <P extends Position> org.locationtech.jts.geom.Geometry to(org.geolatte.geom.Geometry<P> geometry) {
        if (geometry == null) {
            throw new IllegalArgumentException("Null object passed.");
        }
        GeometryFactory gFact = geometryFactory(geometry.getSRID());
        return to(geometry, gFact);
    }


    private static GeometryFactory geometryFactory(int srid) {
        return geometryFactories.computeIfAbsent(srid, id -> buildGeometryFactory(id));
    }

    private static GeometryFactory buildGeometryFactory(int srid) {
        return new GeometryFactory(new PrecisionModel(), srid, pscsFactory);
    }

    /**
     * Converts a JTS <code>Envelope</code> to a geolatte <code>Envelope</code>.
     *
     * @param jtsEnvelope the JTS Envelope to convert
     * @return the corresponding geolatte Envelope.
     * @throws IllegalArgumentException when a null object is passed
     */
    public static org.geolatte.geom.Envelope<C2D> from(org.locationtech.jts.geom.Envelope jtsEnvelope) {
        if (jtsEnvelope == null) {
            throw new IllegalArgumentException("Null object passed.");
        }
        return new org.geolatte.geom.Envelope<C2D>(jtsEnvelope.getMinX(), jtsEnvelope.getMinY(), jtsEnvelope.getMaxX
                (), jtsEnvelope.getMaxY(), CoordinateReferenceSystems.PROJECTED_2D_METER);
    }

    /**
     * Converts a JTS <code>Envelope</code> to a geolatte <code>Envelope</code> with the
     * specified CRS.
     *
     * @param jtsEnvelope the JTS Envelope to convert.
     * @param crs         the <code>CoordinateReferenceSystem</code> to use for the return value.
     * @return the corresponding geolatte Envelope, having the CRS specified in the crsId parameter.
     * @throws IllegalArgumentException when a null object is passed
     */
    public static <P extends Position> org.geolatte.geom.Envelope<P> from(org.locationtech.jts.geom.Envelope
                                                                                  jtsEnvelope,
                                                                          CoordinateReferenceSystem<P> crs) {
        if (jtsEnvelope == null) {
            throw new IllegalArgumentException("Null object passed.");
        }
        return new org.geolatte.geom.Envelope<P>(jtsEnvelope.getMinX(), jtsEnvelope.getMinY(), jtsEnvelope.getMaxX(),
                jtsEnvelope.getMaxY(), crs);
    }

    /**
     * Converts a Geolatte <code>Envelope</code> to a JTS <code>Envelope</code>.
     *
     * @param env the geolatte Envelope.
     * @return the corresponding JTS Envelope.
     * @throws IllegalArgumentException when a null object is passed
     */
    public static org.locationtech.jts.geom.Envelope to(org.geolatte.geom.Envelope<?> env) {
        if (env == null) {
            throw new IllegalArgumentException("Null object passed.");
        }
        return new org.locationtech.jts.geom.Envelope(env.lowerLeft().getCoordinate(0), env.upperRight()
                .getCoordinate(0), env.lowerLeft().getCoordinate(1), env.upperRight().getCoordinate(1));
    }


    ///
    ///  Helpermethods: jts --> geolatte
    ///

    /*
     * Converts a jts multipolygon into a geolatte multipolygon
     */
    @SuppressWarnings("unchecked")
    private static <P extends Position> org.geolatte.geom.MultiPolygon<P> from(MultiPolygon jtsGeometry,
                                                                               CoordinateReferenceSystem<P> crs) {
        if (jtsGeometry.getNumGeometries() == 0) return new org.geolatte.geom.MultiPolygon<P>(crs);
        org.geolatte.geom.Polygon<P>[] polygons = (org.geolatte.geom.Polygon<P>[]) new org.geolatte.geom
                .Polygon[jtsGeometry.getNumGeometries()];
        for (int i = 0; i < jtsGeometry.getNumGeometries(); i++) {
            polygons[i] = from((Polygon) jtsGeometry.getGeometryN(i), crs);
        }
        return new org.geolatte.geom.MultiPolygon<P>(polygons);
    }

    /*
     * Converts a jts polygon into a geolatte polygon
     */
    @SuppressWarnings("unchecked")
    private static <P extends Position> org.geolatte.geom.Polygon<P> from(Polygon jtsGeometry,
                                                                          CoordinateReferenceSystem<P> crs) {
        if (jtsGeometry.isEmpty()) {
            return new org.geolatte.geom.Polygon<P>(crs);
        }
        org.geolatte.geom.LinearRing<P>[] rings = new org.geolatte.geom.LinearRing[jtsGeometry.getNumInteriorRing() +
                1];
        org.geolatte.geom.LineString<P> extRing = from(jtsGeometry.getExteriorRing(), crs);
        rings[0] = new org.geolatte.geom.LinearRing(extRing.getPositions(), extRing.getCoordinateReferenceSystem());
        for (int i = 1; i < rings.length; i++) {
            org.geolatte.geom.LineString intRing = from(jtsGeometry.getInteriorRingN(i - 1), crs);
            rings[i] = new org.geolatte.geom.LinearRing(intRing);
        }
        return new org.geolatte.geom.Polygon(rings);
    }

    /*
     * Converts a jts multilinestring into a geolatte multilinestring
     */
    @SuppressWarnings("unchecked")
    private static <P extends Position> org.geolatte.geom.MultiLineString<P> from(MultiLineString jtsGeometry,
                                                                                  CoordinateReferenceSystem<P> crs) {
        if (jtsGeometry.getNumGeometries() == 0) return new org.geolatte.geom.MultiLineString<P>(crs);
        org.geolatte.geom.LineString<P>[] linestrings = new org.geolatte.geom.LineString[jtsGeometry.getNumGeometries
                ()];
        for (int i = 0; i < linestrings.length; i++) {
            linestrings[i] = from((LineString) jtsGeometry.getGeometryN(i), crs);
        }
        return new org.geolatte.geom.MultiLineString<P>(linestrings);
    }

    /*
     * Converts a jts geometrycollection into a geolatte geometrycollection
     */
    @SuppressWarnings("unchecked")
    private static <P extends Position> org.geolatte.geom.GeometryCollection<P, org.geolatte.geom.Geometry<P>> from
    (GeometryCollection jtsGeometry, CoordinateReferenceSystem<P> crs) {
        if (jtsGeometry.getNumGeometries() == 0)
            return new org.geolatte.geom.GeometryCollection<P, org.geolatte.geom.Geometry<P>>(crs);
        org.geolatte.geom.Geometry<P>[] geoms = new org.geolatte.geom.Geometry[jtsGeometry.getNumGeometries()];
        for (int i = 0; i < jtsGeometry.getNumGeometries(); i++) {
            geoms[i] = from(jtsGeometry.getGeometryN(i), crs);
        }
        return new org.geolatte.geom.GeometryCollection<P, org.geolatte.geom.Geometry<P>>(geoms);
    }

    /*
     * Converts a jts linestring into a geolatte linestring
     */
    private static <P extends Position> org.geolatte.geom.LineString<P> from(LineString jtsLineString,
                                                                             CoordinateReferenceSystem<P> crs) {
        CoordinateSequence cs = jtsLineString.getCoordinateSequence();
        return new org.geolatte.geom.LineString<P>(pscsFactory.toPositionSequence(cs, crs.getPositionClass(), crs),
                crs);

    }

    /*
     * Converts a jts multipoint into a geolatte multipoint
     */
    @SuppressWarnings("unchecked")
    private static <P extends Position> org.geolatte.geom.MultiPoint<P> from(MultiPoint jtsMultiPoint,
                                                                             CoordinateReferenceSystem<P> crs) {
        if (jtsMultiPoint == null || jtsMultiPoint.getNumGeometries() == 0)
            return new org.geolatte.geom.MultiPoint<P>(crs);
        org.geolatte.geom.Point<P>[] points = new org.geolatte.geom.Point[jtsMultiPoint.getNumGeometries()];
        for (int i = 0; i < points.length; i++) {
            points[i] = from((Point) jtsMultiPoint.getGeometryN(i), crs);
        }
        return new org.geolatte.geom.MultiPoint<P>(points);
    }

    /*
     * Converts a jts point into a geolatte point
     */
    private static <P extends Position> org.geolatte.geom.Point<P> from(org.locationtech.jts.geom.Point jtsPoint,
                                                                        CoordinateReferenceSystem<P> crs) {
        CoordinateSequence cs = jtsPoint.getCoordinateSequence();
        return new org.geolatte.geom.Point<P>(pscsFactory.toPositionSequence(cs, crs.getPositionClass(), crs), crs);
    }

    ///
    ///  Helpermethods: geolatte --> jts
    ///

    private static <P extends Position> Polygon to(org.geolatte.geom.Polygon<P> polygon, GeometryFactory gFact) {
        LinearRing shell = to(polygon.getExteriorRing(), gFact);
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int i = 0; i < holes.length; i++) {
            holes[i] = to(polygon.getInteriorRingN(i), gFact);
        }
        return gFact.createPolygon(shell, holes);
    }

    private static <P extends Position> Point to(org.geolatte.geom.Point<P> point, GeometryFactory gFact) {
        return gFact.createPoint(sequenceOf(point));
    }

    private static <P extends Position> LineString to(org.geolatte.geom.LineString<P> lineString, GeometryFactory gFact) {
        return gFact.createLineString(sequenceOf(lineString));
    }

    private static <P extends Position> LinearRing to(org.geolatte.geom.LinearRing<P> linearRing, GeometryFactory gFact) {
        return gFact.createLinearRing(sequenceOf(linearRing));
    }

    private static <P extends Position> MultiPoint to(org.geolatte.geom.MultiPoint<P> multiPoint, GeometryFactory gFact) {
        Point[] points = new Point[multiPoint.getNumGeometries()];
        for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
            points[i] = to(multiPoint.getGeometryN(i), gFact);
        }
        return gFact.createMultiPoint(points);
    }

    private static <P extends Position> MultiLineString to(org.geolatte.geom.MultiLineString<P> multiLineString, GeometryFactory gFact) {
        LineString[] lineStrings = new LineString[multiLineString.getNumGeometries()];
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            lineStrings[i] = to(multiLineString.getGeometryN(i), gFact);
        }
        return gFact.createMultiLineString(lineStrings);
    }

    private static <P extends Position> MultiPolygon to(org.geolatte.geom.MultiPolygon<P> multiPolygon, GeometryFactory gFact) {
        Polygon[] polygons = new Polygon[multiPolygon.getNumGeometries()];
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            polygons[i] = to(multiPolygon.getGeometryN(i), gFact);
        }
        return gFact.createMultiPolygon(polygons);
    }

    private static <P extends Position> GeometryCollection to(org.geolatte.geom.GeometryCollection<P, org.geolatte
            .geom.Geometry<P>> collection, GeometryFactory gFact) {
        Geometry[] geoms = new Geometry[collection.getNumGeometries()];
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            geoms[i] = to(collection.getGeometryN(i));
        }
        return gFact.createGeometryCollection(geoms);
    }

    private static CoordinateSequence sequenceOf(org.geolatte.geom.Geometry geometry) {
        if (geometry == null) {
            throw new JTSConversionException("Can't convert null geometries.");
        } else {
            return (CoordinateSequence) geometry.getPositions();
        }
    }
}

