package org.geolatte.geom.codec.db.oracle;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A Whitebox test for the SDO_ELEM_INFO Serialization/deserializtion
 * Created by Karel Maesen, Geovise BVBA on 15/02/2020.
 */
public class SDOElemInfoTest {

    @Test
    public void testSimpleElements(){
        BigDecimal[] raw = bd(1, 1, 1);
        ElemInfo info = new ElemInfo(raw);
        ElemInfoTriplet[] iinfo = info.interpret();
        assertEquals(1, iinfo.length);
        assertEquals(1, iinfo[0].getStartingOffset());
        assertTrue(iinfo[0].isSimple());
    }

    @Test
    public void testGeometryCollectionInfo(){
        ElemInfo info = new ElemInfo(bd(1, 1, 1, 3, 2, 1, 7, 1003, 1));
        ElemInfoTriplet[] iinfo = info.interpret();
        assertEquals(3, iinfo.length);
        assertEquals(1, iinfo[0].getStartingOffset());
        assertTrue(iinfo[0].isSimple());
        assertEquals(ElementType.POINT, iinfo[0].getElementType());

        //second element
        assertEquals(3, iinfo[1].getStartingOffset());
        assertTrue(iinfo[1].isSimple());
        assertEquals(ElementType.LINE_STRAITH_SEGMENTS, iinfo[1].getElementType());

        //third element
        assertEquals(7, iinfo[2].getStartingOffset());
        assertEquals(ElementType.EXTERIOR_RING_STRAIGHT_SEGMENTS, iinfo[2].getElementType());

    }

    @Test
    public void testPolygonInfo(){

        ElemInfo info = new ElemInfo(bd(1,1003,1,11,2003,1));
        ElemInfoTriplet[] iinfo = info.interpret();
        assertEquals(2, iinfo.length);
        assertEquals(1, iinfo[0].getStartingOffset());
        assertTrue(iinfo[0].isSimple());
        assertEquals(ElementType.EXTERIOR_RING_STRAIGHT_SEGMENTS, iinfo[0].getElementType());

        //second element
        assertEquals(11, iinfo[1].getStartingOffset());
        assertTrue(iinfo[1].isSimple());
        assertEquals(ElementType.INTERIOR_RING_STRAIGHT_SEGMENTS, iinfo[1].getElementType());

    }



    /// utility to create BigDecimal arrays
    private BigDecimal[] bd(int... vals) {
        BigDecimal[] result = new BigDecimal[vals.length];
        for (int idx = 0; idx < vals.length; idx++) {
            result[idx] = BigDecimal.valueOf(vals[idx]);
        }
        return result;
    }

}


