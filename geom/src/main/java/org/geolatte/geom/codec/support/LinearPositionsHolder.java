package org.geolatte.geom.codec.support;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.WktDecodeException;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;

import static org.geolatte.geom.GeometryType.LINESTRING;
import static org.geolatte.geom.GeometryType.MULTIPOINT;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class LinearPositionsHolder extends Holder {

    final private List<PointHolder> pointHolderList = new ArrayList<>();

    public void push(PointHolder holder) {
        pointHolderList.add(holder);
    }

    @Override
    public boolean isEmpty() {
        return pointHolderList.isEmpty();
    }

    @Override
    public int getCoordinateDimension() {
        return pointHolderList.stream().mapToInt(Holder::getCoordinateDimension).max().orElse(0);
    }

    @Override
    public <P extends Position> Geometry<P> toGeometry(CoordinateReferenceSystem<P> crs, GeometryType geomType) {
        if(geomType == LINESTRING) {
            return isEmpty()? Geometries.mkEmptyLineString(crs): Geometries.mkLineString(toPositionSequence(crs), crs);
        }
        if (geomType == MULTIPOINT) {
            return isEmpty()? Geometries.mkEmptyMultiPoint(crs) : Geometries.mkMultiPoint(toPositionSequence(crs), crs);
        }

        throw new WktDecodeException("Can't convert this coordinates array to requested Geomtype: " + geomType);
    }

    public <P extends Position> PositionSequence<P> toPositionSequence(CoordinateReferenceSystem<P> crs) {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(pointHolderList.size(), crs.getPositionClass());
        pointHolderList.forEach(h -> builder.add(h.toPosition(crs)));
        return builder.toPositionSequence();
    }



}
