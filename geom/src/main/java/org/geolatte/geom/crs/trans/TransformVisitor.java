package org.geolatte.geom.crs.trans;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-27.
 */
public class TransformVisitor<P extends Position, Q extends Position> implements GeometryVisitor<P> {

    final private TransformOperation<P, Q> operation;
    private Stack<Geometry<Q>> transformed = new Stack<>();

    public TransformVisitor(TransformOperation<P, Q> op) {
        this.operation = op;
    }

    @Override
    public void visit(Point<P> point) {
        if (point.isEmpty()) {
            this.transformed.push(new Point<>(operation.getTarget()));
        } else {
            transformed.push(new Point<>(operation.forward(point.getPosition()), operation.getTarget()));
        }
    }

    @Override
    public void visit(LineString<P> lineString) {

        if (lineString.isEmpty()) {
            transformed.push(new LineString<>(operation.getTarget()));
        } else {

            ConvertingVisitor llv = new ConvertingVisitor( lineString.getNumPositions());
            lineString.getPositions().accept(llv);
            transformed.push(new LineString<>(llv.build(), operation.getTarget()));
        }

    }

    @Override
    public void visit(Polygon<P> polygon) {
        if (polygon.isEmpty()) {
            transformed.push(new Polygon<>(operation.getTarget()));
        } else {
            LinearRing<Q>[] rings = (LinearRing<Q>[]) new LinearRing[polygon.getNumInteriorRing()+1];
            int idx = 0;
            for (LinearRing<P> ring: polygon.components()) {
                ConvertingVisitor llv = new ConvertingVisitor( ring.getNumPositions());
                ring.getPositions().accept(llv);
                rings[idx++] = new LinearRing<Q>(llv.build(), operation.getTarget());
            }
            transformed.push(new Polygon<>(rings));
        }
    }

    @Override
    public <G extends Geometry<P>> void visit(GeometryCollection<P, G> collection) {
        transformed.push(null); //add NULL marker
    }

    @Override
    public <G extends Geometry<P>> void endVisit(GeometryCollection<P, G> collection) {
        List<Geometry<Q>> parts = new ArrayList<>();
        while ( !transformed.isEmpty() ) {
            Geometry<Q> popped = transformed.pop();
            if (popped == null) break;
            parts.add(popped);
        }
        Collections.reverse(parts);
        transformed.push(Geometries.mkGeometry(collection.getClass(), parts));
    }

    public Geometry<Q> getTransformed() {
            return transformed.pop();
    }

    private class ConvertingVisitor implements LLAPositionVisitor {

        final private PositionSequenceBuilder<Q> builder;
        final private double[] coordinates;

        ConvertingVisitor(int size ) {
            builder = PositionSequenceBuilders.fixedSized(size, operation.getTarget().getPositionClass());
            int coordinateDimension =  operation.getTarget().getCoordinateDimension();
            coordinates = new double[coordinateDimension];
        }

        @Override
        public void visit(double[] inCoordinates) {
            operation.getOperation().forward(inCoordinates, coordinates);
            builder.add(coordinates);
        }

        PositionSequence<Q> build() {
            return builder.toPositionSequence();
        }
    }

}
