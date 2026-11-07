package org.geolatte.geom;

import org.junit.Test;

import static org.geolatte.geom.CrsMock.crs;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;

/**
 * Checks that the topological dimension follows ISO/IEC 13249-3 (SQL/MM Spatial), where the
 * dimension of the empty set is -1.
 */
public class EmptyGeometryDimensionTest {

    @Test
    public void emptyPrimitivesHaveDimensionMinusOne() {
        assertEquals(-1, point(crs).getDimension());
        assertEquals(-1, new LineString<>(crs).getDimension());
        assertEquals(-1, new LinearRing<>(crs).getDimension());
        assertEquals(-1, new Polygon<>(crs).getDimension());
    }

    @Test
    public void emptyCollectionsHaveDimensionMinusOne() {
        assertEquals(-1, multipoint(crs).getDimension());
        assertEquals(-1, new MultiLineString<>(crs).getDimension());
        assertEquals(-1, new MultiPolygon<>(crs).getDimension());
        assertEquals(-1, new GeometryCollection<>(crs).getDimension());
    }

    @Test
    public void collectionOfEmptyMembersHasDimensionMinusOne() {
        GeometryCollection<C2D> gc = geometrycollection(point(crs), new LineString<>(crs));
        assertEquals(-1, gc.getDimension());
    }

    @Test
    public void nonEmptyMembersDetermineCollectionDimension() {
        Polygon<C2D> pgn = polygon(crs, ring(c(0, 0), c(1, 0), c(1, 1), c(0, 0)));
        GeometryCollection<C2D> gc = geometrycollection(point(crs), pgn);
        assertEquals(2, gc.getDimension());

        GeometryCollection<C2D> gc2 = geometrycollection(point(crs, c(1, 1)), new LineString<>(crs));
        assertEquals(0, gc2.getDimension());
    }

    @Test
    public void nonEmptyPrimitivesKeepTheirDimension() {
        assertEquals(0, point(crs, c(1, 1)).getDimension());
        assertEquals(1, linestring(crs, c(0, 0), c(1, 1)).getDimension());
        assertEquals(2, polygon(crs, ring(c(0, 0), c(1, 0), c(1, 1), c(0, 0))).getDimension());
    }
}
