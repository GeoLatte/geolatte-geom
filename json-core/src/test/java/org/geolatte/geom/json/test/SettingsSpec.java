package org.geolatte.geom.json.test;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.pointText3DM;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithUrnCrs;
import static org.geolatte.geom.json.Setting.FORCE_DEFAULT_CRS_DIMENSION;
import static org.geolatte.geom.json.Setting.IGNORE_CRS;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the {@code IGNORE_CRS} and {@code FORCE_DEFAULT_CRS_DIMENSION}
 * settings during deserialization.
 */
public abstract class SettingsSpec extends AbstractGeoJsonContract {

    @Test
    public void ignoreCrsDeserializesAgainstDefault() {
        Point<?> pnt = point(WGS84, g(1, 2));
        assertEquals(pnt,
                newMapper(IGNORE_CRS, true).readValue(pointTextWithUrnCrs, Geometry.class));
    }

    @Test
    public void forceCrsToDefaultDeserializesAgainstDefault() {
        Point<?> pnt = point(WGS84, g(1, 2));
        assertEquals(pnt,
                newMapper(FORCE_DEFAULT_CRS_DIMENSION, true).readValue(pointTextWithUrnCrs, Geometry.class));
    }

    @Test
    public void forceCrsToDefault3DM() {
        Point<G2D> pnt = point(WGS84, g(1, 2));
        assertEquals(pnt,
                newMapper(FORCE_DEFAULT_CRS_DIMENSION, true).readValue(pointText3DM, Geometry.class));
    }
}
