package org.geolatte.geom.json.test;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.pointText3D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Cross-cutting invariants that don't fit a single geometry type:
 * <ul>
 *   <li>Round-trip preserves the coordinate dimension.</li>
 *   <li>An extended CRS is created once and reused for subsequent
 *       deserializations from the same mapper.</li>
 * </ul>
 */
public abstract class InvariantSpec extends AbstractGeoJsonContract {

    /**
     * Verifies that an extended CRS is created once and reused for subsequent
     * deserializations from the same mapper, by checking instance identity.
     */
    @Test
    public void crsDynamicRegistration() {
        MapperLike mapper = newMapper();
        Point<?> pnt1 = mapper.readValue(pointText3D, Point.class);
        Point<?> pnt2 = mapper.readValue(pointText3D, Point.class);
        assertSame(pnt1.getCoordinateReferenceSystem(), pnt2.getCoordinateReferenceSystem());
    }

    @Test
    public void coordinateDimensionInvariant() {
        Point<G2D> geom = point(WGS84, g(1, 2));
        MapperLike mapper = newMapper();
        assertEquals(geom.getCoordinateDimension(),
                mapper.readValue(mapper.writeAsString(geom), Geometry.class).getCoordinateDimension());
    }
}
