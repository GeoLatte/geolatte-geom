package org.geolatte.geom.crs.trans.projections;

import org.geolatte.geom.DecimalDegree;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.geolatte.geom.crs.trans.CoordinateOperation;
import org.geolatte.geom.crs.trans.WithEpsgGOperationMethod;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

/**
 * Created by Karel Maesen, Geovise BVBA on 02/04/2018.
 */
public class PseudoMercator implements CoordinateOperation, WithEpsgGOperationMethod {

    private final Geographic2DCoordinateReferenceSystem baseCrs;
    private final double lonOfNO;
    private final double latOfNO;
    private final double falseE;
    private final double falseN;

    private final double a; // ellipsoid semi-major axis;



    public PseudoMercator() {
        this(WGS84, 0, 0, 0, 0);
    }

    /**
     *
     * @param baseCrs  the Base CRS for the projection (source coordinate system)
     * @param latOfNatOrigin Latitude of Natural Origin in radians
     * @param lonOfNatOrigin Longitude of Natural Origin in radians
     * @param falseE false Easting in meters
     * @param falseN false Northing in meters
     */
    public PseudoMercator(Geographic2DCoordinateReferenceSystem baseCrs, double latOfNatOrigin, double lonOfNatOrigin, double falseE, double falseN){
        this.baseCrs = baseCrs;
        this.latOfNO = latOfNatOrigin;
        this.lonOfNO = lonOfNatOrigin;
        this.falseE = falseE;
        this.falseN = falseN;
        this.a = baseCrs.getDatum().getEllipsoid().getSemiMajorAxis();
    }


    @Override
    public boolean isReversible() {
        return true;
    }

    @Override
    public int inCoordinateDimension() {
        return 2;
    }

    @Override
    public int outCoordinateDimension() {
        return 2;
    }

    @Override
    public void forward(double[] inCoordinate, double[] outCoordinate) {
        double l = Math.toRadians( inCoordinate[0]);
        double p = Math.toRadians( inCoordinate[1]);
        outCoordinate[0] = falseE + a*(l - lonOfNO);
        outCoordinate[1] = falseN + a * Math.log(Math.tan(Math.PI / 4 + p / 2));
    }

    @Override
    public void reverse(double[] inCoordinate, double[] outCoordinate) {
        double d = - (inCoordinate[1] - falseN) / a;
        double p = Math.PI/2 - 2*Math.atan( Math.pow(Math.E, d));
        double l = ((inCoordinate[0] - falseE)/a) + lonOfNO;
        outCoordinate[0] = Math.toDegrees(l);
        outCoordinate[1] = Math.toDegrees(p);
    }

    @Override
    public String getMethodId() {
        return "1024";
    }
}
