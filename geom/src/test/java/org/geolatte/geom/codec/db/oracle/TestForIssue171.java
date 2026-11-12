package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Polygon;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// for testing issue #171: decoding this polygon caused an OutOfMemoryError
public class TestForIssue171 {

    @Test(timeout = 5000)
    public void testCompoundPolygonWithNearlyCollinearArc() {
        int[] elems = {1, 1005, 2, 1, 2, 1, 49, 2, 2};
        Double[] coords = {
                33375930.096, 6017592.535, 0., 33375926.631, 6017591.297, 0., 33375924.383, 6017590.514, 0.,
                33375883.231, 6017576.178, 0., 33375862.305, 6017568.889, 0., 33375856.014, 6017552.118, 0.,
                33375844.541, 6017515.331, 0., 33375849.332, 6017516.935, 0., 33375851.866, 6017517.792, 0.,
                33375863.228, 6017521.556, 0., 33375874.592, 6017525.325, 0., 33375885.954, 6017529.092, 0.,
                33375897.316, 6017532.859, 0., 33375908.896, 6017536.8, 0., 33375920.031, 6017540.626, 0.,
                33375941.906, 6017548.036, 0., 33375945.536, 6017549.333, 0., 33375937.816, 6017570.934, 0.,
                33375930.096, 6017592.535, 0.
        };
        SDOGeometry sdo = SDOGeometryHelper.sdoGeometry(3003, 0, null, elems, coords);
        Geometry<?> geom = Decoders.decode(sdo);
        assertTrue(geom instanceof Polygon);
        // the arc is nearly a straight line, so it should not be densified
        // (the start position of the arc is repeated when the subelements of the compound element are combined)
        assertEquals(coords.length / 3 + 1, geom.getNumPositions());
    }

}
