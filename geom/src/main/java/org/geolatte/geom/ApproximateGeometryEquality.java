package org.geolatte.geom;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-27.
 */
public class ApproximateGeometryEquality extends GeometryPositionEquality {


    /**
     * Constructs an instance that determines equality by comparing the positions to within the
     * specified precision
     *
     * @param precision the precision to use when determining equality
     */
    public ApproximateGeometryEquality(double precision) {
        super(new WithinTolerancePositionEquality(precision));
    }

    @Override
    public <P extends Position> boolean equals(Geometry<P> first, Geometry<P> second) {
        return super.equals(first, second);
    }
}
