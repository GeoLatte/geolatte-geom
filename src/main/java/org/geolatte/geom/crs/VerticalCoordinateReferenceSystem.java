package org.geolatte.geom.crs;

import org.geolatte.geom.Position;
import org.geolatte.geom.V;

/**
 *
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
public class VerticalCoordinateReferenceSystem extends SingleCoordinateReferenceSystem<V> {

    private final VerticalDatum datum;

    /**
     * Constructs an instance.
     *
     * @param crsId the authority and authority c
     * @param name
     */
    public VerticalCoordinateReferenceSystem(CrsId crsId, String name, VerticalDatum datum, VerticalStraightLineAxis axis) {
        super(crsId, name, new OneDimensionCoordinateSystem<V>(axis, V.class));
        this.datum = datum;
    }

    public VerticalDatum getDatum() {
        return datum;
    }

    public VerticalStraightLineAxis getVerticalAxis() {
        return (VerticalStraightLineAxis)getCoordinateSystem().getAxis(0);
    }

    public LinearUnit getUnit() {
        return getVerticalAxis().getUnit();
    }

}
