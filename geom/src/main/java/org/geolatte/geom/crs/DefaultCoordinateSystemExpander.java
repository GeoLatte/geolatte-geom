package org.geolatte.geom.crs;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addVerticalSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 14/02/2020.
 */
@Deprecated
public class DefaultCoordinateSystemExpander implements CoordinateSystemExpander {

    @Override
    public CoordinateReferenceSystem<?> expand(CoordinateReferenceSystem<?> base, CoordinateSystemAxisDirection direction) {
        CoordinateReferenceSystem<?> result = base;
        switch (direction.getDefaultNormalOrder()) {
            case 2:
                result = result.hasZ() ? result : addVerticalSystem(result, Unit.METER);
                break;
            case 3:
                result = result.hasM() ? result : addLinearSystem(result, Unit.METER);
        }
        return result;
    }
}

