package org.geolatte.geom.crs.trans.projections;

import org.geolatte.geom.crs.Ellipsoid;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.geolatte.geom.crs.ProjectedCoordinateReferenceSystem;
import org.geolatte.geom.crs.Projection;
import org.geolatte.geom.crs.trans.CoordinateOperation;
import org.geolatte.geom.crs.trans.WithEpsgGOperationMethod;

import static java.lang.Math.*;

/**
 * Created by Karel Maesen, Geovise BVBA on 05/06/2018.
 *
 * An implementation of the Lambert Conic Conformal (2SP) map projection. This uses the formulas as documented
 * in the EPSG document (373-07-02) for coordinate operation method 9802.
 *
 */
public class LambertConformalConic2SP implements CoordinateOperation, WithEpsgGOperationMethod {

    private final Geographic2DCoordinateReferenceSystem baseCrs;
    private final double latOfFO;
    private final double lonOfFO;
    private final double lat1SP;
    private final double lat2SP;
    private final double eastingAtFO;
    private final double northingAtFO;
    private final double ecc; // eccentricity
    private final double a ; // semi-major axis

    //general parameters
    double m1;
    double m2;
    double t1;
    double t2;
    double tF;
    double n;
    double F;
    double rF;


    public LambertConformalConic2SP(Geographic2DCoordinateReferenceSystem baseCrs,
                                    double latitudeOfFO,
                                    double longitudeOfFO,
                                    double lat1SPDeg,
                                    double lat2SPDeg,
                                    double eastingAtFO,
                                    double northingAtFO) {

        this.baseCrs = baseCrs;
        this.latOfFO = toRadians(latitudeOfFO);
        this.lonOfFO = toRadians(longitudeOfFO);
        this.lat1SP = toRadians(lat1SPDeg);
        this.lat2SP = toRadians(lat2SPDeg);
        this.eastingAtFO = eastingAtFO;
        this.northingAtFO = northingAtFO;
        Ellipsoid ellipsoid = baseCrs.getDatum().getEllipsoid();
        double invFlat = ellipsoid.getInverseFlattening();
        double flattening = 1/invFlat;
        this.ecc = sqrt(2 * flattening - pow(flattening, 2));
        this.a =  ellipsoid.getSemiMajorAxis();

        m1 = cos(this.lat1SP) / sqrt( 1 - pow(ecc,2)*pow(sin(this.lat1SP),2));
        m2 = cos(this.lat2SP) / sqrt( 1 - pow(ecc,2)*pow(sin(this.lat2SP),2));

        t1 = t(this.lat1SP);
        t2 = t(this.lat2SP);
        tF = t(latOfFO);
        n = (log(m1) - log(m2)) / (log(t1) - log(t2));
        F = m1/(n * pow(t1, n));
        rF = a * F * pow(tF, n);
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


    private double t(double phi) {
        return tan( PI/4 - phi/2) / pow(( (1 - ecc*sin(phi))/(1+ecc*sin(phi)) ), ecc/2);
    }

    @Override
    public void forward(double[] inCoordinate, double[] outCoordinate) {
        double phi = toRadians(inCoordinate[1]);
        double lambda = toRadians(inCoordinate[0]);
        double tPhi = t(phi);
        double r = a * F * pow(tPhi, n);
        double theta = n * (lambda - lonOfFO);
        outCoordinate[0] = eastingAtFO + r * sin(theta);
        outCoordinate[1] = northingAtFO + rF - r * cos(theta);
    }

    @Override
    public void reverse(double[] inCoordinate, double[] outCoordinate) {
        double E = inCoordinate[0];
        double N = inCoordinate[1];
        double rNN = this.rF - (N - northingAtFO);
        double rPrime = signum(n) * sqrt(pow(E - eastingAtFO, 2) + pow(rNN, 2));
        double tPrime = pow( rPrime / (a*F), 1/n);
        double thetaPrime = atan( (E - eastingAtFO) / rNN);

        double phi = PI / 2 - 2 * atan(tPrime);
        double phi0;
        int iters = 0;
        do {
            iters++;
            phi0 = phi;
            phi = PI / 2 - 2 * atan(tPrime * pow( (1-ecc*sin(phi0))/(1 + ecc*sin(phi0)), ecc/2));
        } while ( (abs(phi - phi0) > 0.001) && iters < 5);
        double lambda = thetaPrime/n + lonOfFO;

        outCoordinate[0] = toDegrees(lambda);
        outCoordinate[1] = toDegrees(phi);

    }

    @Override
    public String getMethodId() {
        return "9802";
    }
}
