package org.geolatte.geom.crs.trans.projections;

import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.CrsParameter;
import org.geolatte.geom.crs.ProjectedCoordinateReferenceSystem;
import org.geolatte.geom.crs.Projection;
import org.geolatte.geom.crs.trans.CoordinateOperation;
import org.geolatte.geom.crs.trans.CoordinateOperations;
import org.geolatte.geom.crs.trans.UnsupportedTransformException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-24.
 */
public class Projections {

//    final private static Map<CrsId, Class<? extends CoordinateOperation>> METHODS = new HashMap<>();

//    static {
//        //TODO do this using reflection
//        METHODS.put(CrsId.valueOf(1024), PseudoMercator.class);
//        METHODS.put(CrsId.valueOf(9802), PseudoMercator.class);
//    }

    public static CoordinateOperation buildFrom(ProjectedCoordinateReferenceSystem projected) {
        Projection projection  = projected.getProjection();
        List<CrsParameter> params = projected.getParameters();

        if (projection.getName().equalsIgnoreCase("Lambert_Conformal_Conic_2SP") ||
                projection.getCrsId().getCode() == 9802) {
            return LambertConformalConic2SP.fromCrsParameters(projected.getGeographicCoordinateSystem(), params);
        }

        if (projection.getName().equalsIgnoreCase("Mercator_1SP") ||
                projection.getCrsId().getCode() == 1024) {
            return PseudoMercator.fromCrsParameters(projected.getGeographicCoordinateSystem(), params);
        }

        //TODO -- Log a warning in this case
        return CoordinateOperations.identity(2);

    }
}
