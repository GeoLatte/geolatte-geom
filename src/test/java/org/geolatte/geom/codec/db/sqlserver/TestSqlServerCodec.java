package org.geolatte.geom.codec.db.sqlserver;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryEquality;
import org.geolatte.geom.GeometryPointEquality;
import org.geolatte.geom.support.XmlDatabaseCodecTestCases;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/9/13
 */
public class TestSqlServerCodec {

    static XmlDatabaseCodecTestCases testCases;

    final GeometryEquality geometricEquality = new GeometryPointEquality();

    @BeforeClass
    public static void loadTestCases() {
        testCases = XmlDatabaseCodecTestCases.fromResource("sqlserver-test-data.xml");
    }

    @Test
    public void testEncoding() {

        runTest(new CodecTest() {
            void run(Integer id, String wkt, int srid, Geometry expected, ByteBuffer bytes) {
//                System.out.println(wkt);
                assertTrue(String.format("%d: encoding fails for ewkt srid=%d;%s", id, srid, wkt),
                        geometricEquality.equals(expected, Decoders.decode(bytes.toByteArray())));
            }
        });

    }

    @Test
    public void testDecoding() {

        runTest(new CodecTest() {
            void run(Integer id, String wkt, int srid, Geometry geom, ByteBuffer expected) {
//                System.out.println(String.format("%d: decoding for ewkt srid=%d;%s", id, srid, wkt));
                byte[] bytes = Encoders.encode(geom);
                ByteBuffer encoded = ByteBuffer.from(bytes);
                assertTrue(String.format("%d: decoding fails for ewkt srid=%d;%s", id, srid, wkt),
                        expected.hasSameContent(encoded));
            }
        });
    }

    void runTest(CodecTest test) {

        for (Integer id : testCases.all()) {
            String wkt = testCases.getEWKT(id);
            int srid = testCases.getSrid(id);
            Geometry expected = testCases.getGeometry(id);
            ByteBuffer bytes = (ByteBuffer) testCases.getDbRepresentation(id);
            test.run(id, wkt, srid, expected, bytes);
        }

    }

    abstract class CodecTest {
        abstract void run(Integer id, String wkt, int srid, Geometry expected, ByteBuffer bytes);
    }

}
