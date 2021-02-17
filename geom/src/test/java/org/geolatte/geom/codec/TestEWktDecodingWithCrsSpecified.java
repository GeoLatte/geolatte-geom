package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.*;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addVerticalSystem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Karel Maesen, Geovise BVBA on 17/02/15.w
 */
public class TestEWktDecodingWithCrsSpecified {

    ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
    CompoundCoordinateReferenceSystem<C3DM> crs3dm  = addLinearSystem(
            addVerticalSystem(crs, C3D.class, LinearUnit.METER ), C3DM.class, LinearUnit.METER
    );

    CompoundCoordinateReferenceSystem<?> crsUnknown  = addLinearSystem(
            addVerticalSystem(CrsRegistry.getCoordinateReferenceSystemForEPSG(4326, crs), LinearUnit.METER ), LinearUnit.METER
    );

    Geometry<C3DM> original = point(crs3dm, c(688789, 169038, 24, 10));
    String originalWkt = "SRID=31370;POINT(688789 169038 24 10)";

    @Test
    public void testWktDecodingDeterminesCRSCorrectly() {
        Geometry<?> geometry = decode(originalWkt, null);
        assertEquals(geometry.getCoordinateReferenceSystem(), crs3dm);
        assertEquals(original, geometry);
    }

    public void testWktDecodingFailsWhenIncorrectCRSSpecified() {
           Geometry<?> geom = decode(originalWkt, crs);
        assertTrue(geom.getCoordinateReferenceSystem().hasM());
        assertTrue(geom.getCoordinateReferenceSystem().hasZ());
    }

    @Test
    public void testWktDecodingWithCorrectCRSSpecified() {
        Geometry<C3DM> geometry = decode(originalWkt, crs3dm);
        assertEquals(original, geometry);
    }

    @Test
    public void testWktTestWithUntypedCRS(){
        String wkt = "POINT(688789 169038 24 10)";

        Geometry<?> geom = decode(wkt, crsUnknown);
        assertEquals(4,geom.getCoordinateDimension());
        assertEquals(G3DM.class, geom.getCoordinateReferenceSystem().getPositionClass());

    }

    private <P extends Position> Geometry<P> decode(String wkt, CoordinateReferenceSystem<P> crs) {
        return Wkt.newDecoder(Wkt.Dialect.POSTGIS_EWKT_1).decode(wkt, crs);
    }

}
