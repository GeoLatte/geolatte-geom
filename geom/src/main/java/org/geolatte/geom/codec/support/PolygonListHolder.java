package org.geolatte.geom.codec.support;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class PolygonListHolder extends Holder {

    final private List<LinearPositionsListHolder> spcs = new ArrayList<>();

    public void push(LinearPositionsListHolder lph) {
        spcs.add(lph);
    }

    @Override
    public boolean isEmpty() {
        return spcs.isEmpty();
    }

    @Override
    public int getCoordinateDimension() {
        return spcs.stream().mapToInt(Holder::getCoordinateDimension).max().orElse(0);
    }

    @Override
    public <P extends Position> Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs, GeometryType geomType) {
        return isEmpty() ? Geometries.mkEmptyMultiPolygon(crs) : Geometries.mkMultiPolygon(toPolygons(crs));
    }

    <P extends Position> List<Polygon<P>> toPolygons(CoordinateReferenceSystem<P> crs) {
        return spcs.stream().map(lph ->
                lph.isEmpty() ? Geometries.mkEmptyPolygon(crs) : Geometries.mkPolygon(lph.toLinearRings(crs))
        ).collect(Collectors.toList());
    }

}
