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
public class Builder {

    public static LineString lineString(int srid, Point2DToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d2D, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LineString(psBuilder.toPointSequence());
    }

    private static void addPoints(PointSequenceBuilder psBuilder, PointToken[] points) {
        for (PointToken pnt : points) {
            pnt.addTo(psBuilder);
        }
    }

    public static LineString lineString(int srid, Point2DMToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d2DM, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LineString(psBuilder.toPointSequence());
    }

    public static LineString lineString(int srid, Point3DToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d3D, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LineString(psBuilder.toPointSequence());
    }

    public static LineString lineString(int srid, Point3DMToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d3DM, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LineString(psBuilder.toPointSequence());
    }

    public static LinearRing linearRing(int srid, Point2DToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d2D, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LinearRing(psBuilder.toPointSequence());
    }

    public static LinearRing linearRing(int srid, Point2DMToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d2DM, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LinearRing(psBuilder.toPointSequence());
    }

    public static LinearRing linearRing(int srid, Point3DToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d3D, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LinearRing(psBuilder.toPointSequence());
    }

    public static LinearRing linearRing(int srid, Point3DMToken... points) {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(points.length, d3DM, CrsId.valueOf(srid));
        addPoints(psBuilder, points);
        return new LinearRing(psBuilder.toPointSequence());
    }

    public static <T extends PointToken> Polygon polygon(int srid, LinearRingToken<T>... rings) {
        LinearRing[] linearRings = new LinearRing[rings.length];
        int i = 0;
        for (LinearRingToken<?> lrt : rings) {
            linearRings[i++] = lrt.toLinearRing(srid);
        }
        return new Polygon(linearRings);
    }

    // LinearRing tokens
    public static LinearRingToken<Point2DToken> ring(Point2DToken... points) {
        return new LinearRingToken<Point2DToken>(d2D, points);
    }

    public static LinearRingToken<Point2DMToken> ring(Point2DMToken... points) {
        return new LinearRingToken<Point2DMToken>(d2DM, points);
    }

    public static LinearRingToken<Point3DToken> ring(Point3DToken... points) {
        return new LinearRingToken<Point3DToken>(d3D, points);
    }

    public static LinearRingToken<Point3DMToken> ring(Point3DMToken... points) {
        return new LinearRingToken<Point3DMToken>(d3DM, points);
    }

    public static class LinearRingToken<T extends PointToken> {
        T[] pointTokens;
        DimensionalFlag dimFlag;

        LinearRingToken(DimensionalFlag dimFlag, T... tokens) {
            this.pointTokens = tokens;
            this.dimFlag = dimFlag;
        }

        LinearRing toLinearRing(int srid) {
            PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(pointTokens.length, dimFlag, CrsId.valueOf(srid));
            for (PointToken pt : pointTokens){
                pt.addTo(psBuilder);
            }
            return new LinearRing(psBuilder.toPointSequence());
        }
    }


    //Point Tokens
    public static Point2DToken pnt2D(double x, double y) {
        return new Point2DToken(x, y);
    }

    public static Point2DMToken pnt2DM(double x, double y, double m) {
        return new Point2DMToken(x, y, m);
    }

    public static Point3DToken pnt3D(double x, double y, double z) {
        return new Point3DToken(x, y, z);
    }

    public static Point3DMToken pnt3DM(double x, double y, double z, double m) {
        return new Point3DMToken(x, y, z, m);
    }

    abstract static class PointToken {
        abstract void addTo(PointSequenceBuilder psBuilder);
    }

    public static class Point2DToken extends PointToken{
        protected final double x, y;

        Point2DToken(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        void addTo(PointSequenceBuilder psBuilder) {
            psBuilder.add(x, y);
        }
    }

    public static class Point2DMToken extends PointToken {
        protected final double x, y, m;

        Point2DMToken(double x, double y, double m) {
            this.x = x;
            this.y = y;
            this.m = m;
        }

        @Override
        void addTo(PointSequenceBuilder psBuilder) {
            psBuilder.add(x, y, m);
        }
    }

    public static class Point3DToken extends PointToken {
        protected final double x, y, z;

        Point3DToken(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        void addTo(PointSequenceBuilder psBuilder) {
            psBuilder.add(x, y, z);
        }
    }

    public static class Point3DMToken extends PointToken {
        protected final double x, y, z, m;

        Point3DMToken(double x, double y, double z, double m) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.m = m;
        }

        @Override
        void addTo(PointSequenceBuilder psBuilder) {
            psBuilder.add(x, y, z, m);
        }
    }


}
