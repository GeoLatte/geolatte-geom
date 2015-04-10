package org.geolatte.geom.crs;

/**
 * An axis for a {@code LinearCoordinateReferenceSystem}
 * <p/>
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
public class MeasureStraightLineAxis extends StraightLineAxis {

    /**
     * Creates an instance.
     *
     * @param axisName the name for this axis
     * @param unit     the unit of this axis
     */
    public MeasureStraightLineAxis(String axisName, LinearUnit unit) {
        super(axisName, CoordinateSystemAxisDirection.OTHER, unit);
    }

}
