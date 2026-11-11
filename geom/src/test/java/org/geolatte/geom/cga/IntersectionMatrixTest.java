package org.geolatte.geom.cga;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests {@link IntersectionMatrix}.
 */
public class IntersectionMatrixTest {

    @Test
    public void entriesAndToString() {
        IntersectionMatrix m = matrix("212101212", 2, 2);
        assertEquals(2, m.get(Location.INTERIOR, Location.INTERIOR));
        assertEquals(1, m.get(Location.INTERIOR, Location.BOUNDARY));
        assertEquals(0, m.get(Location.BOUNDARY, Location.BOUNDARY));
        assertEquals(2, m.get(Location.EXTERIOR, Location.EXTERIOR));
        assertEquals("212101212", m.toString());
        assertEquals(IntersectionMatrix.FALSE, matrix("FF2FF1212", 2, 2).get(Location.INTERIOR, Location.INTERIOR));
    }

    @Test
    public void matches() {
        IntersectionMatrix m = matrix("212101212", 2, 2);
        assertTrue(m.matches("T*T***T**"));
        assertTrue(m.matches("2********"));
        assertTrue(m.matches("t*t***t**"));
        assertFalse(m.matches("1********"));
        assertFalse(m.matches("F********"));
        assertTrue(matrix("FF2FF1212", 2, 2).matches("FF*FF****"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPatternCharacter() {
        matrix("212101212", 2, 2).matches("21210121X");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPatternLength() {
        matrix("212101212", 2, 2).matches("2121");
    }

    @Test
    public void namedPredicates() {
        // overlapping squares
        IntersectionMatrix overlap = matrix("212101212", 2, 2);
        assertTrue(overlap.isIntersects());
        assertTrue(overlap.isOverlaps());
        assertFalse(overlap.isDisjoint() || overlap.isTouches() || overlap.isWithin() || overlap.isContains()
                || overlap.isEquals() || overlap.isCrosses() || overlap.isCovers() || overlap.isCoveredBy());
        // adjacent squares
        IntersectionMatrix adjacent = matrix("FF2F11212", 2, 2);
        assertTrue(adjacent.isTouches());
        assertFalse(adjacent.isOverlaps());
        // a square inside another one, touching its boundary
        IntersectionMatrix inside = matrix("2FF11F212", 2, 2);
        assertTrue(inside.isWithin() && inside.isCoveredBy());
        assertFalse(inside.isContains() || inside.isCovers());
        assertTrue(inside.transpose().isContains() && inside.transpose().isCovers());
        // equal
        assertTrue(matrix("2FFF1FFF2", 2, 2).isEquals());
        assertFalse(matrix("2FFF1FFF2", 2, 1).isEquals());
        // a line on the boundary of a polygon: covered by, not within
        IntersectionMatrix onBoundary = matrix("F1FF0F212", 1, 2);
        assertTrue(onBoundary.isCoveredBy());
        assertFalse(onBoundary.isWithin());
        assertTrue(onBoundary.isTouches());
        // disjoint
        IntersectionMatrix disjoint = matrix("FF2FF1212", 2, 2);
        assertTrue(disjoint.isDisjoint());
        assertFalse(disjoint.isIntersects() || disjoint.isTouches());
    }

    @Test
    public void crossesDependsOnDimensions() {
        // lines crossing in a point
        assertTrue(matrix("0F1FF0102", 1, 1).isCrosses());
        // lines overlapping: they do not cross, they overlap
        IntersectionMatrix lines = matrix("1010F0102", 1, 1);
        assertFalse(lines.isCrosses());
        assertTrue(lines.isOverlaps());
        // a line crossing a polygon
        IntersectionMatrix lineArea = matrix("101FF0212", 1, 2);
        assertTrue(lineArea.isCrosses());
        assertTrue(lineArea.transpose().isCrosses());
        assertFalse(lineArea.isOverlaps());
        // points: some inside, some outside a line
        assertTrue(matrix("0F0FFF102", 0, 1).isCrosses());
        // touching points do not touch
        assertFalse(matrix("0FFFFFFF2", 0, 0).isTouches());
        // two areas never cross
        assertFalse(matrix("212101212", 2, 2).isCrosses());
    }

    @Test
    public void emptyGeometries() {
        IntersectionMatrix empty = matrix("FFFFFFFF2", -1, -1);
        assertTrue(empty.isEquals());
        assertTrue(empty.isDisjoint());
        assertFalse(empty.isWithin() || empty.isCovers() || empty.isOverlaps() || empty.isCrosses() || empty.isTouches());
        assertFalse(matrix("FFFFFF212", -1, 2).isEquals());
    }

    @Test
    public void transposeAndEquality() {
        IntersectionMatrix m = matrix("1F2001102", 1, 2);
        IntersectionMatrix t = m.transpose();
        assertEquals("101F00212", t.toString());
        assertEquals(m, t.transpose());
        assertEquals(m.hashCode(), t.transpose().hashCode());
        assertNotEquals(m, t);
        assertEquals(2, t.getDimensionA());
        assertEquals(1, t.getDimensionB());
        assertEquals(m.get(Location.INTERIOR, Location.EXTERIOR), t.get(Location.EXTERIOR, Location.INTERIOR));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEntry() {
        new IntersectionMatrix(new int[]{3, 0, 0, 0, 0, 0, 0, 0, 2}, 2, 2);
    }

    private static IntersectionMatrix matrix(String entries, int dimA, int dimB) {
        int[] e = new int[9];
        for (int i = 0; i < 9; i++) e[i] = entries.charAt(i) == 'F' ? IntersectionMatrix.FALSE : entries.charAt(i) - '0';
        return new IntersectionMatrix(e, dimA, dimB);
    }
}
