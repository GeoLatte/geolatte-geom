package org.geolatte.geom.crs;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/4/14
 */
public interface Unit {

    /**
     * Type of <code>LengthUnit</code>: Linear or Angular.
     */
    public static enum Type {
        LINEAR,
        ANGULAR
    }

    public String getName();

    public boolean isAngular();

    public LengthUnit getFundamentalUnit(Type type);

    public double getConversionFactor();

}
