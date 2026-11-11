package org.geolatte.geom;

import org.geolatte.geom.codec.Wkb;
import org.geolatte.geom.codec.Wkt;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.crs;
import static org.geolatte.geom.CrsMock.crsZM;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.*;

/**
 * A geometry collection whose elements are all empty corresponds to the empty set (ISO/IEC 13249-3, ST_IsEmpty),
 * but its elements are still part of its representation.
 */
public class CollectionOfEmptyMembersTest {

    private final GeometryCollection<C3DM> gcOfEmptyPoint = new GeometryCollection<>(new Point<>(crsZM));
    private final MultiPolygon<C3DM> mpOfEmptyPolygon = new MultiPolygon<>(new Polygon<>(crsZM));
    private final MultiPoint<C2D> mpOfEmptyPoints = multipoint(point(crs), point(crs));

    @Test
    public void collectionOfEmptyMembersIsEmpty() {
        assertTrue(gcOfEmptyPoint.isEmpty());
        assertTrue(mpOfEmptyPolygon.isEmpty());
        assertTrue(mpOfEmptyPoints.isEmpty());
        assertEquals(-1, gcOfEmptyPoint.getDimension());
        assertEquals(-1, mpOfEmptyPolygon.getDimension());
        assertEquals(-1, mpOfEmptyPoints.getDimension());
    }

    @Test
    public void collectionOfEmptyMembersKeepsItsMembers() {
        assertEquals(1, gcOfEmptyPoint.getNumGeometries());
        assertEquals(1, mpOfEmptyPolygon.getNumGeometries());
        assertEquals(2, mpOfEmptyPoints.getNumGeometries());
        assertEquals(0, gcOfEmptyPoint.getNumPositions());
    }

    @Test
    public void collectionOfEmptyMembersHasCoordinateDimensionOfItsCrs() {
        assertEquals(4, gcOfEmptyPoint.getCoordinateDimension());
        assertEquals(4, mpOfEmptyPolygon.getCoordinateDimension());
        assertEquals(2, mpOfEmptyPoints.getCoordinateDimension());
        assertEquals(C3DM.class, gcOfEmptyPoint.getPositionClass());
    }

    @Test
    public void collectionOfEmptyMembersEqualsEmptyCollection() {
        assertEquals(new GeometryCollection<>(crsZM), gcOfEmptyPoint);
        assertEquals(new MultiPoint<>(crs), mpOfEmptyPoints);
    }

    @Test
    public void wktWritesTheEmptyMembers() {
        assertEquals("GEOMETRYCOLLECTION ZM (POINT ZM EMPTY)", Wkt.toWkt(gcOfEmptyPoint, Wkt.Dialect.SFA_1_2_1));
        assertEquals("MULTIPOLYGON ZM (EMPTY)", Wkt.toWkt(mpOfEmptyPolygon, Wkt.Dialect.SFA_1_2_1));
        assertEquals("MULTIPOINT(EMPTY,EMPTY)", Wkt.toWkt(mpOfEmptyPoints));
        assertEquals("GEOMETRYCOLLECTION(POINT EMPTY)", Wkt.toWkt(new GeometryCollection<>(new Point<>(crs))));
    }

    @Test
    public void wktOfEmptyGeometriesIsUnchanged() {
        assertEquals("POINT ZM EMPTY", Wkt.toWkt(new Point<>(crsZM), Wkt.Dialect.SFA_1_2_1));
        assertEquals("POINT EMPTY", Wkt.toWkt(new Point<>(crs), Wkt.Dialect.SFA_1_2_1));
        assertEquals("GEOMETRYCOLLECTION ZM EMPTY", Wkt.toWkt(new GeometryCollection<>(crsZM), Wkt.Dialect.SFA_1_2_1));
        assertEquals("POINT ZM (1 2 3 4)", Wkt.toWkt(point(crsZM, c(1, 2, 3, 4)), Wkt.Dialect.SFA_1_2_1));
        assertEquals("POINT EMPTY", Wkt.toWkt(new Point<>(crs)));
    }

    @Test
    public void wktRoundTripKeepsTheEmptyMembers() {
        for (Wkt.Dialect dialect : new Wkt.Dialect[]{Wkt.Dialect.SFA_1_2_1, Wkt.Dialect.POSTGIS_EWKT_1}) {
            Geometry<?> decoded = Wkt.fromWkt(Wkt.toWkt(gcOfEmptyPoint, dialect), crsZM, dialect);
            assertTrue(decoded instanceof GeometryCollection);
            assertEquals(1, ((GeometryCollection<?>) decoded).getNumGeometries());
            assertTrue(decoded.isEmpty());
        }
    }

    @Test
    public void wkbRoundTripKeepsTheEmptyMembers() {
        for (Wkb.Dialect dialect : new Wkb.Dialect[]{Wkb.Dialect.SFA_1_2_1, Wkb.Dialect.POSTGIS_EWKB_1, Wkb.Dialect.POSTGIS_EWKB_2}) {
            ByteBuffer buffer = Wkb.newEncoder(dialect).encode(gcOfEmptyPoint);
            Geometry<?> decoded = Wkb.newDecoder(dialect).decode(buffer, crsZM);
            assertTrue(dialect.toString(), decoded instanceof GeometryCollection);
            assertEquals(dialect.toString(), 1, ((GeometryCollection<?>) decoded).getNumGeometries());
            assertTrue(dialect.toString(), decoded.isEmpty());
        }
        // Note: empty points inside a MultiPoint cannot be round-tripped through WKB, since the MultiPoint
        // parser reads its elements as bare positions. That is a pre-existing limitation.
    }
}
