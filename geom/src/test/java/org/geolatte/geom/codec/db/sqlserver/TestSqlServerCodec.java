package org.geolatte.geom.codec.db.sqlserver;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryEquality;
import org.geolatte.geom.GeometryPositionEquality;
import org.geolatte.geom.codec.testcases.XmlDatabaseCodecTestCases;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the codec against the test cases in sqlserver-test-data.xml. Each test case is run as a separate test.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/9/13
 */
@RunWith(Parameterized.class)
public class TestSqlServerCodec {

    static final XmlDatabaseCodecTestCases testCases = XmlDatabaseCodecTestCases.fromResource("sqlserver-test-data.xml");

    final GeometryEquality geometricEquality = new GeometryPositionEquality();

    private final String message;
    private final Geometry<?> expected;
    private final ByteBuffer bytes;

    @Parameters(name = "{index}: case {0}")
    public static Collection<Object[]> testCases() {
        List<Object[]> ids = new ArrayList<>();
        for (Integer id : testCases.all()) {
            ids.add(new Object[]{id});
        }
        return ids;
    }

    public TestSqlServerCodec(Integer id) {
        this.message = String.format("%d: ewkt srid=%d;%s", id, testCases.getSrid(id), testCases.getEWKT(id));
        this.expected = testCases.getGeometry(id);
        this.bytes = (ByteBuffer) testCases.getDbRepresentation(id);
    }

    @Test
    public void testDecoding() {
        Geometry decoded = Decoders.decode(bytes.toByteArray());
        // GeometryPositionEquality considers all empty geometries equal, so check SRID and type separately.
        assertEquals("decoding SRID for " + message, expected.getSRID(), decoded.getSRID());
        assertEquals("decoding type for " + message, expected.getGeometryType(), decoded.getGeometryType());
        assertTrue("decoding fails for " + message, geometricEquality.equals(expected, decoded));
    }

    /**
     * SQL Server marks invalid geometries in the serialization, so encoding checks validity to match the test cases.
     */
    @Test
    public void testEncoding() {
        ByteBuffer encoded = ByteBuffer.from(Encoders.encode(expected, true));
        assertTrue("encoding fails for " + message, bytes.hasSameContent(encoded));
    }

}
