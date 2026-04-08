package org.geolatte.geom.json;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LinearUnit;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.*;

/**
 * Test fixtures for the Coordinate Reference Systems used by the GeoJSON test suite.
 *
 * <p>Lives in {@code geolatte-geojson-core}'s test sources so it can be shared between
 * the Jackson 2 and Jackson 3 adapter modules via the test-jar.</p>
 *
 * <p>Created by Karel Maesen, Geovise BVBA on 09/09/17.</p>
 */
public class Crss {

    public static final CoordinateReferenceSystem<G3D> wgs3D = addVerticalSystem(WGS84, G3D.class, LinearUnit.METER);
    public static final CoordinateReferenceSystem<G2DM> wgs2DM = addLinearSystem(WGS84, G2DM.class, LinearUnit.METER);
    public static final CoordinateReferenceSystem<G3DM> wgs3DM = addLinearSystem(wgs3D, G3DM.class, LinearUnit.METER);
    public static final CoordinateReferenceSystem<C2D> lambert72 = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
    public static final CoordinateReferenceSystem<C3D> lambert72Z = addVerticalSystem(lambert72, C3D.class, LinearUnit.METER);
    public static final CoordinateReferenceSystem<C3DM> lambert72ZM = addLinearSystem(lambert72Z, C3DM.class, LinearUnit.METER);
}
