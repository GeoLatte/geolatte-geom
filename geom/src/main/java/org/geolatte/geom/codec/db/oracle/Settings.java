package org.geolatte.geom.codec.db.oracle;

/**
 * Created by Karel Maesen, Geovise BVBA on 04/12/16.
 */
public class Settings {

    final public static String USE_SDO_POINT = "GEOLATTE_USE_SDO_POINT_TYPE";



    public static boolean useSdoPointType() {
        String use = System.getProperty(USE_SDO_POINT);
        return determineValue(use);
    }

    private static boolean determineValue(String val){
        if (val == null) return false;
        if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("1") ) {
            return true;
        }
        return false;
    }
}
