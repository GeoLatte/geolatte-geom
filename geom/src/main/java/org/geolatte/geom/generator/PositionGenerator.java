package org.geolatte.geom.generator;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Position;
import org.geolatte.geom.Positions;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
public class PositionGenerator {

    public static <P extends Position>  P positionWithin(Envelope<P> bbox, Random rnd) {
        P ll = bbox.lowerLeft();
        P ur = bbox.upperRight();

        CoordinateReferenceSystem<P> crs = bbox.getCoordinateReferenceSystem();
        double[] coords = new double[crs.getCoordinateDimension()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = ll.getCoordinate(i) + (ur.getCoordinate(i) - ll.getCoordinate(i)) * rnd.nextDouble();
        }
        return Positions.mkPosition(crs, coords);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Position> P[] nPositionsWithin(int size, Envelope<P> bbox, Random rnd) {
        List<P> ret = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            ret.add(positionWithin(bbox, rnd));
        }
        return (P[])ret.toArray();
    }

    public static <P extends Position> P[] nPositionsWithinAndClosed(int size, Envelope<P> bbox, Random rnd) {
        P[] ps = nPositionsWithin(size, bbox, rnd);
        ps[ps.length - 1] = ps[0];
        return ps;
    }
}
