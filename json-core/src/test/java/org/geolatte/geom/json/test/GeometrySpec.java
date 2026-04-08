package org.geolatte.geom.json.test;

import org.geolatte.geom.Geometry;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.builder.DSL.multilinestring;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72ZM;
import static org.geolatte.geom.json.GeoJsonStrings.multiLineString;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithCrs34D;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON deserialization to the generic {@link Geometry}
 * interface (i.e. when the caller asks for {@code Geometry.class} and the
 * concrete subtype is determined by the JSON {@code "type"} member).
 */
public abstract class GeometrySpec extends AbstractGeoJsonContract {

    @Test
    public void deserializeMultiLineStringAsGeometry() {
        Geometry<?> rec = newMapper().readValue(multiLineString, Geometry.class);
        Geometry<?> expected = multilinestring(
                linestring(WGS84, g(1, 1), g(1, 2)),
                linestring(WGS84, g(3, 4), g(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void deserializePointAsGeometry() {
        Geometry<?> rec = newMapper().readValue(pointTextWithCrs34D, Geometry.class);
        Geometry<?> expected = point(lambert72ZM, c(1, 2, 3, 4));
        assertEquals(expected, rec);
    }
}
