package org.geolatte.geom.codec.db.sqlserver;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryEquality;
import org.geolatte.geom.GeometryPositionEquality;
import org.geolatte.geom.LineString;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.Wkt;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;
import java.util.HexFormat;

import static org.junit.Assert.*;

/**
 * Tests of the SQL Server codec that the fixtures of {@link TestSqlServerCodec} cannot express, because the
 * value has no EWKT notation, may only be decoded, or must be rejected.
 *
 * <p>Byte arrays are either taken from [MS-SSCLRT] "Microsoft SQL Server CLR Types Serialization Formats" or from
 * SQL Server, or composed field by field according to the structure layout in that specification (see {@link Bytes}).
 */
public class TestSqlServerCodecEdgeCases {

    // Serialization Properties bits (section 2.1.1)
    private static final int Z = 0x01;
    private static final int V = 0x04;
    private static final int P = 0x08;

    // Figure Attribute values in version 1 (section 2.1.3)
    private static final int STROKE = 1;
    private static final int EXTERIOR_RING = 2;

    // OpenGIS Type values (section 2.1.4)
    private static final int POINT = 1;
    private static final int LINESTRING = 2;
    private static final int POLYGON = 3;
    private static final int MULTIPOINT = 4;
    private static final int GEOMETRYCOLLECTION = 7;

    private final GeometryEquality geometryEquality = new GeometryPositionEquality();

    // --- [MS-SSCLRT] 3.1.3: a NULL Z value ---

    @Test
    public void lineStringWithNullZValue() {
        byte[] bytes = hex("E6100000 01 05 03000000 "
                + "0000000000000000 000000000000F03F 0000000000000840 0000000000000040 "
                + "0000000000001040 0000000000001440 "
                + "000000000000F03F 0000000000000040 000000000000F8FF "
                + "01000000 01 00000000 01000000 FFFFFFFF 00000000 02");

        Geometry<?> decoded = Decoders.decode(bytes);
        assertTrue(decoded instanceof LineString);
        assertEquals(4326, decoded.getSRID());
        assertEquals(3, decoded.getNumPositions());
        assertPosition(decoded.getPositionN(0), 0, 1, 1);
        assertPosition(decoded.getPositionN(1), 3, 2, 2);
        Position last = decoded.getPositionN(2);
        assertEquals(4, last.getCoordinate(0), 0);
        assertEquals(5, last.getCoordinate(1), 0);
        assertTrue("NULL Z value must be decoded as NaN", Double.isNaN(last.getCoordinate(2)));

        assertEquals(hex(bytes), hex(Encoders.encode(decoded)));
    }

    // --- Section 2.1.1: SRID ---

    /**
     * A geometry without SRID has SRID -1, which denotes a null value in the serialization format.
     */
    @Test
    public void undefinedSridIsEncodedAsZero() {
        byte[] bytes = new Bytes().header(0, V | P | Z).doubles(1, 2).doubles(3).toArray();
        assertEquals(hex(bytes), hex(Encoders.encode(Wkt.fromWkt("POINT(1 2 3)"))));
    }

    // --- Section 2.1.4: empty shapes ---

    /**
     * An empty collection as first member of a collection. The bytes are composed by hand, following the layout
     * SQL Server produces for GEOMETRYCOLLECTION(POINT EMPTY, POINT(1 1)) (test case 47 in sqlserver-test-data.xml).
     */
    @Test
    public void geometryCollectionStartingWithEmptyMultiPoint() {
        byte[] bytes = new Bytes().header(4326, V)
                .int32(1).doubles(1, 1)
                .int32(1).figure(STROKE, 0)
                .int32(3).shape(-1, 0, GEOMETRYCOLLECTION).shape(0, -1, MULTIPOINT).shape(0, 0, POINT)
                .toArray();
        assertDecodes("SRID=4326;GEOMETRYCOLLECTION(MULTIPOINT EMPTY, POINT(1 1))", bytes);
    }

    // --- Section 2.1.1: Serialization Properties without the V flag ---

    /**
     * Without validity check, an invalid geometry is marked valid. Test case 51 in sqlserver-test-data.xml has the
     * same polygon as serialized by SQL Server, without the V flag.
     */
    @Test
    public void invalidGeometryIsMarkedValidWithoutValidityCheck() {
        byte[] bytes = new Bytes().header(4326, V)
                .int32(5).doubles(0, 0, 1, 1, 1, 0, 0, 1, 0, 0)
                .int32(1).figure(EXTERIOR_RING, 0)
                .int32(1).shape(-1, 0, POLYGON)
                .toArray();
        assertEquals(hex(bytes), hex(Encoders.encode(Wkt.fromWkt("SRID=4326;POLYGON((0 0, 1 1, 1 0, 0 1, 0 0))"))));
    }

    @Test
    public void decodeSinglePointWithoutValidFlag() {
        byte[] bytes = new Bytes().header(4326, P).doubles(5, 10).toArray();
        assertDecodes("SRID=4326;POINT(5 10)", bytes);
    }

    // --- Values outside of version 1 of the specification ---

    /**
     * CIRCULARSTRING(0 0, 1 1, 2 0) with SRID 4326, as serialized by SQL Server.
     */
    @Test
    public void version2IsRejected() {
        byte[] bytes = hex("E6100000 02 04 03000000 "
                + "0000000000000000 0000000000000000 000000000000F03F 000000000000F03F "
                + "0000000000000040 0000000000000000 "
                + "01000000 02 00000000 01000000 FFFFFFFF 00000000 08");
        assertThrows(IllegalStateException.class, () -> Decoders.decode(bytes));
    }

    @Test
    public void unknownFigureAttributeIsRejected() {
        byte[] bytes = new Bytes().header(0, V)
                .int32(2).doubles(0, 0, 1, 1)
                .int32(1).figure(3, 0)
                .int32(1).shape(-1, 0, LINESTRING)
                .toArray();
        assertThrows(IllegalArgumentException.class, () -> Decoders.decode(bytes));
    }

    @Test
    public void unknownOpenGisTypeIsRejected() {
        byte[] bytes = new Bytes().header(0, V)
                .int32(2).doubles(0, 0, 1, 1)
                .int32(1).figure(STROKE, 0)
                .int32(1).shape(-1, 0, 8)
                .toArray();
        assertThrows(IllegalArgumentException.class, () -> Decoders.decode(bytes));
    }

    // --- GEOGRAPHY: points are stored as (latitude, longitude) (section 2.1.5) ---

    /**
     * [MS-SSCLRT] 3.1.4, which is a GEOGRAPHY example.
     */
    @Test
    public void geographySpecExampleGeometryCollection() {
        byte[] bytes = hex("E6100000 01 04 0D000000 "
                + "0000000000000000 0000000000001040 0000000000000040 0000000000001040 "
                + "0000000000000840 0000000000001440 0000000000000000 0000000000000000 "
                + "0000000000000000 0000000000000840 0000000000000840 0000000000000840 "
                + "0000000000000840 0000000000000000 0000000000000000 0000000000000000 "
                + "000000000000F03F 000000000000F03F 0000000000000040 000000000000F03F "
                + "0000000000000040 0000000000000040 000000000000F03F 0000000000000040 "
                + "000000000000F03F 000000000000F03F "
                + "04000000 01 00000000 01 01000000 02 03000000 00 08000000 "
                + "04000000 FFFFFFFF 00000000 07 00000000 00000000 01 00000000 01000000 02 "
                + "00000000 02000000 03");
        String ewkt = "SRID=4326;GEOMETRYCOLLECTION(POINT(4 0), LINESTRING(4 2, 5 3), "
                + "POLYGON((0 0, 3 0, 3 3, 0 3, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1)))";
        assertDecoded(ewkt, bytes, GeographyDecoders.decode(bytes));
        assertEquals(hex(bytes), hex(GeographyEncoders.encode(Wkt.fromWkt(ewkt))));
    }

    @Test
    public void geometryAndGeographyDecodeWithSwappedAxes() {
        byte[] bytes = new Bytes().header(4326, V | P).doubles(10, 5).toArray();
        assertDecoded("SRID=4326;POINT(10 5)", bytes, Decoders.decode(bytes));
        assertDecoded("SRID=4326;POINT(5 10)", bytes, GeographyDecoders.decode(bytes));
    }

    @Test
    public void geographyWithoutSridIsEncodedWithDefaultSrid() {
        byte[] bytes = new Bytes().header(4326, V | P).doubles(2, 1).toArray();
        assertEquals(hex(bytes), hex(GeographyEncoders.encode(Wkt.fromWkt("POINT(1 2)"))));
    }

    @Test
    public void geographyWithSridOutsideRangeIsRejected() {
        Geometry<?> geom = Wkt.fromWkt("SRID=31370;POINT(150000 200000)");
        assertThrows(IllegalArgumentException.class, () -> GeographyEncoders.encode(geom));
    }

    // --- helpers ---

    private void assertDecodes(String ewkt, byte[] bytes) {
        assertDecoded(ewkt, bytes, Decoders.decode(bytes));
    }

    private void assertDecoded(String ewkt, byte[] bytes, Geometry<?> decoded) {
        Geometry<?> expected = Wkt.fromWkt(ewkt);
        String msg = String.format("decoding %s: expected %s but was %s", hex(bytes), ewkt, Wkt.toWkt(decoded));
        assertEquals(msg, expected.getSRID(), decoded.getSRID());
        assertEquals(msg, expected.getGeometryType(), decoded.getGeometryType());
        assertEquals(msg, expected.isEmpty(), decoded.isEmpty());
        assertTrue(msg, geometryEquality.equals((Geometry) expected, (Geometry) decoded));
    }

    private static void assertPosition(Position pos, double... coordinates) {
        for (int i = 0; i < coordinates.length; i++) {
            assertEquals(coordinates[i], pos.getCoordinate(i), 0);
        }
    }

    private static byte[] hex(String hex) {
        return HexFormat.of().parseHex(hex.replaceAll("\\s", ""));
    }

    private static String hex(byte[] bytes) {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }

    /**
     * Composes a serialized GEOMETRY structure field by field, in little-endian byte order.
     */
    private static class Bytes {

        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        Bytes header(int srid, int properties) {
            return header(srid, 1, properties);
        }

        Bytes header(int srid, int version, int properties) {
            return int32(srid).int8(version).int8(properties);
        }

        Bytes figure(int attribute, int pointOffset) {
            return int8(attribute).int32(pointOffset);
        }

        Bytes shape(int parentOffset, int figureOffset, int openGisType) {
            return int32(parentOffset).int32(figureOffset).int8(openGisType);
        }

        Bytes int8(int value) {
            out.write(value);
            return this;
        }

        Bytes int32(int value) {
            return put(java.nio.ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value));
        }

        Bytes doubles(double... values) {
            for (double value : values) {
                put(java.nio.ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(value));
            }
            return this;
        }

        byte[] toArray() {
            return out.toByteArray();
        }

        private Bytes put(java.nio.ByteBuffer buffer) {
            out.write(buffer.array(), 0, buffer.capacity());
            return this;
        }
    }
}
