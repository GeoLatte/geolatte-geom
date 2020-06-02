package org.geolatte.geom.generator;

import org.geolatte.geom.Box;
import org.geolatte.geom.Position;
import org.geolatte.geom.Positions;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.Random;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
public class PositionGenerator<P extends Position> implements Generator<P> {

    private final Box<P> bbox;
    private final Random rnd;

    PositionGenerator(Box<P> bbox, Random rnd) {
        this.bbox = bbox;
        this.rnd = rnd;
    }

    public static <P extends Position> P positionWithin(Box<P> bbox, Random rnd) {
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
    public static <P extends Position> P[] nPositionsWithin(int size, Box<P> bbox, Random rnd) {
        P[] ret = (P[]) new Position[size];
        for (int i = 0; i < size; i++) {
            ret[i] = positionWithin(bbox, rnd);
        }
        return ret;
    }

    public static <P extends Position> P[] nPositionsWithinAndClosed(int size, Box<P> bbox, Random rnd) {
        P[] ps = nPositionsWithin(size, bbox, rnd);
        ps[ps.length - 1] = ps[0];
        return ps;
    }

    @Override
    public P generate() {
        return positionWithin(bbox, rnd);
    }

}
