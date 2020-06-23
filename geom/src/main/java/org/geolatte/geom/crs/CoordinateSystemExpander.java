package org.geolatte.geom.crs;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addVerticalSystem;
import static org.geolatte.geom.crs.CoordinateSystemAxisDirection.OTHER;
import static org.geolatte.geom.crs.CoordinateSystemAxisDirection.UP;

/**
 * Created by Karel Maesen, Geovise BVBA on 14/02/2020.
 */
public interface CoordinateSystemExpander {

    CoordinateReferenceSystem<?> expand(CoordinateReferenceSystem<?> base, CoordinateSystemAxisDirection direction);

    default CoordinateReferenceSystem<?> expandM(CoordinateReferenceSystem<?> base) {
        return expand(base, OTHER);
    }

    default CoordinateReferenceSystem<?> expandZ(CoordinateReferenceSystem<?> base) {
        return expand(base, UP);
    }

}

