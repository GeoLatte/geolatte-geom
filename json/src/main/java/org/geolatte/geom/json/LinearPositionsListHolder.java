package org.geolatte.geom.json;

import org.geolatte.geom.LineString;
import org.geolatte.geom.LinearRing;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class LinearPositionsListHolder extends Holder {

    final private List<LinearPositionsHolder> spcs = new ArrayList<>();

    void push( LinearPositionsHolder lph) {
        spcs.add(lph);
    }

    @Override
    boolean isEmpty() {
        return spcs.isEmpty();
    }

    @Override
    int getCoordinateDimension() {
        return spcs.stream().mapToInt(Holder::getCoordinateDimension).max().orElse(0);
    }

    <P extends Position> List<LinearRing<P>> toLinearRings(CoordinateReferenceSystem<P> crs) {
        return spcs.stream().map(lph -> new LinearRing<>(lph.toPositionSequence(crs), crs)).collect(Collectors.toList());
    }

    <P extends Position> List<LineString<P>> toLineStrings(CoordinateReferenceSystem<P> crs) {
        return spcs.stream().map(lph -> new LineString<>(lph.toPositionSequence(crs), crs)).collect(Collectors.toList());
    }


}
