package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.CircularArcLinearizer;
import org.geolatte.geom.codec.db.Decoder;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LinearUnit;

import static org.geolatte.geom.PositionSequenceBuilders.fixedSized;

/**
 * Created by Karel Maesen, Geovise BVBA on 17/02/15.
 */
abstract public class AbstractSDODecoder implements Decoder<SDOGeometry> {


    //TODO -- this should be parameterized.
    private static double LINEARIZER_EPSILON = 0.0001;
    private CoordinateReferenceSystem<?> crs;


    @Override
    public Geometry<?> decode(SDOGeometry nativeGeom) {
        if (!accepts(nativeGeom)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " received object of type " + nativeGeom.getGType());
        }
        this.crs = determineCRS(nativeGeom);
        return internalDecode(nativeGeom);
    }

    /**
     * Determine the SRID, by considering the srid value to be an EPSG code.
     *
     * @param nativeGeom
     * @return
     */
    CoordinateReferenceSystem<?> getCoordinateReferenceSystem(SDOGeometry nativeGeom) {
        return crs;
    }

    private CoordinateReferenceSystem<?> determineCRS(SDOGeometry nativeGeom) {

        final int srid = nativeGeom.getSRID();

        CoordinateReferenceSystem<?> crs = CrsRegistry.ifAbsentReturnProjected2D(srid);

        if (getVerticalDimension(nativeGeom) > 0) {
            crs = CoordinateReferenceSystems.addVerticalSystem(crs, LinearUnit.METER);
        }

        if (getLinearReferenceDimension(nativeGeom) > 0) {
            crs = CoordinateReferenceSystems.addLinearSystem(crs, LinearUnit.METER);
        }
        return crs;
    }

    int getCoordinateDimension(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getDimension();
    }

    int getLinearReferenceDimension(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getLRSDimension();
    }

    int getVerticalDimension(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getZDimension();
    }

    //Note in these and subsequent private methods we pass in the crs so that type inferencing can occur
    protected <P extends Position> PositionSequence<P> convertOrdinateArray(Double[] oordinates, SDOGeometry sdoGeom, CoordinateReferenceSystem<P> crs) {
        final int dim = sdoGeom.getDimension();
        int numPos = oordinates.length / dim;

        PositionSequenceBuilder<P> sequenceBuilder = fixedSized(numPos, crs.getPositionClass());

        final int zDim = sdoGeom.getZDimension() - 1;
        final int lrsDim = sdoGeom.getLRSDimension() - 1;

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

    protected <P extends Position> PositionSequence<P> add(PositionSequence<P> seq1, PositionSequence<P> seq2) {
        return add(seq1, 0, seq2, 0);
    }

    protected <P extends Position> PositionSequence<P> add(PositionSequence<P> seq1, int seq1Offset, PositionSequence<P> seq2, int seq2Offset) {
        if (seq1 == null) {
            return seq2;
        }
        if (seq2 == null) {
            return seq1;
        }
        int totalSize = seq1.size() - seq1Offset + seq2.size() - seq2Offset;
        PositionSequenceBuilder<P> builder = fixedSized(totalSize, seq1.getPositionClass());
        CombiningVisitor<P> visitor = new CombiningVisitor<P>(builder);

        addToBuilder(seq1, seq1Offset, builder, visitor);
        addToBuilder(seq2, seq2Offset, builder, visitor);
        return builder.toPositionSequence();

    }

    private <P extends Position> void addToBuilder(PositionSequence<P> seq, int skip, PositionSequenceBuilder<P> builder, CombiningVisitor<P> visitor) {
        if (skip == 0) {
            seq.accept(visitor);
        } else {
            for (P pos : seq) {
                if (skip-- <= 0) {
                    builder.add(pos);
                }
            }
        }
    }


    abstract Geometry<?> internalDecode(SDOGeometry nativeGeom);


    static class CombiningVisitor<P extends Position> implements LLAPositionVisitor {

        final PositionSequenceBuilder<P> builder;

        CombiningVisitor(PositionSequenceBuilder<P> builder) {
            this.builder = builder;
        }

        /**
         * The visit method that is executed for each coordinate.
         *
         * @param coordinate the visited coordinate in array representation
         */
        @Override
        public void visit(double[] coordinate) {
            this.builder.add(coordinate);
        }

        PositionSequence<P> result() {
            return builder.toPositionSequence();
        }
    }

    /**
     * Gets the CoordinateSequence corresponding to a compound element.
     *
     * @param idxFirst the first sub-element of the compound element
     * @param idxLast  the last sub-element of the compound element
     * @param sdoGeom  the SDOGeometry that holds the compound element.
     * @return
     */
    protected <P extends Position> PositionSequence<P> getCompoundCSeq(int idxFirst, int idxLast, SDOGeometry sdoGeom) {
        PositionSequence<P> cs = null;
        for (int i = idxFirst; i <= idxLast; i++) {
            // pop off the last element as it is added with the next
            // coordinate sequence
            if (cs != null && cs.size() > 0) {
                cs = add(cs, 0, getElementCSeq(i, sdoGeom, (i < idxLast), (CoordinateReferenceSystem<P>) crs), 1);
            } else {
                cs = add(cs, getElementCSeq(i, sdoGeom, (i < idxLast), (CoordinateReferenceSystem<P>) crs));
            }
        }
        return cs;
    }

    /**
     * Gets the CoordinateSequence corresponding to an element.
     *
     * @param i
     * @param sdoGeom
     * @return
     */
    protected <P extends Position> PositionSequence<P> getElementCSeq(int i, SDOGeometry sdoGeom, boolean hasNextSE, CoordinateReferenceSystem<P> crs) {
        final ElementType type = sdoGeom.getInfo().getElementType(i);
        final Double[] elemOrdinates = extractOrdinatesOfElement(i, sdoGeom, hasNextSE);
        PositionSequence<P> cs;
        if (type.isStraightSegment()) {
            cs = convertOrdinateArray(elemOrdinates, sdoGeom, crs);
        } else if (type.isArcSegment() || type.isCircle()) {
            cs = linearize(convertOrdinateArray(elemOrdinates, sdoGeom, crs),
                    type.isCircle());
        } else if (type.isRect()) {
            cs = convertOrdinateArray(elemOrdinates, sdoGeom, crs);
            cs = env2Seq(cs.getPositionN(0), cs.getPositionN(1), type.isExteriorRing());
        } else {
            throw new RuntimeException(
                    "Unexpected Element type in compound: "
                            + type
            );
        }
        return cs;
    }

    private <P extends Position> PositionSequence<P> env2Seq(P p0, P p1, boolean asExteriorRing) {
        Envelope<P> env = new Envelope<P>(p0, p1, (CoordinateReferenceSystem<P>) crs);
        if (asExteriorRing) {
            return fixedSized(5, (Class<P>) crs.getPositionClass())
                    .add(env.lowerLeft())
                    .add(env.lowerRight())
                    .add(env.upperRight())
                    .add(env.upperLeft())
                    .add(env.lowerLeft()).toPositionSequence();
        } else {
            return fixedSized(5, (Class<P>) crs.getPositionClass())
                    .add(env.lowerLeft())
                    .add(env.upperLeft())
                    .add(env.upperRight())
                    .add(env.lowerRight())
                    .add(env.lowerLeft()).toPositionSequence();
        }
    }

    /**
     * Linearizes arcs and circles.
     *
     * @return linearized interpolation of arcs or circle
     */
    protected <P extends Position> PositionSequence<P> linearize(PositionSequence<P> positions, boolean entireCirlce) {
        PositionSequence<P> result = null;
        int idx = 0;

        while (idx < positions.size() - 2) { //only iterate if we have at least three more points
            P p0 = positions.getPositionN(idx++);
            P p1 = positions.getPositionN(idx++);
            P p2 = positions.getPositionN(idx); //dont' increment, we repeat next iteration from this index

            CircularArcLinearizer<P> linearizer = new CircularArcLinearizer<P>(p0, p1, p2,
                    LINEARIZER_EPSILON);


            PositionSequence<P> ps;
            if (entireCirlce) {
                ps = linearizer.linearizeCircle();
            } else {
                ps = linearizer.linearize();
            }


            // if this is not the first arcsegment, the first linearized
            // point is already in linearizedArc, so disregard this.
            if (result == null) { // this is the first iteration
                result = ps;
            } else {
                result = add(result, 0, ps, 1);
            }
        }
        return result;
    }

    protected Double[] extractOrdinatesOfElement(int element, SDOGeometry sdoGeom, boolean hasNextSE) {
        final int start = sdoGeom.getInfo().getOrdinatesOffset(element);
        if (element < sdoGeom.getInfo().getSize() - 1) {
            int end = sdoGeom.getInfo().getOrdinatesOffset(element + 1);
            // if this is a subelement of a compound geometry,
            // the last point is the first point of
            // the next subelement.
            if (hasNextSE) {
                end += sdoGeom.getDimension();
            }
            return sdoGeom.getOrdinates().getOrdinatesArray(start, end);
        } else {
            return sdoGeom.getOrdinates().getOrdinatesArray(start);
        }
    }



}
