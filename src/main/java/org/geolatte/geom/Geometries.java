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
 * Copyright (C) 2010 - 2014 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.List;

/**
 * A Factory for {@code Geometry}s
 * <p/>
 * <p>This Factory allows generically creating Geometries</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 5/15/14
 */
public class Geometries {

    /**
     * Creates a {@code Point} from a Position and coordinate reference system
     * @param pos the position for the created {@code Point}
     * @param crs the coordinate reference system for the created {@code Point}
     * @param <P> the type of {@code Position}
     * @return a {@code Point} with the specified position and coordinate reference system
     */
    public static <P extends Position> Point<P> mkPoint(P pos, CoordinateReferenceSystem<P> crs) {
        return new Point<P>(pos, crs);
    }

    /**
     * Creates a {@code LineString} from a {@code PositionSequence} and coordinate reference system
     * @param seq the {@code PositionSequence} for the created {@code LineString}
     * @param crs the coordinate reference system for the created {@code LineString}
     * @param <P> the type of {@code Position}
     * @return a {@code LineString} with the specified positions and coordinate reference system
     */
    public static <P extends Position> LineString<P> mkLineString(PositionSequence<P> seq, CoordinateReferenceSystem<P> crs) {
        return new LineString<P>(seq, crs);
    }

    /**
     * Creates a {@code LinearRing} from a {@code PositionSequence} and coordinate reference system
     * @param seq the {@code PositionSequence} for the created {@code LinearRing}
     * @param crs the coordinate reference system for the created {@code LinearRing}
     * @param <P> the type of {@code Position}
     * @return a {@code LinearRing} with the specified positions and coordinate reference system
     */
    public static <P extends Position> LinearRing<P> mkLinearRing(PositionSequence<P> seq, CoordinateReferenceSystem<P> crs) {
        return new LinearRing<P>(seq, crs);
    }

    public static <P extends Position> Polygon<P> mkPolygon(LinearRing<P>... rings) {
        return new Polygon<P>(rings);
    }

    public static <P extends Position> Polygon<P> mkPolygon(List<LinearRing<P>> rings) {
        LinearRing<P>[] ringArr = new LinearRing[rings.size()];
        return new Polygon<>(rings.toArray(ringArr));
    }

    public static <P extends Position> GeometryCollection<P, Geometry<P>> mkGeometryCollection(Geometry<P>... geometries) {
        return new GeometryCollection<>(geometries);
    }

    public static <P extends Position> GeometryCollection<P, Geometry<P>> mkGeometryCollection(List<Geometry<P>> geometries) {
        Geometry<P>[] geomArr= new Geometry[geometries.size()];
        return new GeometryCollection<>(geometries.toArray(geomArr));
    }


    public static <P extends Position> MultiPoint<P> mkMultiPoint(Point<P>... points) {
        return new MultiPoint<P>(points);
    }

    public static <P extends Position> MultiPoint<P> mkMultiPoint(List<Point<P>> points) {
        Point<P>[] pointArr = new Point[points.size()];
        return new MultiPoint<>(points.toArray(pointArr));
    }

    public static <P extends Position> MultiLineString<P> mkMultiLineString(List<LineString<P>> lineStrings) {
        LineString<P>[] lsArr = new LineString[lineStrings.size()];
        return new MultiLineString<>(lineStrings.toArray(lsArr));
    }

    public static <P extends Position> MultiLineString<P> mkMultiLineString(LineString<P>... linestrings) {
        return new MultiLineString<>(linestrings);
    }

    public static <P extends Position> MultiPolygon<P> mkMultiPolygon(Polygon<P>... polygons) {
        return new MultiPolygon<>(polygons);
    }

    public static <P extends Position> MultiPolygon<P> mkMultiPolygon(List<Polygon<P>> polygons) {
        Polygon<P>[] pArr = new Polygon[polygons.size()];
        return new MultiPolygon<>(polygons.toArray(pArr));
    }


    @SuppressWarnings("unchecked")
    public static <P extends Position> Geometry<P> mkGeometry(Class<? extends Simple> geometryClass,
                                                              PositionSequence<P> positions,
                                                              CoordinateReferenceSystem<P> crs) {

        if (geometryClass == null) {
            throw new IllegalArgumentException("Null argument not allowed");
        }
        if (Point.class.isAssignableFrom(geometryClass)) {
            return new Point<>(positions, crs);
        }

        if (LinearRing.class.isAssignableFrom(geometryClass)) {
            return new LinearRing<>(positions, crs);
        }

        if (LineString.class.isAssignableFrom(geometryClass)) {
            return new LineString<>(positions, crs);
        }

        throw new IllegalStateException("Unknown Geometry class");
    }

    @SuppressWarnings("unchecked")
    public static <P extends Position> Geometry<P> mkGeometry(Class<? extends Complex> geometryClass, Geometry<P>... parts) {
        if (Polygon.class.isAssignableFrom(geometryClass)) {
            return new Polygon((LinearRing<P>[]) parts);
        }
        if (MultiLineString.class.isAssignableFrom(geometryClass)) {
            return new MultiLineString<>((LineString<P>[]) parts);
        }
        if (MultiPoint.class.isAssignableFrom(geometryClass)) {
            return new MultiPoint<>((Point<P>[]) parts);
        }
        if (MultiPolygon.class.isAssignableFrom(geometryClass)) {
            return new MultiPolygon<>((Polygon<P>[]) parts);
        }
        if (GeometryCollection.class.isAssignableFrom(geometryClass)) {
            return new GeometryCollection<P, Geometry<P>>((Geometry<P>[])parts);
        }
        throw new IllegalStateException("Unknown Geometry class");

    }

    public static <Q extends Position> Geometry<Q> mkGeometry(Class<? extends Complex> geometryClass, CoordinateReferenceSystem<Q> crs) {
        if (Polygon.class.isAssignableFrom(geometryClass)) {
                   return new Polygon<>(crs);
               }
               if (MultiLineString.class.isAssignableFrom(geometryClass)) {
                   return new MultiLineString<>(crs);
               }
               if (MultiPoint.class.isAssignableFrom(geometryClass)) {
                   return new MultiPoint<>(crs);
               }
               if (MultiPolygon.class.isAssignableFrom(geometryClass)) {
                   return new MultiPolygon<>(crs);
               }
               if (GeometryCollection.class.isAssignableFrom(geometryClass)) {
                   return new GeometryCollection<>(crs);
               }
               throw new IllegalStateException("Unknown Geometry class");
    }

}
