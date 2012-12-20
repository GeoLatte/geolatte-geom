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

import static org.geolatte.geom.DimensionalFlag.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class DSL {

    public static Vertex2DToken[] empty() {
        return new Vertex2DToken[0];
    }

    public static <T extends VertexToken> Point point(int srid, T pointToken) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(1, pointToken.dFlag(), CrsId.valueOf(srid));
        pointToken.addTo(psBuilder);
        return new Point(psBuilder.toPointSequence());
    }

    private static void addPoints(PointSequenceBuilder psBuilder, VertexToken[] points) {
        for (VertexToken pnt : points) {
            pnt.addTo(psBuilder);
        }
    }

    //TODO -- can now eliminate redundancy because vertexToken reports its dimension.
    public static LineString lineString(int srid, Vertex2DToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d2D, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LineString(psBuilder.toPointSequence());
    }

    public static LineString lineString(int srid, Vertex2DMToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d2DM, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LineString(psBuilder.toPointSequence());
    }

    public static LineString lineString(int srid, Vertex3DToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d3D, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LineString(psBuilder.toPointSequence());
    }

    public static LineString lineString(int srid, Vertex3DMToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d3DM, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LineString(psBuilder.toPointSequence());
    }

    public static LinearRing ring(int srid, Vertex2DToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d2D, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LinearRing(psBuilder.toPointSequence());
    }

    public static LinearRing ring(int srid, Vertex2DMToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d2DM, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LinearRing(psBuilder.toPointSequence());
    }

    public static LinearRing ring(int srid, Vertex3DToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d3D, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LinearRing(psBuilder.toPointSequence());
    }

    public static LinearRing ring(int srid, Vertex3DMToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d3DM, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LinearRing(psBuilder.toPointSequence());
    }

    public static <T extends VertexToken> GeometryCollection geometryCollection(int srid, GeometryToken<?, T>... geometryTokens) {
        return null;
    }


    public static <T extends VertexToken> Polygon polygon(int srid, LinearRingToken<T>... rings) {
        LinearRing[] linearRings = new LinearRing[rings.length];
        int i = 0;
        for (LinearRingToken<?> lrt : rings) {
            linearRings[i++] = lrt.toGeometry(srid);
        }
        return new Polygon(linearRings);
    }

//    //Polygon tokens
//    public static PolygonToken<T> polygon(LinearRingToken<T>... points) {
//            return new LinearRingToken<Vertex2DToken>(d2D, points);
//        }
//
//        public static LinearRingToken<Vertex2DMToken> ring(Vertex2DMToken... points) {
//            return new LinearRingToken<Vertex2DMToken>(d2DM, points);
//        }
//
//        public static LinearRingToken<Vertex3DToken> ring(Vertex3DToken... points) {
//            return new LinearRingToken<Vertex3DToken>(d3D, points);
//        }
//
//        public static LinearRingToken<Vertex3DMToken> ring(Vertex3DMToken... points) {
//            return new LinearRingToken<Vertex3DMToken>(d3DM, points);
//        }
//
//        public static class PolygonToken<T extends VertexToken> extends ComposedGeometryToken<Polygon, T> {
//            PolygonToken(DimensionalFlag dimFlag, GeometryToken<?,T>... tokens) {
//                super(dimFlag, tokens);
//            }
//
//            Polygon toGeometry(int srid) {
//                return new Polygon((LinearRing[])toGeometryArray(srid));
//            }
//        }


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
    public static LineStringToken<Vertex2DToken> lineString(Vertex2DToken... points) {
        return mkLineStringToken(points);
    }

    public static LineStringToken<Vertex2DMToken> lineString(Vertex2DMToken... points) {
        return mkLineStringToken(points);
    }

    public static LineStringToken<Vertex3DToken> lineString(Vertex3DToken... points) {
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
        abstract G toGeometry(int srid);
    }

    public abstract static class SimpleGeometryToken<G extends Geometry, T extends VertexToken> extends GeometryToken<G, T> {
        T[] pointTokens;
        DimensionalFlag dimFlag;

        SimpleGeometryToken(T... tokens) {
            this.pointTokens = tokens;
            this.dimFlag = (tokens == null || tokens.length == 0) ? DimensionalFlag.d2D : tokens[0].dFlag();
        }

        protected PointSequence mkPointSequence(int srid) {
            PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(pointTokens.length, dimFlag, CrsId.valueOf(srid));
            for (VertexToken pt : pointTokens) {
                pt.addTo(psBuilder);
            }
            return psBuilder.toPointSequence();
        }
    }

    public abstract static class ComposedGeometryToken<G extends Geometry, T extends VertexToken> extends GeometryToken<G,T>{
        GeometryToken<?, T>[] geometryTokens;
        DimensionalFlag dimFlag;
        ComposedGeometryToken(DimensionalFlag dimFlag, GeometryToken<?, T>... tokens){
            this.geometryTokens = tokens;
            this.dimFlag = dimFlag;
        }
        protected Geometry[] toGeometryArray(int srid){
            Geometry[] result = new Geometry[geometryTokens.length];
            int i = 0;
            for (GeometryToken<?, T> token : geometryTokens) {
                result[i++] = token.toGeometry(srid);
            }
            return result;
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
