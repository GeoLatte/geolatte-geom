package org.geolatte.geom.json;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
class PolygonListHolder extends Holder {

    final private List<LinearPositionsListHolder> spcs = new ArrayList<>();

    void push( LinearPositionsListHolder lph) {
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

    <P extends Position> List<Polygon<P>> toPolygons(CoordinateReferenceSystem<P> crs) {
        return spcs.stream().map(lph ->
                Geometries.mkPolygon(lph.toLinearRings(crs))
        ).collect(Collectors.toList());
    }

}
