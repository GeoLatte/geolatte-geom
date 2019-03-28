package org.geolatte.geom.crs.trans;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WEB_MERCATOR;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

import static org.geolatte.geom.AssertHelpers.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-27.
 */
public class TestTransformVisitor {


    private CoordinateReferenceSystem<G2D> source = WGS84;
    private CoordinateReferenceSystem<C2D> target = WEB_MERCATOR;
    private TransformOperation<G2D, C2D> op = TransformOperations.from(source, target);

    private TransformVisitor<G2D, C2D> visitor = new TransformVisitor<>(op);

    @Test
    public void testPointTransform() {

        Point<G2D> pnt = point(WGS84, g(5, 50));
        pnt.accept(visitor);
        Geometry<C2D> projected = visitor.getTransformed();
        assertEquals(point(WEB_MERCATOR, c(556597.453966367, 6446275.84101716)), projected);
    }

    @Test
    public void testPointReverseTransform() {

        Point<C2D> pnt = point(WEB_MERCATOR, c(556597.453966367, 6446275.84101716));
        TransformVisitor<C2D, G2D> reversed = visitor.reversed();
        pnt.accept(reversed);
        Geometry<G2D> projected = reversed.getTransformed();
        assertEquals(point(WGS84, g(5, 50)), projected);
    }

    @Test
    public void testEmptyPointTransform() {

        Point<G2D> pnt = Geometries.mkEmptyPoint(WGS84);
        pnt.accept(visitor);
        Geometry<C2D> projected = visitor.getTransformed();
        assertEquals(Geometries.mkEmptyPoint(WEB_MERCATOR), projected);
    }


    @Test
    public void testLineStringTransform() {
        LineString<G2D> line = linestring(WGS84, g(5.32, 51.3), g(4.89, 50.76));
        line.accept(visitor);
        Geometry<C2D> projected = visitor.getTransformed();
        assertEquals(linestring(
                WEB_MERCATOR,
                c((592219.691020215), 6674532.79847308),
                c(544352.309979108, 6578949.80039655)
        ), projected);
    }

    @Test
    public void testPolygonTransform() {
        Polygon<G2D> poly = polygon(
                WGS84,
                ring(
                        g(5.32, 51.3),
                        g(5.32, 51.4),
                        g(5.33, 51.4),
                        g(5.33, 51.3),
                        g(5.32, 51.3)
                )
        );
        poly.accept(visitor);
        Geometry<C2D> projected = visitor.getTransformed();
        assertEquals(
                polygon(
                        WEB_MERCATOR,
                        ring(
                                c(592219.691020215, 6674532.79847308),
                                c(592219.691020215, 6692356.43526254),
                                c(593332.885928148, 6692356.43526254),
                                c(593332.885928148, 6674532.79847308),
                                c(592219.691020215, 6674532.79847308)
                        )
                ), projected
        );
    }

    @Test
    public void testMultiPointTransform() {
        MultiPoint<G2D> mpnt = multipoint(WGS84, g(5, 50), g(5.32, 51.4));
        mpnt.accept(visitor);
        Geometry<C2D> projected = visitor.getTransformed();
        assertEquals(multipoint(WEB_MERCATOR,
                c(556597.453966367, 6446275.84101716),
                c(592219.691020215, 6692356.43526254)), projected);
    }

    @Test
    public void testMultiLineStringTransform() {
        MultiLineString<G2D> mls = multilinestring(WGS84,
                linestring(g(5, 50), g(5.32, 51.4)),
                linestring(g(6, 51), g(6.32, 52.4))
        );
        mls.accept(visitor);
        Geometry<C2D> projected = visitor.getTransformed();
        assertEquals(multilinestring(WEB_MERCATOR,
                linestring(c(556597.453966367, 6446275.84101716), c(592219.691020215, 6692356.43526254)),
                linestring(c(667916.944759642, 6621293.72274017), c(703539.181813488, 6872776.25367374))
                ),
                projected);
    }

    @Test
    public void testNestedGeometryCollection() {
        GeometryCollection<G2D, Geometry<G2D>> gc = geometrycollection(WGS84,
                point(g(5, 50)),
                multipoint(point(g(5, 50)), point(g(5.32, 51.4))),
                linestring(g(5.32, 51.3), g(4.89, 50.76))
        );
        gc.accept(visitor);
        Geometry<C2D> projected = visitor.getTransformed();
        assertEquals(
                geometrycollection(WEB_MERCATOR,
                point(c(556597.453966367, 6446275.84101716)),
                multipoint(
                        point(c(556597.453966367, 6446275.84101716)),
                        point(c(592219.691020215, 6692356.43526254))
                ),
                linestring(
                        c((592219.691020215), 6674532.79847308),
                        c(544352.309979108, 6578949.80039655)
                )),
                projected);
    }

    @Test
    public void testLineStringTransform2DTo3D() {
        LineString<G2D> line = linestring(WGS84, g(5.32, 51.3), g(4.89, 50.76));
        TransformOperation<G2D, C3D> op = TransformOperations.from(source, MERCATOR_Z);
        TransformVisitor<G2D, C3D> visitor = new TransformVisitor<>(op);
        line.accept(visitor);
        Geometry<C3D> projected = visitor.getTransformed();
        assertEquals(linestring(
                MERCATOR_Z,
                c((592219.691020215), 6674532.79847308, 0.0),
                c(544352.309979108, 6578949.80039655, 0.0)
        ), projected);
    }

    @Test
    public void testLineStringTransform3DTo2D() {
        LineString<G3D> line = linestring(WGS84_Z, g(5.32, 51.3, 10.0), g(4.89, 50.76, 12.0));
        TransformOperation<G3D, C2D> op = TransformOperations.from(WGS84_Z, MERCATOR);
        TransformVisitor<G3D, C2D> visitor = new TransformVisitor<>(op);
        line.accept(visitor);
        Geometry<C2D> projected = visitor.getTransformed();
        assertEquals(linestring(
                MERCATOR,
                c((592219.691020215), 6674532.79847308),
                c(544352.309979108, 6578949.80039655)
        ), projected);
    }

    @Test
    public void testLineStringTransform3DTo2DM() {
        LineString<G3D> line = linestring(WGS84_Z, g(5.32, 51.3, 10.0), g(4.89, 50.76, 12.0));
        TransformOperation<G3D, C2DM> op = TransformOperations.from(WGS84_Z, MERCATOR_M);
        TransformVisitor<G3D, C2DM> visitor = new TransformVisitor<>(op);
        line.accept(visitor);
        Geometry<C2DM> projected = visitor.getTransformed();
        assertEquals(linestring(
                MERCATOR_M,
                cM((592219.691020215), 6674532.79847308, 0),
                cM(544352.309979108, 6578949.80039655, 0)
        ), projected);
    }


    @Test
    public void testLineStringTransform2DTo3DM() {
        LineString<G2D> line = linestring(WGS84, g(5.32, 51.3), g(4.89, 50.76));
        TransformOperation<G2D, C3DM> op = TransformOperations.from(WGS84, MERCATOR_ZM);
        TransformVisitor<G2D, C3DM> visitor = new TransformVisitor<>(op);
        line.accept(visitor);
        Geometry<C3DM> projected = visitor.getTransformed();
        assertEquals(linestring(
                MERCATOR_ZM,
                c((592219.691020215), 6674532.79847308, 0, 0),
                c(544352.309979108, 6578949.80039655, 0, 0)
        ), projected);
    }


    @Test
    public void testLineStringTransform3DTo3D() {
        LineString<G3D> line = linestring(WGS84_Z, g(5.32, 51.3, 10.0), g(4.89, 50.76, 12.0));
        TransformOperation<G3D, C3D> op = TransformOperations.from(WGS84_Z, MERCATOR_Z);
        TransformVisitor<G3D, C3D> visitor = new TransformVisitor<>(op);
        line.accept(visitor);
        Geometry<C3D> projected = visitor.getTransformed();
        assertEquals(linestring(
                MERCATOR_Z,
                c((592219.691020215), 6674532.79847308, 10.0),
                c(544352.309979108, 6578949.80039655, 12.0)
        ), projected);
    }


    @Test
    public void testLineStringTransform3DMTo3DM() {
        LineString<G3DM> line = linestring(WGS84_ZM, g(5.32, 51.3, 10.0, 1.0), g(4.89, 50.76, 12.0, 1.0));
        TransformOperation<G3DM, C3DM> op = TransformOperations.from(WGS84_ZM, MERCATOR_ZM);
        TransformVisitor<G3DM, C3DM> visitor = new TransformVisitor<>(op);
        line.accept(visitor);
        Geometry<C3DM> projected = visitor.getTransformed();
        assertEquals(linestring(
                MERCATOR_ZM,
                c((592219.691020215), 6674532.79847308, 10.0, 1.0),
                c(544352.309979108, 6578949.80039655, 12.0, 1.0)
        ), projected);
    }

    @Test
    public void testLineStringTransform3DMTo3DMReverse() {
        LineString<C3DM> line = linestring(
                MERCATOR_ZM,
                c((592219.691020215), 6674532.79847308, 10.0, 1.0),
                c(544352.309979108, 6578949.80039655, 12.0, 1.0)
        );
        TransformOperation<C3DM, G3DM> op = TransformOperations.from(MERCATOR_ZM, WGS84_ZM);
        TransformVisitor<C3DM, G3DM> visitor = new TransformVisitor<>(op);
        line.accept(visitor);
        Geometry<G3DM> projected = visitor.getTransformed();
        assertEquals(linestring(WGS84_ZM, g(5.32, 51.3, 10.0, 1.0), g(4.89, 50.76, 12.0, 1.0)), projected);
    }

    @Test
    public void testLineStringTransform3DTo3DReverse() {
        LineString<C3D> line = linestring(
                MERCATOR_Z,
                c((592219.691020215), 6674532.79847308, 10.0),
                c(544352.309979108, 6578949.80039655, 12.0));
        TransformOperation<C3D, G3D> op = TransformOperations.from(MERCATOR_Z, WGS84_Z);
        TransformVisitor<C3D, G3D> visitor = new TransformVisitor<>(op);
        line.accept(visitor);
        Geometry<G3D> projected = visitor.getTransformed();
        assertEquals(
                linestring(WGS84_Z, g(5.32, 51.3, 10.0), g(4.89, 50.76, 12.0))
                , projected);
    }


    //TODO -- add test for  linear axis in feet, not meters

}
