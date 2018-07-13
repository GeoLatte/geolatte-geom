import org.geolatte.geom.codec.db.oracle.Settings;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Karel Maesen, Geovise BVBA on 04/12/16.
 */
public class TestSettings {


    @Test
    public void testFeaturesSdoPointSettoTrue(){
        System.setProperty(Settings.USE_SDO_POINT, "true");
        assertTrue(Settings.useSdoPointType());
        System.setProperty(Settings.USE_SDO_POINT, "1");
        assertTrue(Settings.useSdoPointType());
        //reset to default
        System.setProperty(Settings.USE_SDO_POINT, "false");
    }

    @Test
    public void testFeaturesSdoPointSettoFalse(){
        System.setProperty(Settings.USE_SDO_POINT, "false");
        assertFalse(Settings.useSdoPointType());
        System.setProperty(Settings.USE_SDO_POINT, "0");
        assertFalse(Settings.useSdoPointType());
    }


}
