package org.geolatte.geom;

/**
 * Utility class to represent geographic coordinates in DD.
 *
 *
 *
 * Created by Karel Maesen, Geovise BVBA on 21/07/17.
 */
public class DecimalDegree {

    private static final double ARCSECOND_IN_RADIANS = 4.8481368111E-6;

    public static String DMS(double lon, double lat) {
        return lonDMS(lon) + " " +  latDMS(lat);
    }

    private static String lonDMS(double lon) {
        return lon > 0 ? toDMS(lon) + "E" : toDMS(lon) + "W";
     }


    private static String latDMS(double lat) {
        return lat > 0 ? toDMS(lat) + "N" : toDMS(lat) + "S";
    }

    private static String toDMS(double c) {
        double deg = Math.floor(c);
        double mindd = (c - deg) * 60 ;
        double min = Math.floor(mindd);
        double sec = Math.round( 6000 * (mindd - min)) / 100; //we multiply by 100 and then divide to get correct rounding tot two decimals

        return String.format("%1$d %2$d' %3$.2f''", (int)deg, (int)min, sec);
    }


    public static double secondsToRadians(double v) {
        return v * ARCSECOND_IN_RADIANS;
    }
}

