package org.geolatte.geom.codec.support;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.geolatte.geom.GeometryType.MULTILINESTRING;
import static org.geolatte.geom.GeometryType.POLYGON;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class LinearPositionsListHolder extends Holder {

    final private List<LinearPositionsHolder> linearPositionsHolderList = new ArrayList<>();

    public void push(LinearPositionsHolder lph) {
        linearPositionsHolderList.add(lph);
    }

    @Override
    public boolean isEmpty() {
        return linearPositionsHolderList.isEmpty();
    }

    @Override
    public int getCoordinateDimension() {
        return linearPositionsHolderList.stream().mapToInt(Holder::getCoordinateDimension).max().orElse(0);
    }

    @Override
    public <P extends Position> Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs, GeometryType geomType) {
        if (geomType == POLYGON) {
            if (isEmpty()) {
                return Geometries.mkEmptyPolygon(crs);
            }
            return Geometries.mkPolygon(toLinearRings(crs));
        }

        if (geomType == MULTILINESTRING) {
            if (isEmpty()) {
                return Geometries.mkEmptyMultiLineString(crs);
            }
            return Geometries.mkMultiLineString(toLineStrings(crs));
        }

        throw new DecodeException("Can't convert this coordinates array to requested Geomtype: " + geomType);
    }

    <P extends Position> List<LinearRing<P>> toLinearRings(CoordinateReferenceSystem<P> crs) {
        try {
            return linearPositionsHolderList.stream().map(lph -> new LinearRing<>(lph.toPositionSequence(crs), crs)).collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            throw new DecodeException("Failure to create LinearRings", ex);
        }
    }

    <P extends Position> List<LineString<P>> toLineStrings(CoordinateReferenceSystem<P> crs) {
        return linearPositionsHolderList.stream().map(lph -> new LineString<>(lph.toPositionSequence(crs), crs)).collect(Collectors.toList());
    }


}
