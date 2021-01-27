package org.geolatte.geom.json;

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

    @Override
    <P extends Position> Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs, GeometryType geomType) throws GeoJsonProcessingException {
        if (geomType == POLYGON) {
            if (isEmpty()) {
                return Geometries.mkEmptyPolygon(crs);
            }
            return Geometries.mkPolygon(toLinearRings(crs));
        }

        if(geomType == MULTILINESTRING) {
            if (isEmpty()){
                return Geometries.mkEmptyMultiLineString(crs);
            }
            return Geometries.mkMultiLineString(toLineStrings(crs));
        }

        throw new GeoJsonProcessingException("Can't convert this coordinates array to requested Geomtype: " + geomType);
    }

    <P extends Position> List<LinearRing<P>> toLinearRings(CoordinateReferenceSystem<P> crs) {
        return spcs.stream().map(lph -> new LinearRing<>(lph.toPositionSequence(crs), crs)).collect(Collectors.toList());
    }

    <P extends Position> List<LineString<P>> toLineStrings(CoordinateReferenceSystem<P> crs) {
        return spcs.stream().map(lph -> new LineString<>(lph.toPositionSequence(crs), crs)).collect(Collectors.toList());
    }


}
