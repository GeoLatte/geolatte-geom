package org.geolatte.geom.crs;

/**
 * Angular units
 *
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class AngularUnit extends Unit {

    /**
     * Creates an instance.
     *
     * @param crsId
     * @param name
     * @param conversionFactor
     */
    public AngularUnit(CrsId crsId, String name, double conversionFactor) {
        super(crsId, name, conversionFactor);
    }

    @Override
    public AngularUnit getFundamentalUnit() {
        return Unit.RADIAN;
    }

    @Override
    public boolean isAngular() {
        return true;
    }

}
