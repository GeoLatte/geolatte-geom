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

package org.geolatte.geom.builder;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Contains a Domain Specific Language for constructing Geometries.
 * <p/>
 * Usage example:
 * <pre>
 * {@code
 *
 * import static org.geolatte.geom.builder.DSL.*;
 *
 *
 * public Polygon createPolygon() {
 *     return polygon(crs, ring(p(0, 0), p(0, 1), p(1, 1), p(1, 0), p(0, 0)));
 * }
 *
 * }
 * </pre>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class DSL {

    public static PosToken p(double... coords) {
        return new PosToken(coords);
    }


    public static <P extends Position<P>> Point<P> point(CoordinateReferenceSystem<P> crs, double... coords) {
        P position = Positions.mkPosition(crs, coords);
        return new Point<>(position);
    }

    public static PointToken point(double... coords) {
        return new PointToken(coords);
    }

    public static <P extends Position<P>> LineString<P> linestring(CoordinateReferenceSystem<P> crs, PosToken... points) {
        return new LineString<>(toSeq(crs, points));
    }

    public static LineStringToken linestring(PosToken... points) {
        return new LineStringToken(points);
    }

    public static <P extends Position<P>> LinearRing<P> ring(CoordinateReferenceSystem<P> crs, PosToken... points) {
        return new LinearRing<>(toSeq(crs, points));
    }

    public static LinearRingToken ring(PosToken... points) {
        return new LinearRingToken(points);
    }

    @SuppressWarnings("unchecked")
    private static <P extends Position<P>, G extends Geometry<P>> G[] combine(Class<? super G> resultType, G geometry, G... geometries) {
        Object[] allGeometries = (Object[]) Array.newInstance(resultType, geometries.length + 1);
        allGeometries[0] = geometry;
        System.arraycopy(geometries, 0, allGeometries, 1, geometries.length);
        return (G[]) allGeometries;
    }

    @SafeVarargs
    public static <P extends Position<P>> GeometryCollection<P, Geometry<P>> geometrycollection(Geometry<P> geometry, Geometry<P>... geometries) {
        return new GeometryCollection<>(combine(Geometry.class, geometry, geometries));
    }

    @SuppressWarnings("unchecked")
    public static <P extends Position<P>> GeometryCollection<P, Geometry<P>> geometrycollection(CoordinateReferenceSystem<P> crs, GeometryToken... tokens) {
        if (tokens.length == 0) return new GeometryCollection<>(crs);
        Geometry<P>[] parts = new Geometry[tokens.length];
        int idx = 0;
        for (GeometryToken t : tokens) {
            parts[idx++] = t.toGeometry(crs);
        }
        return new GeometryCollection<>(parts);
    }

    public static GeometryCollectionToken geometrycollection(GeometryToken... tokens) {
        return new GeometryCollectionToken(tokens);
    }

//    public static <P extends Position<P>> GeometryCollection<P, Geometry<P>> geometrycollection(CoordinateReferenceSystem<P> crs) {
//        return new GeometryCollection<>(crs);
//    }

    @SafeVarargs
    public static <P extends Position<P>> Polygon<P> polygon(LinearRing<P> hull, LinearRing<P>... rings) {
        LinearRing<P>[] combined = combine(LinearRing.class, hull, rings);
        return new Polygon<>(combined);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Position<P>> Polygon<P> polygon(CoordinateReferenceSystem<P> crs, LinearRingToken... tokens) {
        if (tokens.length == 0) {
            return new Polygon<>(crs);
        }
        LinearRing<P>[] rings = new LinearRing[tokens.length];
        int idx = 0;
        for (LinearRingToken t : tokens) {
            rings[idx++] = t.toGeometry(crs);
        }
        return new Polygon<>(rings);
    }

    public static PolygonToken polygon(LinearRingToken... tokens) {
        return new PolygonToken(tokens);
    }

    @SafeVarargs
    public static <P extends Position<P>> MultiPoint<P> multipoint(Point<P> point, Point<P>... points) {
        return new MultiPoint<>(combine(Point.class, point, points));
    }

    public static MultiPointToken multipoint(PointToken... tokens) {
        return new MultiPointToken(tokens);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Position<P>> MultiPoint<P> multipoint(CoordinateReferenceSystem<P> crs, PointToken... tokens) {

        if (tokens.length == 0) return new MultiPoint<>(crs);

        Point<P>[] points = new Point[tokens.length];
        int idx = 0;
        for (PointToken t : tokens) {
            points[idx++] = t.toGeometry(crs);
        }
        return new MultiPoint<>(points);

    }

    @SafeVarargs
    public static <P extends Position<P>> MultiLineString<P> multilinestring(LineString<P> linestring, LineString<P>... linestrings) {
        return new MultiLineString<>(combine(LineString.class, linestring, linestrings));
    }

    public static MultiLineStringToken multilinestring(LineStringToken... tokens) {
        return new MultiLineStringToken(tokens);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Position<P>> MultiLineString<P> multilinestring(CoordinateReferenceSystem<P> crs, LineStringToken... tokens) {
        if (tokens.length == 0) return new MultiLineString<>(crs);
        LineString<P>[] linestrings = new LineString[tokens.length];
        int idx = 0;
        for (LineStringToken t : tokens) {
            linestrings[idx++] = t.toGeometry(crs);
        }
        return new MultiLineString<>(linestrings);
    }

    @SafeVarargs
    public static <P extends Position<P>> MultiPolygon<P> multipolygon(Polygon<P> polygon, Polygon<P>... polygons) {
        return new MultiPolygon<>(combine(Polygon.class, polygon, polygons));
    }

    public static MultiPolygonToken multipolygon(PolygonToken... tokens) {
        return new MultiPolygonToken(tokens);
    }

    public static <P extends Position<P>> MultiPolygon<P> multipolygon(CoordinateReferenceSystem<P> crs, PolygonToken... tokens) {

        if (tokens.length == 0) return new MultiPolygon<>(crs);
        Polygon<P>[] polygons = new Polygon[tokens.length];
        int idx = 0;
        for (PolygonToken t : tokens) {
            polygons[idx++] = t.toGeometry(crs);
        }
        return new MultiPolygon<>(polygons);

    }

    static <P extends Position<P>> PositionSequence<P> toSeq(CoordinateReferenceSystem<P> crs, PosToken... tokens) {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(tokens.length, crs);
        for (PosToken t : tokens) {
            P pos = Positions.mkPosition(crs, t.coords);
            builder.add(pos);
        }
        return builder.toPositionSequence();
    }


    public static class PosToken<P extends Position<P>> {
        final double[] coords;

        PosToken(double... coords) {
            this.coords = coords;
        }
    }

    public abstract static class GeometryToken {
        abstract <P extends Position<P>> Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs);

    }

    public static class PointToken extends GeometryToken {
        private double[] coords;

        PointToken(double[] c) {
            coords = Arrays.copyOf(c, c.length);
        }

        @Override
        <P extends Position<P>> Point<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            P position = Positions.mkPosition(crs, coords);
            return new Point<>(position);
        }
    }

    public static class LineStringToken extends GeometryToken {
        private PosToken[] positions;

        LineStringToken(PosToken... positions) {
            this.positions = Arrays.copyOf(positions, positions.length);
        }

        @Override
        <P extends Position<P>> LineString<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            return new LineString<>(toSeq(crs, positions));
        }
    }

    public static class LinearRingToken extends GeometryToken {
        private PosToken[] positions;

        LinearRingToken(PosToken... positions) {
            this.positions = Arrays.copyOf(positions, positions.length);
        }

        @Override
        <P extends Position<P>> LinearRing<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            return new LinearRing<>(toSeq(crs, positions));
        }
    }


    public static class PolygonToken extends GeometryToken {
        private LinearRingToken[] ringTokens;

        PolygonToken(LinearRingToken... ringTokens) {
            this.ringTokens = Arrays.copyOf(ringTokens, ringTokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        <P extends Position<P>> Polygon<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            LinearRing<P>[] rings = new LinearRing[ringTokens.length];

            for (int i = 0; i < rings.length; i++) {
                rings[i] = ringTokens[i].toGeometry(crs);
            }
            return new Polygon(rings);
        }
    }

    public static class GeometryCollectionToken extends GeometryToken {
        private GeometryToken[] tokens;

        GeometryCollectionToken(GeometryToken... tokens) {
            this.tokens = Arrays.copyOf(tokens, tokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        <P extends Position<P>> GeometryCollection<P, Geometry<P>> toGeometry(CoordinateReferenceSystem<P> crs) {
            Geometry<P>[] parts = new Geometry[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                parts[i] = tokens[i].toGeometry(crs);
            }
            return new GeometryCollection<>(parts);
        }

    }

    public static class MultiPointToken extends GeometryToken {
        private PointToken[] tokens;

        MultiPointToken(PointToken... tokens) {
            this.tokens = Arrays.copyOf(tokens, tokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        <P extends Position<P>> MultiPoint<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            Point<P>[] parts = new Point[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                parts[i] = tokens[i].toGeometry(crs);
            }
            return new MultiPoint<>(parts);
        }

    }


    public static class MultiLineStringToken extends GeometryToken {
        private LineStringToken[] tokens;

        MultiLineStringToken(LineStringToken... tokens) {
            this.tokens = Arrays.copyOf(tokens, tokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        <P extends Position<P>> MultiLineString<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            LineString<P>[] parts = new LineString[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                parts[i] = tokens[i].toGeometry(crs);
            }
            return new MultiLineString<>(parts);
        }

    }

    public static class MultiPolygonToken extends GeometryToken {
        private PolygonToken[] tokens;

        MultiPolygonToken(PolygonToken... tokens) {
            this.tokens = Arrays.copyOf(tokens, tokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        <P extends Position<P>> MultiPolygon<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            Polygon<P>[] parts = new Polygon[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                parts[i] = tokens[i].toGeometry(crs);
            }
            return new MultiPolygon<>(parts);
        }

    }

}
