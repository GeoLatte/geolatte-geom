package org.geolatte.geom;


import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

/**
 * Utility class to represent geographic coordinates in DD.
 *
 *
 *
 * Created by Karel Maesen, Geovise BVBA on 21/07/17.
 */
public class DecimalDegree {

    private static final Pattern DMS_REGEX=
            Pattern.compile("(\\d+)˚??\\s*(\\d+)'\\s*(\\d*.??\\d*)''\\s*([EW])\\s+(\\d+)˚??\\s*(\\d+)'\\s*(\\d*.??\\d*)''\\s*([NS])");

    private static final double ARCSECOND_IN_RADIANS = 4.8481368111E-6;

    public static String DMS(double lon, double lat) {
        return lonDMS(lon) + " " +  latDMS(lat);
    }

    public static double secondsToRadians(double v) {
        return v * ARCSECOND_IN_RADIANS;
    }

    public static G2D parseDMS(String dms){
        Matcher matcher = DMS_REGEX.matcher(dms);
        if (!matcher.matches()) {
            //todo introduce proper exception class
            throw new RuntimeException(format("%s is Not a valid DMS String", dms));
        }

        int lonDeg = parseInt(matcher.group(1));
        int lonMin = parseInt(matcher.group(2));
        double lonSecs = parseDouble(matcher.group(3));
        String lonNWSE = matcher.group(4);

        int latDeg = parseInt(matcher.group(5));
        int latMin = parseInt(matcher.group(6));
        double latSecs = parseDouble(matcher.group(7));
        String latNWSE = matcher.group(8);

        return new G2D(fromDMS(lonDeg, lonMin, lonSecs, lonNWSE), fromDMS(latDeg, latMin, latSecs, latNWSE));
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

        return format("%1$d˚ %2$d' %3$.2f''", (int)deg, (int)min, sec);
    }

    private static double fromDMS(int deg, int min, double sec, String NWSE) {
        int sgn = "E".equalsIgnoreCase(NWSE) || "N".equalsIgnoreCase(NWSE) ? +1 : -1;
        return sgn * deg + min/60.0 + sec/(60.0*60.0);
    }
}

