package org.geolatte.geom.crs;

import static org.geolatte.geom.crs.Unit.METER;
import static org.geolatte.geom.crs.Unit.UNKNOWN_LINEAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.geolatte.geom.G3D;
import org.junit.Test;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/2018.
 */
public class TestCrsExtendedId {

    @Test
    public void testExtensions(){
        CrsId cM = CrsId.valueOf(200).extend(null, METER);
        CrsId cM2 = CrsId.valueOf(200).extend(null, METER);
        assertEquals(cM, cM2);
        assertTrue(!cM.equals(CrsId.valueOf(200)));
    }

    @Test
    public void testDifferentExtensionsSameBase(){
        CrsId cM = CrsId.valueOf(200).extend(null, METER);
        CrsId cV = CrsId.valueOf(200).extend(METER,null);
        assertTrue(!cM.equals(cV));
    }

    @Test
    public void testExtensionsWithDifferentUnits(){
        CrsId cM = CrsId.valueOf(200).extend(null, METER);
        CrsId cM2 = CrsId.valueOf(200).extend(null, UNKNOWN_LINEAR);
        assertTrue(!cM.equals(cM2));
    }


    @Test
    public void testIfNullOnbothParamsEqualsBase(){
        CrsId base = CrsId.valueOf(200);
        CrsId extended = base.extend(null, null);

        assertEquals(base, extended);
    }

    @Test
    public void testAsKeysInCrsRegistry() {
        Geographic2DCoordinateReferenceSystem crs = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);
        CompoundCoordinateReferenceSystem<G3D> extended = CoordinateReferenceSystems.addLinearSystem(crs, G3D.class, METER);

        CrsId newCrsId = crs.getCrsId().extend(null, METER);
        CrsRegistry.registerCoordinateReferenceSystem(newCrsId, extended);

        CoordinateReferenceSystem<?> retrieved = CrsRegistry.getCoordinateReferenceSystem(newCrsId, null);
        assertEquals(extended, retrieved);

    }
}
