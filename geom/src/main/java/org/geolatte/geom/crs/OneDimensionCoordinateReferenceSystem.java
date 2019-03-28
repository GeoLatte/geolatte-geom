package org.geolatte.geom.crs;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-28.
 */
public interface OneDimensionCoordinateReferenceSystem {

    CoordinateSystemAxis getStraightLineAxis();

    default Unit getUnit() {
        return getStraightLineAxis().getUnit();
    }
}
