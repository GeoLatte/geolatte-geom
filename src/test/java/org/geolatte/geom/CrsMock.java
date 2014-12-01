package org.geolatte.geom;

import org.geolatte.geom.crs.*;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addVerticalSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 30/11/14.
 */
public class CrsMock {

    public static  Geographic2DCoordinateReferenceSystem WGS84 = CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);
    public static  CoordinateReferenceSystem<G3D> WGS84_Z = addVerticalSystem(WGS84, G3D.class, Unit.METER);
    public static  CoordinateReferenceSystem<G2DM> WGS84_M = addLinearSystem(WGS84, G2DM.class, Unit.METER);
    public static  CoordinateReferenceSystem<G3DM> WGS84_ZM =addLinearSystem(WGS84_Z, G3DM.class, Unit.METER);

    public static CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;
    public static CoordinateReferenceSystem<C3D> crsZ = addVerticalSystem(crs, C3D.class, Unit.METER);

    public static CoordinateReferenceSystem<C2DM> crsM = addLinearSystem(crs, C2DM.class, Unit.METER);
    public static CoordinateReferenceSystem<C3DM> crsZM = addLinearSystem(crsZ, C3DM.class, Unit.METER);


}
