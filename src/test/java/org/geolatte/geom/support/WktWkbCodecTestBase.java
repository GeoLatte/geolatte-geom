package org.geolatte.geom.support;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for specifying Wkt/Wkb test cases
 */
abstract public class WktWkbCodecTestBase {
    public final Map<Integer, CodecTestInput> testCases = new HashMap<Integer, CodecTestInput>();

    public WktWkbCodecTestBase() {
    }

    public void addCase(Integer key, String wkt, String wkb, Geometry<?> geom) {
        addCase(key, wkt, wkb, geom, true);
    }

    public void addCase(Integer key, String wkt, String wkb, Geometry<?> geom, boolean testEncoding) {
        CodecTestInput testInput = new CodecTestInput();
        testInput.wkt = wkt;
        testInput.wkbHex = wkb;
        testInput.wkb = ByteBuffer.from(wkb);
        testInput.expected = geom;
        testInput.testEncoding = testEncoding;
        this.testCases.put(key, testInput);

    }

    /**
     * Returns the WKT for the test case
     * @param testCase
     * @return
     */
    public String getWKT(Integer testCase) {
        return this.testCases.get(testCase).wkt;
    }

    /**
     * Return the {@code Geometry} expected when parsing Wkt/Wkb
     * @param testCase
     * @return
     */
    public Geometry<?> getExpected(Integer testCase) {
        return this.testCases.get(testCase).expected;
    }

    /**
     * Return the WKB for the test case
     * @param testCase
     * @return
     */
    public ByteBuffer getWKB(Integer testCase) {
        return this.testCases.get(testCase).wkb;
    }

    /**
     * Returns the WKB as a hex-string for the test case
     * @param testCase
     * @return
     */
    public String getWKBHexString(Integer testCase) {
        return this.testCases.get(testCase).wkbHex;
    }

    /**
     * Checks whether encoding the geometry into WKT should give the specified WKT.
     *
     * <p>In some cases (e.g. MySQL MultiPoint) a WKT representation should be parseable, although it is no strictly
     * correct WKT (a database-specific variant). The WKT encoders never return this representation.</p>
     *
     * @param testCase
     * @return
     */
    public boolean getTestEncoding(Integer testCase) {
        return this.testCases.get(testCase).testEncoding;
    }

    public Set<Integer> getCases() {
        return this.testCases.keySet();
    }

    static private class CodecTestInput {
        public String wkt;
        public ByteBuffer wkb;
        public Geometry expected;
        public boolean testEncoding = true;
        public String wkbHex;
    }

}