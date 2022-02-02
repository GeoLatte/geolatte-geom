package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.LLAPositionVisitors;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.List;

import static org.geolatte.geom.PositionSequenceBuilders.fixedSized;

abstract class Element {
    //Combination of SDO_ETYPE and SDO_INTERPRETATION
    final private ElementType elementType;

    final Linearizer linearizer = new Linearizer();

    Element(ElementType elementType) {
        this.elementType = elementType;
    }

    ElementType getElementType() {
        return elementType;
    }

    abstract boolean isCompound();

    boolean isCircle() {
        return this.elementType.isCircle();
    }

    boolean isRect() {
        return this.elementType.isRect();
    }

    boolean isExteriorRing() {
        return this.elementType.isExteriorRing();
    }

    boolean isInteriorRing(){
        return this.elementType.isInteriorRing();
    }

    abstract <P extends Position> PositionSequence<P> linearizedPositions(SDOGType gtype, CoordinateReferenceSystem<P> crs);
}

class SimpleElement extends Element {
    //array with all ordinates for this elements AFTER linearisation
    final private Double[] ordinates;

    public SimpleElement(ElementType elementType, Double[] ordinates) {
        super(elementType);
        this.ordinates = ordinates;
    }

    boolean hasArcSegments() {
        return this.getElementType().hasArcSegment();
    }

    @Override
    boolean isCompound() {
        return false;
    }

    @Override
    <P extends Position> PositionSequence<P> linearizedPositions(SDOGType gtype, CoordinateReferenceSystem<P> crs) {
        PositionSequence<P> positions = toPositions(this.ordinates, gtype, crs);
        if (isCircle() || hasArcSegments()) {
            return this.linearizer.linearize(positions, isCircle());
        }

        //todo -- Can this be optimized??
        if (isRect()) {
            P lowerLeft = positions.first();
            P upperRight = positions.last();
            Envelope<P> env = new Envelope<>(lowerLeft, upperRight, crs);
            if (isExteriorRing()) {
                return fixedSized(5, crs.getPositionClass())
                        .add(env.lowerLeft())
                        .add(env.lowerRight())
                        .add(env.upperRight())
                        .add(env.upperLeft())
                        .add(env.lowerLeft()).toPositionSequence();
            } else {
                return fixedSized(5, crs.getPositionClass())
                        .add(env.lowerLeft())
                        .add(env.upperLeft())
                        .add(env.upperRight())
                        .add(env.lowerRight())
                        .add(env.lowerLeft()).toPositionSequence();
            }
        }
        return positions;
    }

    protected <P extends Position> PositionSequence<P> toPositions(Double[] oordinates, SDOGType gtype, CoordinateReferenceSystem<P> crs) {
        final int dim = gtype.getDimension();
        int numPos = oordinates.length / dim;

        PositionSequenceBuilder<P> sequenceBuilder = fixedSized(numPos, crs.getPositionClass());

        final int zDim = gtype.getZDimension() - 1;
        final int lrsDim = gtype.getLRSDimension() - 1;

        double[] buffer = new double[dim];
        for (int posIdx = 0; posIdx < numPos; posIdx++) {
            int componentIdx = 0; //tracks component in Position
            buffer[componentIdx] = oordinates[posIdx * dim + componentIdx]; //x
            componentIdx++;
            buffer[componentIdx] = oordinates[posIdx * dim + componentIdx]; //y
            if (zDim > 0) {
                componentIdx++;
                /*
                 * Z ordinate can be null! This is valid in ORACLE, but leads to an NPE because of autoboxing.
                 */
                Double zOrdinate = oordinates[posIdx * dim + zDim];
                buffer[componentIdx] = zOrdinate == null ? Double.NaN : zOrdinate;
            }
            if (lrsDim > 0) {
                componentIdx++;
                buffer[componentIdx] = oordinates[posIdx * dim + lrsDim];
            }

            sequenceBuilder.add(buffer);
        }
        return sequenceBuilder.toPositionSequence();
    }

}

class CompoundElement extends Element {
    final private List<Element> subelements;

    public CompoundElement(ElementType elementType, List<Element> subelements) {
        super(elementType);
        this.subelements = subelements;
    }

    @Override
    boolean isCompound() {
        return true;
    }

    List<Element> getSubelements() {
        return subelements;
    }

    @Override
    <P extends Position> PositionSequence<P> linearizedPositions(SDOGType gtype, CoordinateReferenceSystem<P> crs) {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.variableSized(crs.getPositionClass());
        LLAPositionVisitor visitor = LLAPositionVisitors.mkCombiningVisitor(builder);
        for (Element el : getSubelements()) {
            el.linearizedPositions(gtype, crs).accept(visitor);
        }
        return builder.toPositionSequence();
    }
}
