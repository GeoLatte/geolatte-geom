package org.geolatte.geom.crs.trans;

import org.geolatte.geom.crs.GeographicCoordinateReferenceSystem;

import static java.lang.Math.*;


/**
 * The Geographic to Geocentric Coordinate conversion.
 *
 * Created by Karel Maesen, Geovise BVBA on 20/07/17.
 */
public class GeographicToGeocentricConversion implements CoordinateOperation, WithEpsgGOperationMethod {

    final double eccentricity2;
    final double semiMajorAxis;
    final double primeMerid;

    //for the reverse calculation
    final double epsilon;
    final double semiMinorAxis; //b



    public GeographicToGeocentricConversion(GeographicCoordinateReferenceSystem sourceCRS) {

        primeMerid = sourceCRS.getPrimeMeridian().getLongitude();
        semiMajorAxis = sourceCRS.getDatum().getEllipsoid().getSemiMajorAxis();
        double invFlatt = sourceCRS.getDatum().getEllipsoid().getInverseFlattening();
        double flatt = 1 / invFlatt;
        eccentricity2 = (2  - flatt) * flatt;
        epsilon = eccentricity2 / (1 - eccentricity2);
        semiMinorAxis = semiMajorAxis * (1 - flatt);
    }

    @Override
    public boolean isReversible() {
        return false;
    }

    @Override
    public int inCoordinateDimension() {
        return 3;
    }

    @Override
    public int outCoordinateDimension() {
        return 3;
    }

    @Override
    public void forward(double[] inCoordinate, double[] outCoordinate) {
        double phi = toRadians(inCoordinate[1]);
        double lon = toRadians(inCoordinate[0] - primeMerid);
        double h = inCoordinate[2]; // note this is here assumed to be ellipsoid height (see the note in G7-2, sec. 2.2.1, pag 95)

        double primeVerticalRadius = semiMajorAxis / sqrt(1 - eccentricity2 *pow(sin(phi), 2));

        double vhc = (primeVerticalRadius + h)* cos(phi);
        outCoordinate[0] =  vhc  * cos(lon);
        outCoordinate[1] = vhc  * sin(lon);
        outCoordinate[2] = ((1 - eccentricity2) * primeVerticalRadius + h) * sin(phi);

    }

    @Override
    public void reverse(double[] inCoordinate, double[] outCoordinate) {
        double x = inCoordinate[0];
        double y = inCoordinate[1];
        double z = inCoordinate[2];
        double p = hypot(x, y);
        double q = atan( z * semiMajorAxis / (p * semiMinorAxis) );
        double phi = atan((z + epsilon * semiMinorAxis * pow(sin(q), 3)) /(p - eccentricity2*semiMajorAxis*pow(cos(q),3)) );
        double lambda = atan(y / x);
        double primeVerticalRadius = semiMajorAxis / sqrt(1 - eccentricity2 *pow(sin(phi), 2));
        double h = (p / cos(phi) - primeVerticalRadius);
        outCoordinate[0] = toDegrees(lambda);
        outCoordinate[1] = toDegrees(phi);
        outCoordinate[2] = h;
    }

    @Override
    public String getMethodId() {
        return "9602";
    }
}
