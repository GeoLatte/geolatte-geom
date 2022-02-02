package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.PositionSequenceBuilder;
import org.geolatte.geom.PositionSequenceBuilders;
import org.geolatte.geom.cga.CircularArcLinearizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Linearizer {

    private static final double DEFAULT_EPSILON = 0.0001;

    private final double epsilon;

    public Linearizer() {
        this(DEFAULT_EPSILON);
    }

    public Linearizer(double epsilon) {
        this.epsilon = epsilon;
    }

    <P extends Position> List<P> linearizeToList(PositionSequence<P> positions, boolean entireCircle) {
        List<P> result = new ArrayList<>();
        int idx = 0;

        while (idx < positions.size() - 2) { //only iterate if we have at least three more points
            P p0 = positions.getPositionN(idx++);
            P p1 = positions.getPositionN(idx++);
            P p2 = positions.getPositionN(idx); //dont' increment, we repeat next iteration from this index

            CircularArcLinearizer<P> linearizer = new CircularArcLinearizer<>(p0, p1, p2, epsilon);

            PositionSequence<P> ps;
            if (entireCircle) {
                result.addAll(linearizer.linearizeCircle().stream().collect(Collectors.toList()));
            } else {
                result.addAll(linearizer.linearize().stream().collect(Collectors.toList()));
            }
        }
        return result;
    }

    <P extends Position> PositionSequence<P> linearize(PositionSequence<P> positions, boolean entireCircle) {
        List<P> linearized = linearizeToList(positions, entireCircle);
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(linearized.size(), positions.getPositionClass());
        builder.addAll(linearized);
        return builder.toPositionSequence();
    }

}
