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
import org.geolatte.geom.crs.CrsId;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class DSL {

    public static Vertex2DToken[] empty() {
        return new Vertex2DToken[0];
    }

    public static <T extends VertexToken> Point point(int srid, T pointToken) {
        return new PointToken(pointToken).toGeometry(srid);
    }

    private static <T extends VertexToken> LineString mkLineString(int srid, T... points) {
        return new LineStringToken<T>(points).toGeometry(srid);
    }

    public static LineString linestring(int srid, Vertex2DToken... points) {
        return mkLineString(srid, points);
    }

    public static LineString linestring(int srid, Vertex2DMToken... points) {
        return mkLineString(srid, points);
    }

    public static LineString linestring(int srid, Vertex3DToken... points) {
        return mkLineString(srid, points);
    }

    public static LineString linestring(int srid, Vertex3DMToken... points) {
        return mkLineString(srid, points);
    }

    private static <T extends VertexToken> LinearRing mkLinearRing(int srid, T... points) {
        return new LinearRingToken<T>(points).toGeometry(srid);
    }

    public static LinearRing ring(int srid, Vertex2DToken... points) {
        return mkLinearRing(srid, points);
    }

    public static LinearRing ring(int srid, Vertex2DMToken... points) {
        return mkLinearRing(srid, points);
    }

    public static LinearRing ring(int srid, Vertex3DToken... points) {
        return mkLinearRing(srid, points);
    }

    public static LinearRing ring(int srid, Vertex3DMToken... points) {
        return mkLinearRing(srid, points);
    }

    public static <T extends VertexToken> GeometryCollection geometrycollection(int srid, GeometryToken<?, T>... geometryTokens) {
        return new GeometryCollectionToken<T>(geometryTokens).toGeometry(srid);
    }


    public static <T extends VertexToken> Polygon polygon(int srid, LinearRingToken<T>... rings) {
        return new PolygonToken<T>(rings).toGeometry(srid);
    }

    public static <T extends VertexToken> MultiPoint multipoint(int srid, PointToken<T>... pointTokens) {
        return new MultiPointToken<T>(pointTokens).toGeometry(srid);
    }

    public static <T extends VertexToken> MultiLineString multilinestring(int srid, LineStringToken<T>... tokens) {
            return new MultiLineStringToken<T>(tokens).toGeometry(srid);
    }

    public static <T extends VertexToken> MultiPolygon multipolygon(int srid, PolygonToken<T>... tokens) {
            return new MultiPolygonToken<T>(tokens).toGeometry(srid);
    }


    //GeometryCollection Tokens
    public static <T extends VertexToken> GeometryCollectionToken<T> geometrycollection(GeometryToken<?, T>... geometryTokens) {
        return new GeometryCollectionToken<T>(geometryTokens);
    }

    public static class GeometryCollectionToken<T extends VertexToken> extends ComposedGeometryToken<GeometryCollection, T> {

        GeometryCollectionToken(GeometryToken... tokens) {
            super(tokens);
        }

        @Override
        GeometryCollection toGeometry(int srid) {
            Geometry[] geoms = mkGeometryArray();
            loadGeometries(srid, geoms);
            return toGeometryCollection(geoms);
        }

        protected void loadGeometries(int srid, Geometry[] geoms) {
            int i = 0;
            for (GeometryToken<?, T> token : geometryTokens) {
                geoms[i++] = token.toGeometry(srid);
            }
        }

        protected GeometryCollection toGeometryCollection(Geometry[] geoms) {
            return new GeometryCollection(geoms);
        }

        protected Geometry[] mkGeometryArray() {
            return new Geometry[geometryTokens.length];
        }
    }

    public static <T extends VertexToken> MultiPointToken<T> multipoint(PointToken<T>... pointTokens) {
        return new MultiPointToken<T>(pointTokens);
    }

    public static class MultiPointToken<T extends VertexToken> extends GeometryCollectionToken<T> {
        MultiPointToken(PointToken... tokens){
            super(tokens);
        }

        protected MultiPoint toGeometryCollection(Geometry[] geoms) {
            return new MultiPoint((Point[])geoms);
        }

        protected Point[] mkGeometryArray() {
            return new Point[geometryTokens.length];
        }

        MultiPoint toGeometry(int srid) {
            return (MultiPoint)super.toGeometry(srid);
        }

    }

    public static <T extends VertexToken> MultiLineStringToken<T> multilinestring(LineStringToken<T>... tokens) {
        return new MultiLineStringToken<T>(tokens);
    }

    public static class MultiLineStringToken<T extends VertexToken> extends GeometryCollectionToken<T> {
        MultiLineStringToken(LineStringToken... tokens){
            super(tokens);
        }

        protected MultiLineString toGeometryCollection(Geometry[] geoms) {
            return new MultiLineString((LineString[])geoms);
        }

        protected LineString[] mkGeometryArray() {
            return new LineString[geometryTokens.length];
        }

        MultiLineString toGeometry(int srid) {
            return (MultiLineString)super.toGeometry(srid);
        }

    }

    public static <T extends VertexToken> MultiPolygonToken<T> multipolygon(PolygonToken<T>... tokens) {
           return new MultiPolygonToken<T>(tokens);
       }

       public static class MultiPolygonToken<T extends VertexToken> extends GeometryCollectionToken<T> {
           MultiPolygonToken(PolygonToken... tokens){
               super(tokens);
           }

           protected MultiPolygon toGeometryCollection(Geometry[] geoms) {
               return new MultiPolygon((Polygon[])geoms);
           }

           protected Polygon[] mkGeometryArray() {
               return new Polygon[geometryTokens.length];
           }

           MultiPolygon toGeometry(int srid) {
               return (MultiPolygon)super.toGeometry(srid);
           }

       }

    //Polygon tokens
    public static <T extends VertexToken> PolygonToken<T> polygon(LinearRingToken<T>... rings) {
        return new PolygonToken<T>(rings);
    }

    public static class PolygonToken<T extends VertexToken> extends ComposedGeometryToken<Polygon, T> {

        PolygonToken(GeometryToken<?, T>... tokens) {
            super(tokens);
        }

        protected LinearRing[] toGeometryArray(int srid) {
            LinearRing[] result = new LinearRing[geometryTokens.length];
            int i = 0;
            for (GeometryToken<?, T> token : geometryTokens) {
                result[i++] = ((LinearRingToken<?>)token).toGeometry(srid);
            }
            return result;
        }

        Polygon toGeometry(int srid) {
            return new Polygon((LinearRing[]) toGeometryArray(srid));
        }
    }


    // LinearRing tokens
    public static LinearRingToken<Vertex2DToken> ring(Vertex2DToken... points) {
        return mkRing(points);
    }

    private static <T extends VertexToken> LinearRingToken<T> mkRing(T... points) {
        return new LinearRingToken<T>(points);
    }

    public static LinearRingToken<Vertex2DMToken> ring(Vertex2DMToken... points) {
        return mkRing(points);
    }

    public static LinearRingToken<Vertex3DToken> ring(Vertex3DToken... points) {
        return mkRing(points);
    }

    public static LinearRingToken<Vertex3DMToken> ring(Vertex3DMToken... points) {
        return mkRing(points);
    }

    public static class LinearRingToken<T extends VertexToken> extends SimpleGeometryToken<LinearRing, T> {
        LinearRingToken(T... tokens) {
            super(tokens);
        }

        LinearRing toGeometry(int srid) {
            return new LinearRing(mkPointSequence(srid));
        }
    }

    //LineString tokens
    private static <T extends VertexToken> LineStringToken<T> mkLineStringToken(T... points) {
        return new LineStringToken<T>(points);
    }

    public static LineStringToken<Vertex2DToken> linestring(Vertex2DToken... points) {
        return mkLineStringToken(points);
    }

    public static LineStringToken<Vertex2DMToken> lineString(Vertex2DMToken... points) {
        return mkLineStringToken(points);
    }

    public static LineStringToken<Vertex3DToken> linestring(Vertex3DToken... points) {
        return mkLineStringToken(points);
    }

    public static LineStringToken<Vertex3DMToken> lineString(Vertex3DMToken... points) {
        return mkLineStringToken(points);
    }

    public static class LineStringToken<T extends VertexToken> extends SimpleGeometryToken<LineString, T> {

        LineStringToken(T... tokens) {
            super(tokens);
        }

        @Override
        LineString toGeometry(int srid) {
            return new LineString(mkPointSequence(srid));
        }
    }

    //Point Tokens
    public static <T extends VertexToken> PointToken<T> point(T point) {
        return new PointToken<T>(point);
    }

    public static class PointToken<T extends VertexToken> extends SimpleGeometryToken<Point, T> {

        PointToken(T token) {
            super(token);
        }

        @Override
        Point toGeometry(int srid) {
            return new Point(mkPointSequence(srid));
        }
    }

    public abstract static class GeometryToken<G extends Geometry, T extends VertexToken> {
        DimensionalFlag dimFlag;

        abstract G toGeometry(int srid);

        DimensionalFlag dimFlag() {
            return this.dimFlag;
        }

        void extractDimensionalFlag(T[] tokens) {
            this.dimFlag = (tokens == null || tokens.length == 0) ? DimensionalFlag.d2D : tokens[0].dFlag();
        }
    }

    public abstract static class SimpleGeometryToken<G extends Geometry, T extends VertexToken> extends GeometryToken<G, T> {
        T[] pointTokens;

        SimpleGeometryToken(T... tokens) {
            this.pointTokens = tokens;
            extractDimensionalFlag(tokens);
        }

        protected PointSequence mkPointSequence(int srid) {
            PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(pointTokens.length, dimFlag, CrsId.valueOf(srid));
            for (VertexToken pt : pointTokens) {
                pt.addTo(psBuilder);
            }
            return psBuilder.toPointSequence();
        }

    }

    public abstract static class ComposedGeometryToken<G extends Geometry, T extends VertexToken> extends GeometryToken<G, T> {
        GeometryToken<?, T>[] geometryTokens;
        DimensionalFlag dimFlag;

        ComposedGeometryToken(GeometryToken<?, T>... tokens) {
            this.geometryTokens = tokens;
            this.dimFlag = (tokens == null || tokens.length == 0) ? DimensionalFlag.d2D : tokens[0].dimFlag();
        }

        protected Geometry[] toGeometryArray(int srid) {
            Geometry[] result = new Geometry[geometryTokens.length];
            int i = 0;
            for (GeometryToken<?, T> token : geometryTokens) {
                result[i++] = token.toGeometry(srid);
            }
            return result;
        }

        DimensionalFlag dimFlag() {
            return this.dimFlag;
        }
    }

    /**
     * A 2D vertex
     *
     * @param x the X-coordinate
     * @param y the Y-coordinate
     * @return
     */
    public static Vertex2DToken c(double x, double y) {
        return new Vertex2DToken(x, y);
    }

    /**
     * A 2DM vertex
     *
     * @param x
     * @param y
     * @param m
     * @return
     */
    public static Vertex2DMToken cM(double x, double y, double m) {
        return new Vertex2DMToken(x, y, m);
    }

    /**
     * A 3D vertex
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Vertex3DToken cZ(double x, double y, double z) {
        return new Vertex3DToken(x, y, z);
    }

    /**
     * A 3DM vertex
     *
     * @param x
     * @param y
     * @param z
     * @param m
     * @return
     */
    public static Vertex3DMToken c(double x, double y, double z, double m) {
        return new Vertex3DMToken(x, y, z, m);
    }

    abstract static class VertexToken {
        abstract void addTo(PointSequenceBuilder psBuilder);

        abstract DimensionalFlag dFlag();
    }

    public static class Vertex2DToken extends VertexToken {
        protected final double x, y;

        Vertex2DToken(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        void addTo(PointSequenceBuilder psBuilder) {
            psBuilder.add(x, y);
        }

        @Override
        DimensionalFlag dFlag() {
            return DimensionalFlag.d2D;
        }
    }

    public static class Vertex2DMToken extends VertexToken {
        protected final double x, y, m;

        Vertex2DMToken(double x, double y, double m) {
            this.x = x;
            this.y = y;
            this.m = m;
        }

        @Override
        void addTo(PointSequenceBuilder psBuilder) {
            psBuilder.add(x, y, m);
        }

        @Override
        DimensionalFlag dFlag() {
            return DimensionalFlag.d2DM;
        }
    }

    public static class Vertex3DToken extends VertexToken {
        protected final double x, y, z;

        Vertex3DToken(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        void addTo(PointSequenceBuilder psBuilder) {
            psBuilder.add(x, y, z);
        }

        @Override
        DimensionalFlag dFlag() {
            return DimensionalFlag.d3D;
        }
    }

    public static class Vertex3DMToken extends VertexToken {
        protected final double x, y, z, m;

        Vertex3DMToken(double x, double y, double z, double m) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.m = m;
        }

        @Override
        void addTo(PointSequenceBuilder psBuilder) {
            psBuilder.add(x, y, z, m);
        }

        @Override
        DimensionalFlag dFlag() {
            return DimensionalFlag.d3DM;
        }
    }


}
