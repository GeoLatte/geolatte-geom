package org.geolatte.geom.support;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CodecTestBase {
    public final Map<Integer, CodecTestInput> testCases = new HashMap<Integer, CodecTestInput>();

    public CodecTestBase() {
    }

    public void addCase(Integer key, String wkt, String wkb, Geometry geom) {
        addCase(key, wkt, wkb, geom, true);
    }

    public void addCase(Integer key, String wkt, String wkb, Geometry geom, boolean testEncoding) {
        CodecTestInput testInput = new CodecTestInput();
        testInput.wkt = wkt;
        testInput.wkbHex = wkb;
        testInput.wkb = ByteBuffer.from(wkb);
        testInput.expected = geom;
        testInput.testEncoding = testEncoding;
        this.testCases.put(key, testInput);

    }

    public String getWKT(Integer testCase) {
        return this.testCases.get(testCase).wkt;
    }

    public Geometry getExpected(Integer testCase) {
        return this.testCases.get(testCase).expected;
    }

    public ByteBuffer getWKB(Integer testCase) {
        return this.testCases.get(testCase).wkb;
    }

    public String getWKBHexString(Integer testCase) {
        return this.testCases.get(testCase).wkbHex;
    }

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