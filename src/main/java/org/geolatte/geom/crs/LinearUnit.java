package org.geolatte.geom.crs;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class LinearUnit extends Unit {
    /**
     * Creates an instance.
     *
     * @param crsId
     * @param name
     * @param conversionFactor
     */
    public LinearUnit(CrsId crsId, String name, double conversionFactor) {
        super(crsId, name, conversionFactor);
    }

    @Override
    public LinearUnit getFundamentalUnit() {
        return Unit.METER;
    }

    @Override
    public boolean isAngular() {
        return false;
    }

}
