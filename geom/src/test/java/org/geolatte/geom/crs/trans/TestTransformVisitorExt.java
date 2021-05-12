package org.geolatte.geom.crs.trans;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.junit.Test;

import static org.geolatte.geom.AssertHelpers.assertEquals;
import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.*;

/**
 * Test transform Visitor that make use of higher dimensions
 *
 */
public class TestTransformVisitorExt {
    private CoordinateReferenceSystem<C2D> source = PROJECTED_2D_METER;
    private CoordinateReferenceSystem<C3DM> target = PROJECTED_3DM_METER;

    private TransformOperation<C2D, C3DM> op = TransformOperations.from(source, target);
    private TransformVisitor<C2D, C3DM> visitor = new TransformVisitor<>(op);
    private TransformVisitor<C3DM, C2D> invisitor = new TransformVisitor<>(op.reversed());

    @Test
    public void testTransformLineString() {
        LineString<C2D> line = linestring(source, c(5.32, 51.3), c(4.89, 50.76));
        line.accept(visitor);
        Geometry<C3DM> ext = visitor.getTransformed();
        assertEquals(linestring(
                target,
                c(5.32, 51.3, 0.0, 0.0), c(4.89, 50.76, 0.0, 0.0)),
                ext);

        ext.accept(invisitor);
        assertEquals(line, invisitor.getTransformed());
    }

    @Test
    public void testTransformPoint() {
        Point<C2D> pnt = point(source, c(5.32, 51.3));
        pnt.accept(visitor);
        Geometry<C3DM> ext = visitor.getTransformed();
        assertEquals(point(target,c(5.32, 51.3, 0.0, 0.0)),
                ext);
        ext.accept(invisitor);
        assertEquals(pnt, invisitor.getTransformed());
    }
}
