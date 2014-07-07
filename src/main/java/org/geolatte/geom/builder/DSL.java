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

    /**
     * Creates a projected 2D position token.
     *
     * @param x x or easting
     * @param y y or northing
     * @return a projected 2D PosToken
     */
    public static PosToken<P2D> p(double x, double y) {
        return new PosToken<>(x, y);
    }

    /**
     * Creates a projected 3D position token
     *
     * @param x x or easting
     * @param y y or northing
     * @param z z or altitude
     * @return a projected 3D PosToken
     */
    public static PosToken<P3D> p(double x, double y, double z) {
        return new PosToken<>(x, y, z);
    }

    /**
     * Creates a projected 2DM position token
     *
     * @param x x or easting
     * @param y y or northing
     * @param m measure value
     * @return a projected 2DM PosToken
     */
    public static PosToken<P2DM> pM(double x, double y, double m) {
        return new PosToken<>(x, y, m);
    }

    /**
     * Creates a projected 3DM position token
     *
     * @param x x or easting
     * @param y y or northing
     * @param z z or altitude
     * @param m measure value
     * @return a projected 3DM PosToken
     */
    public static PosToken<P3DM> p(double x, double y, double z, double m) {
        return new PosToken<>(x, y, z, m);
    }

    /**
     * Creates a geographic 2D position token
     *
     * @param lon longitude
     * @param lat latitude
     * @return a geographic 2D PosToken
     */
    public static PosToken<G2D> g(double lon, double lat) {
        return new PosToken<>(lon, lat);
    }

    /**
     * Creates a geographic 3D position token
     *
     * @param lon longitude
     * @param lat latitude
     * @param alt altitude
     * @return a geographic 3D PosToken
     */
    public static PosToken<G3D> g(double lon, double lat, double alt) {
        return new PosToken<>(lon, lat, alt);
    }

    /**
     * Creates a geographic 2DM position token
     *
     * @param lon longitude
     * @param lat latitude
     * @param m   measure value
     * @return a geographic 2DM PosToken
     */
    public static PosToken<G2DM> gM(double lon, double lat, double m) {
        return new PosToken<>(lon, lat, m);
    }

    /**
     * Creates a geographic 3DM position token
     *
     * @param lon longitude
     * @param lat latitude
     * @param alt altitude
     * @param m  measure value
     * @return a geographic 3DM PosToken
     */
    public static PosToken<G3DM> g(double lon, double lat, double alt, double m) {
        return new PosToken<>(lon, lat, alt, m);
    }


/* == REMOVED because not type-safe
    public static <P extends Position<P>> Point<P> point(CoordinateReferenceSystem<P> crs, double... coords) {
        P position = Positions.mkPosition(crs, coords);
        return new Point<>(position);
    }
*/

    public static <P extends Position<P>> Point<P> point(CoordinateReferenceSystem<P> crs, PosToken<P> p) {
        P position = Positions.mkPosition(crs, p.coords);
        return new Point<>(position);
    }

    public static <P extends Position<P>> PointToken<P> point(PosToken<P> position) {
        return new PointToken<>(position);
    }

    @SafeVarargs
    public static <P extends Position<P>> LineString<P> linestring(CoordinateReferenceSystem<P> crs, PosToken<P>... positions) {
        return new LineString<>(toSeq(crs, positions));
    }

    @SafeVarargs
    public static <P extends Position<P>> LineStringToken<P> linestring(PosToken<P>... positions) {
        return new LineStringToken<>(positions);
    }

    @SafeVarargs
    public static <P extends Position<P>> LinearRing<P> ring(CoordinateReferenceSystem<P> crs, PosToken<P>... positions) {
        return new LinearRing<>(toSeq(crs, positions));
    }

    @SafeVarargs
    public static <P extends Position<P>> LinearRingToken<P> ring(PosToken<P>... points) {
        return new LinearRingToken<>(points);
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
    @SafeVarargs
    public static <P extends Position<P>> GeometryCollection<P, Geometry<P>> geometrycollection(CoordinateReferenceSystem<P> crs, GeometryToken<P>... tokens) {
        if (tokens.length == 0) return new GeometryCollection<>(crs);
        Geometry<P>[] parts = new Geometry[tokens.length];
        int idx = 0;
        for (GeometryToken t : tokens) {
            parts[idx++] = t.toGeometry(crs);
        }
        return new GeometryCollection<>(parts);
    }

    @SafeVarargs
    public static <P extends Position<P>> GeometryCollectionToken<P> geometrycollection(GeometryToken<P>... tokens) {
        return new GeometryCollectionToken<>(tokens);
    }

    @SafeVarargs
    public static <P extends Position<P>> Polygon<P> polygon(LinearRing<P> hull, LinearRing<P>... rings) {
        LinearRing<P>[] combined = combine(LinearRing.class, hull, rings);
        return new Polygon<>(combined);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position<P>> Polygon<P> polygon(CoordinateReferenceSystem<P> crs, LinearRingToken<P>... tokens) {
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

    @SuppressWarnings("unchecked")
    public static <P extends Position<P>> PolygonToken<P> polygon(LinearRingToken<P>... tokens) {
        return new PolygonToken<>(tokens);
    }

    @SafeVarargs
    public static <P extends Position<P>> MultiPoint<P> multipoint(Point<P> point, Point<P>... points) {
        return new MultiPoint<>(combine(Point.class, point, points));
    }

    @SafeVarargs
    public static <P extends Position<P>> MultiPointToken<P> multipoint(PointToken<P>... tokens) {
        return new MultiPointToken<>(tokens);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position<P>> MultiPoint<P> multipoint(CoordinateReferenceSystem<P> crs, PointToken<P>... tokens) {

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

    @SafeVarargs
    public static <P extends Position<P>> MultiLineStringToken<P> multilinestring(LineStringToken<P>... tokens) {
        return new MultiLineStringToken<>(tokens);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Position<P>> MultiLineString<P> multilinestring(CoordinateReferenceSystem<P> crs, LineStringToken<P>... tokens) {
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

    @SafeVarargs
    public static <P extends Position<P>> MultiPolygonToken<P> multipolygon(PolygonToken<P>... tokens) {
        return new MultiPolygonToken<>(tokens);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Position<P>> MultiPolygon<P> multipolygon(CoordinateReferenceSystem<P> crs, PolygonToken<P>... tokens) {

        if (tokens.length == 0) return new MultiPolygon<>(crs);
        Polygon<P>[] polygons = new Polygon[tokens.length];
        int idx = 0;
        for (PolygonToken t : tokens) {
            polygons[idx++] = t.toGeometry(crs);
        }
        return new MultiPolygon<>(polygons);

    }

    @SafeVarargs
    static <P extends Position<P>> PositionSequence<P> toSeq(CoordinateReferenceSystem<P> crs, PosToken<P>... tokens) {
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

    public abstract static class GeometryToken<P extends Position<P>> {
        abstract Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs);

    }

    public static class PointToken<P extends Position<P>> extends GeometryToken<P> {
        private PosToken<P> p;

        PointToken(PosToken<P> p) {
            this.p = p;
        }

        @Override
        Point<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            P position = Positions.mkPosition(crs, p.coords);
            return new Point<>(position);
        }
    }

    public static class LineStringToken<P extends Position<P>> extends GeometryToken<P> {
        private PosToken[] positions;

        LineStringToken(PosToken<P>... positions) {
            this.positions = Arrays.copyOf(positions, positions.length);
        }

        @Override
        LineString<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            return new LineString<>(toSeq(crs, positions));
        }
    }

    public static class LinearRingToken<P extends Position<P>> extends GeometryToken<P> {
        private PosToken[] positions;

        LinearRingToken(PosToken<P>... positions) {
            this.positions = Arrays.copyOf(positions, positions.length);
        }

        @Override
        LinearRing<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            return new LinearRing<>(toSeq(crs, positions));
        }
    }


    public static class PolygonToken<P extends Position<P>> extends GeometryToken<P> {
        private LinearRingToken[] ringTokens;

        PolygonToken(LinearRingToken<P>... ringTokens) {
            this.ringTokens = Arrays.copyOf(ringTokens, ringTokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        Polygon<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            LinearRing<P>[] rings = new LinearRing[ringTokens.length];

            for (int i = 0; i < rings.length; i++) {
                rings[i] = ringTokens[i].toGeometry(crs);
            }
            return new Polygon(rings);
        }
    }

    public static class GeometryCollectionToken<P extends Position<P>> extends GeometryToken<P> {
        private GeometryToken[] tokens;

        GeometryCollectionToken(GeometryToken<P>... tokens) {
            this.tokens = Arrays.copyOf(tokens, tokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        GeometryCollection<P, Geometry<P>> toGeometry(CoordinateReferenceSystem<P> crs) {
            Geometry<P>[] parts = new Geometry[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                parts[i] = tokens[i].toGeometry(crs);
            }
            return new GeometryCollection<>(parts);
        }

    }

    public static class MultiPointToken<P extends Position<P>> extends GeometryToken<P> {
        private PointToken[] tokens;

        MultiPointToken(PointToken<P>... tokens) {
            this.tokens = Arrays.copyOf(tokens, tokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        MultiPoint<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            Point<P>[] parts = new Point[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                parts[i] = tokens[i].toGeometry(crs);
            }
            return new MultiPoint<>(parts);
        }

    }


    public static class MultiLineStringToken<P extends Position<P>> extends GeometryToken<P> {
        private LineStringToken[] tokens;

        MultiLineStringToken(LineStringToken... tokens) {
            this.tokens = Arrays.copyOf(tokens, tokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        MultiLineString<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            LineString<P>[] parts = new LineString[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                parts[i] = tokens[i].toGeometry(crs);
            }
            return new MultiLineString<>(parts);
        }

    }

    public static class MultiPolygonToken<P extends Position<P>> extends GeometryToken<P> {
        private PolygonToken[] tokens;

        MultiPolygonToken(PolygonToken... tokens) {
            this.tokens = Arrays.copyOf(tokens, tokens.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        MultiPolygon<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            Polygon<P>[] parts = new Polygon[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                parts[i] = tokens[i].toGeometry(crs);
            }
            return new MultiPolygon<>(parts);
        }

    }

}
