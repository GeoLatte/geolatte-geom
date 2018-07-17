package org.geolatte.geom.json;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;

import static org.geolatte.geom.GeometryType.LINESTRING;
import static org.geolatte.geom.GeometryType.MULTIPOINT;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
class LinearPositionsHolder extends Holder {

    final private List<PointHolder> spcs = new ArrayList<>();

    void push(PointHolder holder) {
        spcs.add(holder);
    }

    @Override
    boolean isEmpty() {
        return spcs.isEmpty();
    }

    @Override
    int getCoordinateDimension() {
        return spcs.stream().mapToInt(Holder::getCoordinateDimension).max().orElse(0);
    }

    @Override
    <P extends Position> Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs, GeometryType geomType) throws GeoJsonProcessingException {
        if(geomType == LINESTRING) {
            return isEmpty()? Geometries.mkEmptyLineString(crs): Geometries.mkLineString(toPositionSequence(crs), crs);
        }
        if (geomType == MULTIPOINT) {
            return isEmpty()? Geometries.mkEmptyMultiPoint(crs) : Geometries.mkMultiPoint(toPositionSequence(crs), crs);
        }
        throw new GeoJsonProcessingException("Can't convert this coordinates array to requested Geomtype: " + geomType);
    }

    <P extends Position> PositionSequence<P> toPositionSequence(CoordinateReferenceSystem<P> crs) {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(spcs.size(), crs.getPositionClass());
        spcs.forEach(h -> builder.add(h.toPosition(crs)));
        return builder.toPositionSequence();
    }



}
