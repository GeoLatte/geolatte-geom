package org.geolatte.geom.codec;

import org.geolatte.geom.C3D;
import org.geolatte.geom.C3DM;
import org.geolatte.geom.G3DM;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CompoundCoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LinearUnit;
import org.geolatte.geom.crs.ProjectedCoordinateReferenceSystem;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addVerticalSystem;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 17/02/15.
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

        Geometry<?> geometry = Wkt.fromWkt(originalWkt);

        assertEquals(geometry.getCoordinateReferenceSystem(), crs3dm);
        assertEquals(original, geometry);
    }

    @Test(expected = WktDecodeException.class)
    public void testWktDecodingFailsWhenIncorrectCRSSpecified() {
           Wkt.fromWkt(originalWkt, crs);
    }

    @Test
    public void testWktDecodingWithCorrectCRSSpecified() {
        Geometry<C3DM> geometry = Wkt.fromWkt(originalWkt, crs3dm);
        assertEquals(original, geometry);
    }

    @Test
    public void testWktTestWithUntypedCRS(){
        String wkt = "POINT(688789 169038 24 10)";

        Geometry<?> geom = Wkt.fromWkt(wkt, crsUnknown);
        assertEquals(4,geom.getCoordinateDimension());
        assertEquals(G3DM.class, geom.getCoordinateReferenceSystem().getPositionClass());

    }


}
