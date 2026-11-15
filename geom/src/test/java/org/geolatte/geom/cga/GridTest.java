package org.geolatte.geom.cga;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class GridTest {

    @Test
    public void factoriesAgree() {
        Grid mm = Grid.fixed("0.001");
        assertEquals(mm, Grid.fixed(0.001));
        assertEquals(mm, Grid.fixed(new BigDecimal("0.0010")));
        assertEquals(mm, Grid.ofScale(1000));
        assertEquals(mm, Grid.ofScale(new BigDecimal("1E3")));
        assertEquals(mm.hashCode(), Grid.ofScale(1000).hashCode());
        assertEquals(Grid.fixed(10), Grid.ofScale(0.1));
        assertEquals(Grid.fixed("0.25"), Grid.ofScale(4));
        assertNotEquals(mm, Grid.fixed("0.01"));
        assertNotEquals(mm, Grid.DOUBLE);
        assertEquals(new BigDecimal("0.001"), mm.cellSize());
        assertTrue(mm.isFixed());
        assertEquals("Grid[0.001]", mm.toString());
        assertEquals("Grid[10]", Grid.fixed(10).toString());
    }

    @Test
    public void invalidGrids() {
        assertThrows(IllegalArgumentException.class, () -> Grid.ofScale(3));
        assertThrows(IllegalArgumentException.class, () -> Grid.ofScale(0));
        assertThrows(IllegalArgumentException.class, () -> Grid.fixed(0));
        assertThrows(IllegalArgumentException.class, () -> Grid.fixed(-1));
        assertThrows(IllegalArgumentException.class, () -> Grid.fixed(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Grid.fixed(Double.POSITIVE_INFINITY));
        assertThrows(NumberFormatException.class, () -> Grid.fixed("mm"));
    }

    @Test
    public void theDoubleGrid() {
        assertFalse(Grid.DOUBLE.isFixed());
        assertEquals(BigDecimal.ZERO, Grid.DOUBLE.cellSize());
        assertEquals(0.1, Grid.DOUBLE.round(0.1), 0);
        assertEquals(0L, Double.doubleToRawLongBits(Grid.DOUBLE.round(-0.0)));
        assertThrows(IllegalArgumentException.class, () -> Grid.DOUBLE.round(Double.NaN));
    }

    @Test
    public void rounding() {
        assertEquals(152000.123, Grid.fixed("0.001").round(152000.1234999), 0);
        assertEquals(20, Grid.fixed(10).round(15), 0);
        assertEquals(0.5, Grid.ofScale(2).round(0.4), 0);
    }
}
