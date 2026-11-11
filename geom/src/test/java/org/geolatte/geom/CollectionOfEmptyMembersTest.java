package org.geolatte.geom;

import org.geolatte.geom.codec.Wkb;
import org.geolatte.geom.codec.WkbDecodeException;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.WktDecodeException;
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
    }

    private final MultiPoint<C2D> mpWithEmptyPoint = multipoint(point(crs, c(1, 2)), point(crs));
    private final MultiLineString<C2D> mlWithEmptyLine = multilinestring(new LineString<>(crs), linestring(crs, c(0, 0), c(1, 1)));
    private final MultiPolygon<C2D> mpolyWithEmptyPolygon = multipolygon(polygon(crs, ring(c(0, 0), c(1, 0), c(1, 1), c(0, 0))), new Polygon<>(crs));
    private final AbstractGeometryCollection<C2D, ?>[] collectionsWithEmptyMembers =
            new AbstractGeometryCollection[]{mpWithEmptyPoint, mlWithEmptyLine, mpolyWithEmptyPolygon, mpOfEmptyPoints};

    @Test
    public void wkbRoundTripKeepsEmptyMembersOfHomogeneousCollections() {
        // MySQL has no representation for empty geometries, so it is left out
        for (Wkb.Dialect dialect : new Wkb.Dialect[]{Wkb.Dialect.SFA_1_1_0, Wkb.Dialect.SFA_1_2_1,
                Wkb.Dialect.POSTGIS_EWKB_1, Wkb.Dialect.POSTGIS_EWKB_2, Wkb.Dialect.HANA_EWKB}) {
            for (AbstractGeometryCollection<C2D, ?> geom : collectionsWithEmptyMembers) {
                ByteBuffer buffer = Wkb.newEncoder(dialect).encode(geom);
                Geometry<?> decoded = Wkb.newDecoder(dialect).decode(buffer, crs);
                assertSameMembers(dialect + " " + Wkt.toWkt(geom), geom, decoded);
            }
        }
    }

    @Test
    public void wkbDecodesMultiPointWithEmptyPoint() {
        // SFA 1.2.1: an empty point in a MultiPoint is encoded as a point with NaN coordinates
        String hex = "0104000000020000000101000000000000000000F03F00000000000000400101000000000000000000F87F000000000000F87F";
        Geometry<C2D> decoded = Wkb.newDecoder(Wkb.Dialect.SFA_1_2_1).decode(ByteBuffer.from(hex), crs);
        assertSameMembers("SFA_1_2_1", mpWithEmptyPoint, decoded);
        // PostGIS EWKB v1: an empty point is encoded as an empty geometry collection
        hex = "0104000000020000000101000000000000000000F03F0000000000000040010700000000000000";
        decoded = Wkb.newDecoder(Wkb.Dialect.POSTGIS_EWKB_1).decode(ByteBuffer.from(hex), crs);
        assertSameMembers("POSTGIS_EWKB_1", mpWithEmptyPoint, decoded);
    }

    @Test(expected = WkbDecodeException.class)
    public void wkbRejectsNonEmptyMemberOfWrongType() {
        // a MULTIPOINT whose single member is LINESTRING(1 2, 3 4)
        String hex = "010400000001000000" + "010200000002000000" + "000000000000F03F0000000000000040" + "00000000000008400000000000001040";
        Wkb.newDecoder(Wkb.Dialect.SFA_1_2_1).decode(ByteBuffer.from(hex), crs);
    }

    @Test
    public void wktParsesEmptyMembersOfHomogeneousCollections() {
        for (Wkt.Dialect dialect : new Wkt.Dialect[]{Wkt.Dialect.SFA_1_1_0, Wkt.Dialect.SFA_1_2_1, Wkt.Dialect.POSTGIS_EWKT_1}) {
            assertEquals(dialect.toString(), mpWithEmptyPoint, Wkt.fromWkt("MULTIPOINT((1 2),EMPTY)", crs, dialect));
            assertEquals(dialect.toString(), mpWithEmptyPoint, Wkt.fromWkt("MULTIPOINT(1 2, EMPTY)", crs, dialect));
            assertEquals(dialect.toString(), mpOfEmptyPoints, Wkt.fromWkt("MULTIPOINT(EMPTY, EMPTY)", crs, dialect));
            assertEquals(dialect.toString(), mlWithEmptyLine, Wkt.fromWkt("MULTILINESTRING(EMPTY,(0 0,1 1))", crs, dialect));
            assertEquals(dialect.toString(), mpolyWithEmptyPolygon,
                    Wkt.fromWkt("MULTIPOLYGON(((0 0,1 0,1 1,0 0)),EMPTY)", crs, dialect));
            // the members are kept, also when all are empty
            assertSameMembers(dialect.toString(), mpOfEmptyPoints, Wkt.fromWkt("MULTIPOINT(EMPTY, EMPTY)", crs, dialect));
        }
    }

    @Test
    public void wktRoundTripKeepsEmptyMembersOfHomogeneousCollections() {
        for (Wkt.Dialect dialect : new Wkt.Dialect[]{Wkt.Dialect.SFA_1_1_0, Wkt.Dialect.SFA_1_2_1, Wkt.Dialect.POSTGIS_EWKT_1}) {
            for (AbstractGeometryCollection<C2D, ?> geom : collectionsWithEmptyMembers) {
                String wkt = Wkt.toWkt(geom, dialect);
                assertSameMembers(dialect + " " + wkt, geom, Wkt.fromWkt(wkt, crs, dialect));
            }
        }
        // SFA 1.1.0 writes a MultiPoint as a list of bare positions; empty points are written as EMPTY
        assertEquals("MULTIPOINT(1 2,EMPTY)", Wkt.toWkt(mpWithEmptyPoint, Wkt.Dialect.SFA_1_1_0));
        assertEquals("MULTIPOINT(EMPTY,EMPTY)", Wkt.toWkt(mpOfEmptyPoints, Wkt.Dialect.SFA_1_1_0));
        assertEquals("MULTIPOINT EMPTY", Wkt.toWkt(new MultiPoint<>(crs), Wkt.Dialect.SFA_1_1_0));
        // without a CRS given, the Z/M markers of the tag determine the coordinate dimension
        Geometry<?> decoded = Wkt.fromWkt("MULTIPOINT ZM (EMPTY, EMPTY)", Wkt.Dialect.SFA_1_2_1);
        assertEquals(2, ((MultiPoint<?>) decoded).getNumGeometries());
        assertEquals(4, decoded.getCoordinateDimension());
        decoded = Wkt.fromWkt("MULTIPOINT ZM ((1 2 3 4), EMPTY)", Wkt.Dialect.SFA_1_2_1);
        assertEquals(4, decoded.getCoordinateDimension());
        assertTrue(((MultiPoint<?>) decoded).getGeometryN(1).isEmpty());
    }

    private void assertSameMembers(String msg, AbstractGeometryCollection<C2D, ?> expected, Geometry<?> actual) {
        assertEquals(msg, expected.getClass(), actual.getClass());
        AbstractGeometryCollection<?, ?> actualCollection = (AbstractGeometryCollection<?, ?>) actual;
        assertEquals(msg, expected.getNumGeometries(), actualCollection.getNumGeometries());
        for (int i = 0; i < expected.getNumGeometries(); i++) {
            assertEquals(msg + " member " + i, expected.getGeometryN(i), actualCollection.getGeometryN(i));
        }
        assertEquals(msg, expected, actual);
    }

    @Test(expected = WktDecodeException.class)
    public void wktRejectsNonEmptyMemberOfWrongShape() {
        Wkt.fromWkt("MULTIPOINT((1 2, 3 4))", crs, Wkt.Dialect.SFA_1_2_1);
    }
}
