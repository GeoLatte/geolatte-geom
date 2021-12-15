package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.PositionSequenceBuilder;
import org.geolatte.geom.cga.CircularArcLinearizer;

import static org.geolatte.geom.PositionSequenceBuilders.fixedSized;

class Linearizer {

    private static final double DEFAULT_EPSILON = 0.0001;

    private final double epsilon;
    public Linearizer(){
        this(DEFAULT_EPSILON);
    }

    public Linearizer(double epsilon) {
        this.epsilon = epsilon;
    }

    <P extends Position> PositionSequence<P> linearize(PositionSequence<P> positions, boolean entireCirlce) {
        PositionSequence<P> result = null;
        int idx = 0;

        while (idx < positions.size() - 2) { //only iterate if we have at least three more points
            P p0 = positions.getPositionN(idx++);
            P p1 = positions.getPositionN(idx++);
            P p2 = positions.getPositionN(idx); //dont' increment, we repeat next iteration from this index

            CircularArcLinearizer<P> linearizer = new CircularArcLinearizer<P>(p0, p1, p2,
                    epsilon);

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
                result = add(result, ps);
            }
        }
        return result;
    }

    private <P extends Position> PositionSequence<P> add(PositionSequence<P> seq1, PositionSequence<P> seq2) {
        if (seq1 == null) {
            return seq2;
        }
        if (seq2 == null) {
            return seq1;
        }
        int totalSize = seq1.size() + seq2.size() - 1;
        PositionSequenceBuilder<P> builder = fixedSized(totalSize, seq1.getPositionClass());
        AbstractSDODecoder.CombiningVisitor<P> visitor = new AbstractSDODecoder.CombiningVisitor<P>(builder);

        addToBuilder(seq1, 0, builder, visitor);
        addToBuilder(seq2, 1, builder, visitor);
        return builder.toPositionSequence();

    }

    private <P extends Position> void addToBuilder(PositionSequence<P> seq, int skip, PositionSequenceBuilder<P> builder, AbstractSDODecoder.CombiningVisitor<P> visitor) {
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

}
