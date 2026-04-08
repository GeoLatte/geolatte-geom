package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;

public class InvariantTest extends GeoJsonTest{



    public void testCoordinateDimension() throws JsonProcessingException {
        Point<G2D> geom = point(WGS84, g(1, 2));
        assertEquals(geom.getCoordinateDimension(),
                mapper.readValue(mapper.writeValueAsString(geom), Geometry.class).getCoordinateDimension());

    }


}
