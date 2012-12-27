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
 * Contains a Domain Specific Language for constructing Geometries.
 *
 * Usage example:
 * <pre>
 * {@code
 *
 * import static org.geolatte.geom.builder.DSL.*;
 *
 * public Polygon createPolygon() {
 *     return polygon(4326, ring(c(0, 0), c(0, 1), c(1, 1), c(1, 0), c(0, 0)));
 * }
 *
 * }
 * </pre>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class DSL {

    public static Vertex2DToken[] empty() {
        return new Vertex2DToken[0];
    }

    public static Point point(int srid, Vertex2DToken token) {
        return new Point2DToken(token).toGeometry(srid);
    }

    public static Point point(int srid, Vertex3DToken token) {
        return new Point3DToken(token).toGeometry(srid);
    }

    public static Point point(int srid, Vertex2DMToken token) {
        return new Point2DMToken(token).toGeometry(srid);
    }

    public static Point point(int srid, Vertex3DMToken token) {
        return new Point3DMToken(token).toGeometry(srid);
    }

    public static LineString linestring(int srid, Vertex2DToken... points) {
        return new LineString2DToken(points).toGeometry(srid);
    }

    public static LineString linestring(int srid, Vertex2DMToken... points) {
        return new LineString2DMToken(points).toGeometry(srid);
    }

    public static LineString linestring(int srid, Vertex3DToken... points) {
        return new LineString3DToken(points).toGeometry(srid);
    }

    public static LineString linestring(int srid, Vertex3DMToken... points) {
        return new LineString3DMToken(points).toGeometry(srid);
    }

    public static LinearRing ring(int srid, Vertex2DToken... points) {
        return new LinearRing2DToken(points).toGeometry(srid);
    }

    public static LinearRing ring(int srid, Vertex2DMToken... points) {
        return new LinearRing2DMToken(points).toGeometry(srid);
    }

    public static LinearRing ring(int srid, Vertex3DToken... points) {
        return new LinearRing3DToken(points).toGeometry(srid);
    }

    public static LinearRing ring(int srid, Vertex3DMToken... points) {
        return new LinearRing3DMToken(points).toGeometry(srid);
    }

    public static GeometryCollection geometrycollection(int srid, Geometry2DToken... geometryTokens) {
        return new GeometryCollection2DToken(geometryTokens).toGeometry(srid);
    }

    public static GeometryCollection geometrycollection(int srid, Geometry3DToken... geometryTokens) {
        return new GeometryCollection3DToken(geometryTokens).toGeometry(srid);
    }

    public static GeometryCollection geometrycollection(int srid, Geometry2DMToken... geometryTokens) {
        return new GeometryCollection2DMToken(geometryTokens).toGeometry(srid);
    }

    public static GeometryCollection geometrycollection(int srid, Geometry3DMToken... geometryTokens) {
        return new GeometryCollection3DMToken(geometryTokens).toGeometry(srid);
    }


    public static Polygon polygon(int srid, LinearRing2DToken... rings) {
        return new Polygon2DToken(rings).toGeometry(srid);
    }

    public static Polygon polygon(int srid, LinearRing3DToken... rings) {
        return new Polygon3DToken(rings).toGeometry(srid);
    }

    public static Polygon polygon(int srid, LinearRing2DMToken... rings) {
        return new Polygon2DMToken(rings).toGeometry(srid);
    }

    public static Polygon polygon(int srid, LinearRing3DMToken... rings) {
        return new Polygon3DMToken(rings).toGeometry(srid);
    }

    public static MultiPoint multipoint(int srid, Point2DToken... pointTokens) {
        return new MultiPoint2DToken(pointTokens).toGeometry(srid);
    }

    public static MultiPoint multipoint(int srid, Point3DToken... pointTokens) {
        return new MultiPoint3DToken(pointTokens).toGeometry(srid);
    }

    public static MultiPoint multipoint(int srid, Point2DMToken... pointTokens) {
        return new MultiPoint2DMToken(pointTokens).toGeometry(srid);
    }

    public static MultiPoint multipoint(int srid, Point3DMToken... pointTokens) {
        return new MultiPoint3DMToken(pointTokens).toGeometry(srid);
    }

    public static MultiLineString multilinestring(int srid, LineString2DToken... tokens) {
        return new MultiLineString2DToken(tokens).toGeometry(srid);
    }

    public static MultiLineString multilinestring(int srid, LineString3DToken... tokens) {
        return new MultiLineString3DToken(tokens).toGeometry(srid);
    }

    public static MultiLineString multilinestring(int srid, LineString2DMToken... tokens) {
        return new MultiLineString2DMToken(tokens).toGeometry(srid);
    }

    public static MultiLineString multilinestring(int srid, LineString3DMToken... tokens) {
        return new MultiLineString3DMToken(tokens).toGeometry(srid);
    }


    public static MultiPolygon multipolygon(int srid, Polygon2DToken... tokens) {
        return new MultiPolygon2DToken(tokens).toGeometry(srid);
    }

    public static MultiPolygon multipolygon(int srid, Polygon3DToken... tokens) {
        return new MultiPolygon3DToken(tokens).toGeometry(srid);
    }

    public static MultiPolygon multipolygon(int srid, Polygon2DMToken... tokens) {
        return new MultiPolygon2DMToken(tokens).toGeometry(srid);
    }

    public static MultiPolygon multipolygon(int srid, Polygon3DMToken... tokens) {
        return new MultiPolygon3DMToken(tokens).toGeometry(srid);
    }


    //GeometryCollection Tokens
    public static GeometryCollection2DToken geometrycollection(Geometry2DToken... geometryTokens) {
        return new GeometryCollection2DToken(geometryTokens);
    }

    public static GeometryCollection3DToken geometrycollection(Geometry3DToken... geometryTokens) {
        return new GeometryCollection3DToken(geometryTokens);
    }

    public static GeometryCollection2DMToken geometrycollection(Geometry2DMToken... geometryTokens) {
        return new GeometryCollection2DMToken(geometryTokens);
    }

    public static GeometryCollection3DMToken geometrycollection(Geometry3DMToken... geometryTokens) {
        return new GeometryCollection3DMToken(geometryTokens);
    }

    private abstract static class GeometryCollectionToken extends ComposedGeometryToken<GeometryCollection> {

        GeometryCollectionToken(GeometryToken... tokens) {
            super(tokens);
        }

        @Override
        public GeometryCollection toGeometry(int srid) {
            Geometry[] geoms = mkGeometryArray();
            loadGeometries(srid, geoms);
            return toGeometryCollection(geoms);
        }

        protected void loadGeometries(int srid, Geometry[] geoms) {
            int i = 0;
            for (GeometryToken token : geometryTokens) {
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

    public static class GeometryCollection2DToken extends GeometryCollectionToken implements Geometry2DToken{
        GeometryCollection2DToken(Geometry2DToken... tokens) {
            super(tokens);
        }
    }

    public static class GeometryCollection3DToken extends GeometryCollectionToken implements Geometry3DToken {
        GeometryCollection3DToken(Geometry3DToken... tokens) {
            super(tokens);
        }
    }

    public static class GeometryCollection2DMToken extends GeometryCollectionToken implements Geometry2DMToken{
        GeometryCollection2DMToken(Geometry2DMToken... tokens) {
            super(tokens);
        }
    }

    public static class GeometryCollection3DMToken extends GeometryCollectionToken implements Geometry3DMToken {
        GeometryCollection3DMToken(Geometry3DMToken... tokens) {
            super(tokens);
        }
    }


    public static MultiPoint2DToken multipoint(Point2DToken... pointTokens) {
        return new MultiPoint2DToken(pointTokens);
    }

    public static MultiPoint3DToken multipoint(Point3DToken... pointTokens) {
        return new MultiPoint3DToken(pointTokens);
    }

    public static MultiPoint2DMToken multipoint(Point2DMToken... pointTokens) {
        return new MultiPoint2DMToken(pointTokens);
    }

    public static MultiPoint3DMToken multipoint(Point3DMToken... pointTokens) {
        return new MultiPoint3DMToken(pointTokens);
    }

    private abstract static class MultiPointToken extends GeometryCollectionToken {

        public MultiPointToken(GeometryToken... tokens) {
            super(tokens);
        }

        protected MultiPoint toGeometryCollection(Geometry[] geoms) {
            return new MultiPoint((Point[]) geoms);
        }

        protected Point[] mkGeometryArray() {
            return new Point[geometryTokens.length];
        }

        public MultiPoint toGeometry(int srid) {
            return (MultiPoint) super.toGeometry(srid);
        }

    }

    public static class MultiPoint2DToken extends MultiPointToken implements Geometry2DToken {
        MultiPoint2DToken(Point2DToken... tokens) {
            super(tokens);
        }
    }

    public static class MultiPoint3DToken extends MultiPointToken implements Geometry3DToken {
        MultiPoint3DToken(Point3DToken... tokens) {
            super(tokens);
        }
    }

    public static class MultiPoint2DMToken extends MultiPointToken implements Geometry2DMToken {
        MultiPoint2DMToken(Point2DMToken... tokens) {
            super(tokens);
        }
    }

    public static class MultiPoint3DMToken extends MultiPointToken implements Geometry3DMToken {
        MultiPoint3DMToken(Point3DMToken... tokens) {
            super(tokens);
        }
    }


    public static MultiLineString2DToken multilinestring(LineString2DToken... tokens) {
        return new MultiLineString2DToken(tokens);
    }

    public static MultiLineString3DToken multilinestring(LineString3DToken... tokens) {
        return new MultiLineString3DToken(tokens);
    }

    public static MultiLineString2DMToken multilinestring(LineString2DMToken... tokens) {
        return new MultiLineString2DMToken(tokens);
    }

    public static MultiLineString3DMToken multilinestring(LineString3DMToken... tokens) {
        return new MultiLineString3DMToken(tokens);
    }

    private abstract static class MultiLineStringToken extends GeometryCollectionToken {
        MultiLineStringToken(GeometryToken... tokens) {
            super(tokens);
        }

        protected MultiLineString toGeometryCollection(Geometry[] geoms) {
            return new MultiLineString((LineString[]) geoms);
        }

        protected LineString[] mkGeometryArray() {
            return new LineString[geometryTokens.length];
        }

        public MultiLineString toGeometry(int srid) {
            return (MultiLineString) super.toGeometry(srid);
        }

    }

    public static class MultiLineString2DToken extends MultiLineStringToken implements Geometry2DToken{
        MultiLineString2DToken(LineString2DToken... tokens) {
            super(tokens);
        }
    }

    public static class MultiLineString3DToken extends MultiLineStringToken implements Geometry3DToken {
        MultiLineString3DToken(LineString3DToken... tokens) {
            super(tokens);
        }
    }

    public static class MultiLineString2DMToken extends MultiLineStringToken implements Geometry2DMToken {
        MultiLineString2DMToken(LineString2DMToken... tokens) {
            super(tokens);
        }
    }

    public static class MultiLineString3DMToken extends MultiLineStringToken implements Geometry3DMToken {
        MultiLineString3DMToken(LineString3DMToken... tokens) {
            super(tokens);
        }
    }


    public static MultiPolygon2DToken multipolygon(Polygon2DToken... tokens) {
        return new MultiPolygon2DToken(tokens);
    }

    public static MultiPolygon3DToken multipolygon(Polygon3DToken... tokens) {
        return new MultiPolygon3DToken(tokens);
    }

    public static MultiPolygon2DMToken multipolygon(Polygon2DMToken... tokens) {
        return new MultiPolygon2DMToken(tokens);
    }

    public static MultiPolygon3DMToken multipolygon(Polygon3DMToken... tokens) {
        return new MultiPolygon3DMToken(tokens);
    }
    

    private abstract static class MultiPolygonToken extends GeometryCollectionToken {
        MultiPolygonToken(GeometryToken... tokens) {
            super(tokens);
        }

        protected MultiPolygon toGeometryCollection(Geometry[] geoms) {
            return new MultiPolygon((Polygon[]) geoms);
        }

        protected Polygon[] mkGeometryArray() {
            return new Polygon[geometryTokens.length];
        }

        public MultiPolygon toGeometry(int srid) {
            return (MultiPolygon) super.toGeometry(srid);
        }
    }

    public static class MultiPolygon2DToken extends MultiPolygonToken implements Geometry2DToken {
        MultiPolygon2DToken(Polygon2DToken... tokens) {
            super(tokens);
        }        
    }

    public static class MultiPolygon3DToken extends MultiPolygonToken implements Geometry3DToken {
        MultiPolygon3DToken(Polygon3DToken... tokens) {
            super(tokens);
        }        
    }

    public static class MultiPolygon2DMToken extends MultiPolygonToken implements Geometry2DMToken {
        MultiPolygon2DMToken(Polygon2DMToken... tokens) {
            super(tokens);
        }        
    }

    public static class MultiPolygon3DMToken extends MultiPolygonToken implements Geometry3DMToken {
        MultiPolygon3DMToken(Polygon3DMToken... tokens) {
            super(tokens);
        }        
    }
    
    

    //Polygon tokens
    public static Polygon2DToken polygon(LinearRing2DToken... rings) {
        return new Polygon2DToken(rings);
    }

    public static Polygon3DToken polygon(LinearRing3DToken... rings) {
        return new Polygon3DToken(rings);
    }

    public static Polygon2DMToken polygon(LinearRing2DMToken... rings) {
        return new Polygon2DMToken(rings);
    }

    public static Polygon3DMToken polygon(LinearRing3DMToken... rings) {
        return new Polygon3DMToken(rings);
    }

    private static abstract class PolygonToken extends ComposedGeometryToken<Polygon> {

        PolygonToken(GeometryToken... tokens) {
            super(tokens);
        }

        protected LinearRing[] toGeometryArray(int srid) {
            LinearRing[] result = new LinearRing[geometryTokens.length];
            int i = 0;
            for (GeometryToken token : geometryTokens) {
                result[i++] = ((LinearRingToken) token).toGeometry(srid);
            }
            return result;
        }

        public Polygon toGeometry(int srid) {
            return new Polygon((LinearRing[]) toGeometryArray(srid));
        }
    }

    public static class Polygon2DToken extends PolygonToken implements Geometry2DToken {
        Polygon2DToken(LinearRing2DToken... tokens) {
            super(tokens);
        }
    }

    public static class Polygon3DToken extends PolygonToken implements Geometry3DToken {
        Polygon3DToken(LinearRing3DToken... tokens) {
            super(tokens);
        }
    }

    public static class Polygon2DMToken extends PolygonToken implements Geometry2DMToken {
        Polygon2DMToken(LinearRing2DMToken... tokens) {
            super(tokens);
        }
    }

    public static class Polygon3DMToken extends PolygonToken implements Geometry3DMToken {
        Polygon3DMToken(LinearRing3DMToken... tokens) {
            super(tokens);
        }
    }


    // LinearRing tokens
    public static LinearRing2DToken ring(Vertex2DToken... points) {
        return new LinearRing2DToken(points);
    }

    public static LinearRing2DMToken ring(Vertex2DMToken... points) {
        return new LinearRing2DMToken(points);
    }

    public static LinearRing3DToken ring(Vertex3DToken... points) {
        return new LinearRing3DToken(points);
    }

    public static LinearRing3DMToken ring(Vertex3DMToken... points) {
        return new LinearRing3DMToken(points);
    }

    private abstract static class LinearRingToken extends SimpleGeometryToken<LinearRing> {
        LinearRingToken(VertexToken... tokens) {
            super(tokens);
        }

        public LinearRing toGeometry(int srid) {
            return new LinearRing(mkPointSequence(srid));
        }
    }

    public static class LinearRing2DToken extends LinearRingToken implements Geometry2DToken {
        LinearRing2DToken(Vertex2DToken... tokens) {
            super(tokens);
        }
    }

    public static class LinearRing3DToken extends LinearRingToken implements Geometry3DToken {
        LinearRing3DToken(Vertex3DToken... tokens) {
            super(tokens);
        }
    }

    public static class LinearRing2DMToken extends LinearRingToken implements Geometry2DMToken {
        LinearRing2DMToken(Vertex2DMToken... tokens) {
            super(tokens);
        }
    }

    public static class LinearRing3DMToken extends LinearRingToken implements Geometry3DMToken {
        LinearRing3DMToken(Vertex3DMToken... tokens) {
            super(tokens);
        }
    }


    //LineString tokens
    public static LineString2DToken linestring(Vertex2DToken... points) {
        return new LineString2DToken(points);
    }

    public static LineString2DMToken lineString(Vertex2DMToken... points) {
        return new LineString2DMToken(points);
    }

    public static LineString3DToken linestring(Vertex3DToken... points) {
        return new LineString3DToken(points);
    }

    public static LineString3DMToken lineString(Vertex3DMToken... points) {
        return new LineString3DMToken(points);
    }

    private abstract static class LineStringToken extends SimpleGeometryToken<LineString> {

        LineStringToken(VertexToken... tokens) {
            super(tokens);
        }

        @Override
        public LineString toGeometry(int srid) {
            return new LineString(mkPointSequence(srid));
        }
    }

    public static class LineString2DToken extends LineStringToken implements Geometry2DToken {
        LineString2DToken(Vertex2DToken... tokens) {
            super(tokens);
        }
    }

    public static class LineString3DToken extends LineStringToken implements Geometry3DToken {
        LineString3DToken(Vertex3DToken... tokens) {
            super(tokens);
        }
    }

    public static class LineString2DMToken extends LineStringToken implements Geometry2DMToken {
        LineString2DMToken(Vertex2DMToken... tokens) {
            super(tokens);
        }
    }

    public static class LineString3DMToken extends LineStringToken implements Geometry3DMToken {
        LineString3DMToken(Vertex3DMToken... tokens) {
            super(tokens);
        }
    }

    //Point Tokens
    public static Point2DToken point(Vertex2DToken token) {
        return new Point2DToken(token);
    }

    public static Point3DToken point(Vertex3DToken token) {
        return new Point3DToken(token);
    }

    public static Point2DMToken point(Vertex2DMToken token) {
        return new Point2DMToken(token);
    }

    public static Point3DMToken point(Vertex3DMToken token) {
        return new Point3DMToken(token);
    }

    public abstract static class PointToken extends SimpleGeometryToken<Point> {

        PointToken(VertexToken... token) {
            super(token);
        }

        @Override
        public Point toGeometry(int srid) {
            return new Point(mkPointSequence(srid));
        }
    }

    public static class Point2DToken extends PointToken implements Geometry2DToken {
        Point2DToken(Vertex2DToken token) {
            super(token);
        }
    }

    public static class Point3DToken extends PointToken implements Geometry3DToken {
        Point3DToken(Vertex3DToken token) {
            super(token);
        }
    }

    public static class Point2DMToken extends PointToken implements Geometry2DMToken {
        Point2DMToken(Vertex2DMToken token) {
            super(token);
        }
    }

    public static class Point3DMToken extends PointToken implements Geometry3DMToken {
        Point3DMToken(Vertex3DMToken token) {
            super(token);
        }
    }

    //    dimensional marker interfaces
    public abstract static interface GeometryToken {
        Geometry toGeometry(int srid);

        DimensionalFlag dimFlag();
    }

    public abstract static interface Geometry2DToken extends GeometryToken {
    }

    public abstract static interface Geometry3DToken extends GeometryToken {
    }

    public abstract static interface Geometry2DMToken extends GeometryToken {
    }

    public abstract static interface Geometry3DMToken extends GeometryToken {
    }


    //base classes for GeometryTokens
    private abstract static class AbstractGeometryToken<G extends Geometry> {
        DimensionalFlag dimFlag;

        abstract G toGeometry(int srid);

        public DimensionalFlag dimFlag() {
            return this.dimFlag;
        }

        void extractDimensionalFlag(VertexToken[] tokens) {
            this.dimFlag = (tokens == null || tokens.length == 0) ? DimensionalFlag.d2D : tokens[0].dFlag();
        }
    }

    private abstract static class SimpleGeometryToken<G extends Geometry> extends AbstractGeometryToken<G> {
        VertexToken[] pointTokens;

        SimpleGeometryToken(VertexToken... tokens) {
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

    private abstract static class ComposedGeometryToken<G extends Geometry> extends AbstractGeometryToken<G> {
        GeometryToken[] geometryTokens;
        DimensionalFlag dimFlag;

        ComposedGeometryToken(GeometryToken... tokens) {
            this.geometryTokens = tokens;
            this.dimFlag = (tokens == null || tokens.length == 0) ? DimensionalFlag.d2D : tokens[0].dimFlag();
        }

        protected Geometry[] toGeometryArray(int srid) {
            Geometry[] result = new Geometry[geometryTokens.length];
            int i = 0;
            for (GeometryToken token : geometryTokens) {
                result[i++] = token.toGeometry(srid);
            }
            return result;
        }

        public DimensionalFlag dimFlag() {
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
