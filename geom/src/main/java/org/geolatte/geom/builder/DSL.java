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
 * <p>
 * The DSL has methods for creating {@code Position}s, {@code Geometry}s and {@code GeometryTokens}.
 * </p>
 * <p>
 * The methods for creating {@code Position}s are:
 * </p>
 * <ul>
 *     <li>g(lon, lat), g(lon,lat,height), gM(lon,lat, measure), and g(lon,lat,height, measure) for resp. {@code G2D}, {@code G3D}, {@code G2DM} and
 *     {@code G3DM} {@code Position} instances</li>
 *     <li>c(x,y), p(x,y,z), cM(x,y,measure), c(x,y,z,m) for resp. {@code P2D}, {@code P3D}, {@code P2DM} and {@code P3DM} {@code Position} instances</li>
 * </ul>
 * <p>
 * The methods for creating geometries are named for the geometry the create.
 * </p>
 * <p>
 * {@code GeometryToken}s are intermediate representations that are handy when creating a {@code Geometry} that is composed of {@code Geometry}s. It allows the
 * DSL user to specify the coordinate reference system only once.
 * </p>
 * Usage example:
 * <pre>
 * {@code
 *
 * import static org.geolatte.geom.builder.DSL.*;
 *
 *
 * public Polygon createPolygon() {
 *     CoordinateReferenceSystem crs = ...
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

    private DSL(){
        //do nothing.
    }

    /**
     * Creates a projected 2D position token.
     *
     * @param x x or easting
     * @param y y or northing
     * @return a projected 2D PosToken
     */
    public static C2D c(double x, double y) {
        return new C2D(x, y);
    }

    /**
     * Creates a projected 3D position token
     *
     * @param x x or easting
     * @param y y or northing
     * @param z z or height
     * @return a projected 3D PosToken
     */
    public static C3D c(double x, double y, double z) {
        return new C3D(x, y, z);
    }

    /**
     * Creates a projected 2DM position token
     *
     * @param x x or easting
     * @param y y or northing
     * @param m measure value
     * @return a projected 2DM PosToken
     */
    public static C2DM cM(double x, double y, double m) {
        return new C2DM(x, y, m);
    }

    /**
     * Creates a projected 3DM position token
     *
     * @param x x or easting
     * @param y y or northing
     * @param z z or heightitude
     * @param m measure value
     * @return a projected 3DM PosToken
     */
    public static C3DM c(double x, double y, double z, double m) {
        return new C3DM(x, y, z, m);
    }

    /**
     * Creates a geographic 2D position token
     *
     * @param lon longitude
     * @param lat latitude
     * @return a geographic 2D PosToken
     */
    public static G2D g(double lon, double lat) {
        return new G2D(lon, lat);
    }

    /**
     * Creates a geographic 3D position token
     *
     * @param lon longitude
     * @param lat latitude
     * @param height height
     * @return a geographic 3D PosToken
     */
    public static G3D g(double lon, double lat, double height) {
        return new G3D(lon, lat, height);
    }

    /**
     * Creates a geographic 2DM position token
     *
     * @param lon longitude
     * @param lat latitude
     * @param m   measure value
     * @return a geographic 2DM PosToken
     */
    public static G2DM gM(double lon, double lat, double m) {
        return new G2DM(lon, lat, m);
    }

    /**
     * Creates a geographic 3DM position token
     *
     * @param lon longitude
     * @param lat latitude
     * @param height height
     * @param m  measure value
     * @return a geographic 3DM PosToken
     */
    public static G3DM g(double lon, double lat, double height, double m) {
        return new G3DM(lon, lat, height, m);
    }


    /**
     * Creates a {@code Point}
     *
     * @param crs the {@code CoordinateReferenceSystem} for the {@code Point}
     * @param p the {@code Position} for the {@code Point}
     * @param <P> the {@code Position} type
     * @return a {@code Point} having the specified {@code Position} and {@code CoordinateReferenceSystem}
     */
    public static <P extends Position> Point<P> point(CoordinateReferenceSystem<P> crs, P p) {
        return new Point<>(p, crs);
    }

    /**
     * Creates a {@code PointToken}
     * @param position the {@code Position} for the {@code PointToken}
     * @param <P> the {@code Position} type
     * @return a {@code PointToken} having the specified {@code Position}
     */
    public static <P extends Position> PointToken<P> point(P position) {
        return new PointToken<P>(position);
    }

    /**
     * Creates a {@code LineString}
     *
     * @param crs the {@code CoordinateReferenceSystem} for the {@code LineString}
     * @param positions the {@code Position}s for the {@code LineString}
     * @param <P> the {@code Position} type
     * @return a {@code LineString} having the specified {@code Position}s and {@code CoordinateReferenceSystem}
     */
    public static <P extends Position> LineString<P> linestring(CoordinateReferenceSystem<P> crs, P... positions) {
        return new LineString<P>(toSeq(crs, positions), crs);
    }

    /**
     * Creates a {@code LineStringToken}
     *
     * @param positions the {@code Position}s for the {@code LineStringToken}
     * @param <P> the {@code Position} type
     * @return a {@code LineStringToken} having the specified {@code Position}s
     */
    public static <P extends Position> LineStringToken<P> linestring(P... positions) {
        return new LineStringToken<P>(positions);
    }

    /**
     * Creates a {@code LinearRing}
     *
     * @param crs the {@code CoordinateReferenceSystem} for the {@code LinearRing}
     * @param positions the {@code Position}s for the {@code LinearRing}
     * @param <P> the {@code Position} type
     * @return a {@code LinearRing} having the specified {@code Position}s and {@code CoordinateReferenceSystem}
     */
    public static <P extends Position> LinearRing<P> ring(CoordinateReferenceSystem<P> crs, P... positions) {
        return new LinearRing<P>(toSeq(crs, positions), crs);
    }

    /**
     * Creates a {@code LinearRingToken}
     *
     * @param positions the {@code Position}s for the {@code LinearRingToken}
     * @param <P> the {@code Position} type
     * @return a {@code LinearRingToken} having the specified {@code Position}s
     */
    @SafeVarargs
    public static <P extends Position> LinearRingToken<P> ring(P... positions) {
        return new LinearRingToken<P>(positions);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    private static <P extends Position, G extends Geometry<P>> G[] combine(Class<G> resultType, G geometry, G... geometries) {
        Object[] allGeometries = (Object[]) Array.newInstance(resultType, geometries.length + 1);
        allGeometries[0] = geometry;
        System.arraycopy(geometries, 0, allGeometries, 1, geometries.length);
        return (G[]) allGeometries;
    }

    /**
     * Creates a {@code GeometryCollection} from the specified {@code Geometry}s.
     * @param geometry the first constituent {@code Geometry}
     * @param geometries the rest of the constituent {@code Geometry}s
     * @param <P> the {@code Position} type
     * @return the {@code GeometryCollection} of the specified constituent {@code Geometry}s.
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> GeometryCollection<P, Geometry<P>> geometrycollection(Geometry<P> geometry, Geometry<P>... geometries) {
        return new GeometryCollection<P, Geometry<P>>(combine(Geometry.class, geometry, geometries));
    }

    /**
     * Creates a {@code GeometryCollection} from the specified {@code GeometryToken}s and {@code CoordinateReferenceSystem}.
     *
     * @param crs the {@code CoordinateReferenceSystem} for the {@code GeometryCollection}
     * @param tokens the {@code GeometryTokens} for the constituent {@code Geometry}s of the returned {@code GeometryCollection}
     * @param <P> the {@code Position} type
     * @return the {@code GeometryCollection} of the specified constituent {@code Geometry}s and {@code CoordinateReferenceSystem}
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> GeometryCollection<P, Geometry<P>> geometrycollection(CoordinateReferenceSystem<P> crs, GeometryToken<P>... tokens) {
        if (tokens.length == 0) return new GeometryCollection<P, Geometry<P>>(crs);
        Geometry<P>[] parts = new Geometry[tokens.length];
        int idx = 0;
        for (GeometryToken t : tokens) {
            parts[idx++] = t.toGeometry(crs);
        }
        return new GeometryCollection<P, Geometry<P>>(parts);
    }

    /**
     * Creates a {@code GeometryCollectionToken} of the specified {@code GeometryToken}s
     *
     * @param tokens the {@code GeometryToken}s that represent the constituent {@code Geometry}s of the returned {@code GeometryCollection}
     * @param <P> the {@code Position} type
     * @return the {@code GeometryCollectionToken} of the specified constituent {@code GeometryToken}s
     */
    @SafeVarargs
    public static <P extends Position> GeometryCollectionToken<P> geometrycollection(GeometryToken<P>... tokens) {
        return new GeometryCollectionToken<P>(tokens);
    }

    /**
     * Creates a {@code Polygon} from the specified outer ring (or hull) and inner rings (if any)
     *
     * @param hull the outer ring of the returned {@code Polygon}
     * @param rings the inner rings of the returned {@code Polygon}
     * @param <P> the {@code Position} type
     * @return the {@code Polygon} defined by the specified outer and inner rings.
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> Polygon<P> polygon(LinearRing<P> hull, LinearRing<P>... rings) {
        LinearRing<P>[] combined = combine(LinearRing.class, hull, rings);
        return new Polygon<P>(combined);
    }

    /**
     * Creates a {@code Polygon} from the specified ring tokens and {@code CoordinateReferenceSystem}
     *
     * @param crs the {@code CoordinateReferenceSystem} for the returned {@code Polygon}
     * @param tokens the {@code GeometryTokens} representing (in order) the outer and any inner rings
     * @param <P> the {@code Position} type
     * @return the {@code Polygon} defined by the specified coordinate reference system and ring tokens
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> Polygon<P> polygon(CoordinateReferenceSystem<P> crs, LinearRingToken<P>... tokens) {
        if (tokens.length == 0) {
            return new Polygon<P>(crs);
        }
        LinearRing<P>[] rings = new LinearRing[tokens.length];
        int idx = 0;
        for (LinearRingToken t : tokens) {
            rings[idx++] = t.toGeometry(crs);
        }
        return new Polygon<P>(rings);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> PolygonToken<P> polygon(LinearRingToken<P>... tokens) {
        return new PolygonToken<P>(tokens);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <P extends Position> MultiPoint<P> multipoint(Point<P> point, Point<P>... points) {
        return new MultiPoint<P>(combine(Point.class, point, points));
    }

    @SafeVarargs
    public static <P extends Position> MultiPointToken<P> multipoint(PointToken<P>... tokens) {
        return new MultiPointToken<P>(tokens);
    }

    public static <P extends Position> MultiPoint<P> multipoint(CoordinateReferenceSystem<P> crs) {
        return Geometries.mkEmptyMultiPoint(crs);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> MultiPoint<P> multipoint(CoordinateReferenceSystem<P> crs, P... positions) {
        PositionSequence<P> ps = Positions.collect(crs.getPositionClass(), positions);
        return Geometries.mkMultiPoint(ps, crs);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> MultiPoint<P> multipoint(CoordinateReferenceSystem<P> crs, PointToken<P>... tokens) {

        if (tokens.length == 0) return new MultiPoint<P>(crs);

        Point<P>[] points = new Point[tokens.length];
        int idx = 0;
        for (PointToken t : tokens) {
            points[idx++] = t.toGeometry(crs);
        }
        return new MultiPoint<P>(points);

    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> MultiLineString<P> multilinestring(LineString<P> linestring, LineString<P>... linestrings) {
        return new MultiLineString<P>(combine(LineString.class, linestring, linestrings));
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> MultiLineStringToken<P> multilinestring(LineStringToken<P>... tokens) {
        return new MultiLineStringToken<P>(tokens);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> MultiLineString<P> multilinestring(CoordinateReferenceSystem<P> crs, LineStringToken<P>... tokens) {
        if (tokens.length == 0) return new MultiLineString<P>(crs);
        LineString<P>[] linestrings = new LineString[tokens.length];
        int idx = 0;
        for (LineStringToken t : tokens) {
            linestrings[idx++] = t.toGeometry(crs);
        }
        return new MultiLineString<P>(linestrings);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> MultiPolygon<P> multipolygon(Polygon<P> polygon, Polygon<P>... polygons) {
        return new MultiPolygon<P>(combine(Polygon.class, polygon, polygons));
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> MultiPolygonToken<P> multipolygon(PolygonToken<P>... tokens) {
        return new MultiPolygonToken<P>(tokens);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <P extends Position> MultiPolygon<P> multipolygon(CoordinateReferenceSystem<P> crs, PolygonToken<P>... tokens) {

        if (tokens.length == 0) return new MultiPolygon<P>(crs);
        Polygon<P>[] polygons = new Polygon[tokens.length];
        int idx = 0;
        for (PolygonToken t : tokens) {
            polygons[idx++] = t.toGeometry(crs);
        }
        return new MultiPolygon<P>(polygons);

    }


    static <P extends Position> PositionSequence<P> toSeq(CoordinateReferenceSystem<P> crs, P[] positions) {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(positions.length, crs.getPositionClass());
        double[] coords = new double[crs.getCoordinateDimension()];
        for (P t : positions) {
            P pos = Positions.mkPosition(crs, t.toArray(coords));
            builder.add(pos);
        }
        return builder.toPositionSequence();
    }

    public abstract static class GeometryToken<P extends Position> {
        abstract Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs);

    }

    public static class PointToken<P extends Position> extends GeometryToken<P> {
        private P p;

        PointToken(P p) {
            this.p = p;
        }

        @Override
        Point<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            return new Point<P>(p, crs);
        }
    }

    public static class LineStringToken<P extends Position> extends GeometryToken<P> {
        private P[] positions;


        LineStringToken(P... positions) {
            this.positions = Arrays.copyOf(positions, positions.length);
        }

        @Override
        LineString<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            return new LineString<P>(toSeq(crs, positions), crs);
        }
    }

    public static class LinearRingToken<P extends Position> extends GeometryToken<P> {
        private P[] positions;


        LinearRingToken(P... positions) {
            this.positions = Arrays.copyOf(positions, positions.length);
        }

        @Override
        LinearRing<P> toGeometry(CoordinateReferenceSystem<P> crs) {
            return new LinearRing<P>(toSeq(crs, positions), crs);
        }
    }


    public static class PolygonToken<P extends Position> extends GeometryToken<P> {
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

    public static class GeometryCollectionToken<P extends Position> extends GeometryToken<P> {
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
            return new GeometryCollection<P, Geometry<P>>(parts);
        }

    }

    public static class MultiPointToken<P extends Position> extends GeometryToken<P> {
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
            return new MultiPoint<P>(parts);
        }

    }


    public static class MultiLineStringToken<P extends Position> extends GeometryToken<P> {
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
            return new MultiLineString<P>(parts);
        }

    }

    public static class MultiPolygonToken<P extends Position> extends GeometryToken<P> {
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
            return new MultiPolygon<P>(parts);
        }

    }

}
