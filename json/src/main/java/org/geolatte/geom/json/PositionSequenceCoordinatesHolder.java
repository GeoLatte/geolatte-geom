package org.geolatte.geom.json;

import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.PositionSequenceBuilder;
import org.geolatte.geom.PositionSequenceBuilders;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
class PositionSequenceCoordinatesHolder extends CoordinatesHolder {

    final private List<SinglePositionCoordinatesHolder> spcs = new ArrayList<>();

    void push(SinglePositionCoordinatesHolder holder) {
        spcs.add(holder);
    }

    @Override
    boolean isEmpty() {
        return spcs.isEmpty();
    }

    @Override
    int getCoordinateDimension() {
        return spcs.stream().mapToInt(CoordinatesHolder::getCoordinateDimension).max().orElse(0);
    }

    <P extends Position> PositionSequence<P> toPositionSequence(CoordinateReferenceSystem<P> crs) {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(spcs.size(), crs.getPositionClass());
        spcs.forEach(h -> builder.add(h.toPosition(crs)));
        return builder.toPositionSequence();
    }


}
