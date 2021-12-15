package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.G2D;
import org.geolatte.geom.G3DM;
import org.geolatte.geom.PositionSequence;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.geolatte.geom.CrsMock.WGS84_ZM;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.*;

/**
 * A Whitebox test for the SDO_ELEM_INFO Serialization/deserializtion
 * Created by Karel Maesen, Geovise BVBA on 15/02/2020.
 */
public class SDOElemInfoTest {

    @Test
    public void testSimpleElements() {
        SDOGType gtype = SDOGType.parse(2001);
        Double[] ordinates = new Double[]{3.0, 4.0};
        BigDecimal[] raw = bd(1, 1, 1);
        ElemInfo info = new ElemInfo(raw);
        List<Element> iinfo = info.interpret(gtype, ordinates);
        assertEquals(1, iinfo.size());
        assertEquals(new G2D(3., 4.), iinfo.get(0).linearizedPositions(gtype, WGS84).first());
        assertFalse(iinfo.get(0).isCompound());
        assertFalse(iinfo.get(0).isCircle());
    }

    @Test
    public void testPolygonInfo() {
        SDOGType gtype = SDOGType.parse(2003);
        Double[] ordinates = new Double[]{2d, 4d, 4d, 3d, 10d, 3d, 13d, 5d, 13d, 9d, 11d, 13d, 5d, 13d, 2d,
                11d, 2d, 4d, 7d, 5d, 7d, 10d, 10d, 10d, 10d, 5d, 7d, 5d};
        ElemInfo info = new ElemInfo(bd(1, 1003, 1, 19, 2003, 1));
        List<Element> iinfo = info.interpret(gtype, ordinates);
        assertEquals(2, iinfo.size());
        assertEquals(9, iinfo.get(0).linearizedPositions(gtype, WGS84).size());
        assertEquals(new G2D(2, 4), iinfo.get(0).linearizedPositions(gtype, WGS84).first());
        assertFalse(iinfo.get(0).isCompound());
        assertEquals(ElementType.EXTERIOR_RING_STRAIGHT_SEGMENTS, iinfo.get(0).getElementType());

        //second element
        assertEquals(5, iinfo.get(1).linearizedPositions(gtype, WGS84).size());
        assertEquals(new G2D(7, 5), iinfo.get(1).linearizedPositions(gtype, WGS84).first());
        assertFalse(iinfo.get(1).isCompound());
        assertEquals(ElementType.INTERIOR_RING_STRAIGHT_SEGMENTS, iinfo.get(1).getElementType());

    }


    @Test
    public void testCompoundElement() {
        //see Figure 2.5 in Spatial Documentation, para. 2.7 Geometry Examples
        SDOGType gtype = SDOGType.parse(2002); //line or curve
        Double[] ordinates = new Double[]{10., 10., 10., 14., 6., 10., 14., 10.};
        ElemInfo info = new ElemInfo(bd(1, 4, 2, 1, 2, 1, 3, 2, 2));
        List<Element> iinfo = info.interpret(gtype, ordinates);
        assertEquals(1, iinfo.size());
        assertTrue(iinfo.get(0) instanceof CompoundElement);
        CompoundElement elem = (CompoundElement) iinfo.get(0);
        assertEquals(2, elem.getSubelements().size());
        PositionSequence<G2D> positions = elem.getSubelements().get(0).linearizedPositions(gtype, WGS84);
        assertEquals(2, positions.size());
        assertEquals(new G2D(10., 10.), positions.first());
        positions = elem.getSubelements().get(1).linearizedPositions(gtype, WGS84);
        assertEquals(new G2D(10, 14), positions.first());
        assertEquals(new G2D(14, 10), positions.getPositionN(positions.size() - 1));
    }

    @Test
    public void test4Dcoordinates() {
        SDOGType gtype = SDOGType.parse(4404);
        Double[] ordinates = new Double[]{
                10., 5., 1., 2.,
                20., 6., 2., 3.,
                1., 1., 49., 1.,
                99., 99., 1., 99.};
        ElemInfo info = new ElemInfo(bd(1, 2, 1));
        List<Element> iinfo = info.interpret(gtype, ordinates);
        assertEquals(1, iinfo.size());
        PositionSequence<G3DM> positions = iinfo.get(0).linearizedPositions(gtype, WGS84_ZM);
        assertEquals(4, positions.size());
        assertEquals(new G3DM(10, 5, 1, 2), positions.first());
    }

    @Test
    public void testEmptyPoint() {
        SDOGType gtype = SDOGType.parse(2005);
        Double[] ordinates = new Double[]{10., 5.};
        ElemInfo info = new ElemInfo(bd(1, 1, 1, 1, 1, 1));
        List<Element> elements = info.interpret(gtype, ordinates);
        PositionSequence<G2D> positions = elements.get(0).linearizedPositions(gtype, WGS84);
        assertEquals(0, positions.size());
        positions = elements.get(1).linearizedPositions(gtype, WGS84);
        assertEquals(1, positions.size());
    }

    @Test
    public void test4DMcoordinatesBeforeZ() {
        SDOGType gtype = SDOGType.parse(4304);
        Double[] ordinates = new Double[]{
                10., 5., 1., 2.,
                20., 6., 2., 3.,
                1., 1., 49., 1.,
                99., 99., 1., 99.};
        ElemInfo info = new ElemInfo(bd(1, 2, 1));
        List<Element> iinfo = info.interpret(gtype, ordinates);
        assertEquals(1, iinfo.size());
        PositionSequence<G3DM> positions = iinfo.get(0).linearizedPositions(gtype, WGS84_ZM);
        assertEquals(4, positions.size());
        assertEquals(new G3DM(10, 5, 2, 1), positions.first());
    }

    @Test
    public void testRectangle(){
        //see example 2.7.1 in Oracle Spatial documentation
        SDOGType gtype = SDOGType.parse(2003);
        Double[] ordinates = new Double[]{1., 1., 5., 7.};
        ElemInfo info = new ElemInfo(bd(1, 1003, 3));
        Element element = info.interpret(gtype, ordinates).get(0);
        assertFalse(element.isCompound());
        assertEquals(ElementType.EXTERIOR_RING_RECT, element.getElementType());
        PositionSequence<G2D> positions = element.linearizedPositions(gtype, WGS84);
        assertEquals(5, positions.size());
    }

    @Test
    public void testGeomCollection() {
        SDOGType gtype = SDOGType.parse(2004);
        Double[] ordinates = new Double[]{10., 5., 10., 10., 20., 10., 21., 11.,
                0., 0., 50., 0., 100., 100., 0., 100., 0., 0., 1., 1., 49., 1., 99., 99., 1., 99., 1., 1.};
        ElemInfo info = new ElemInfo(bd(1, 1, 1, 3, 1, 3, 9, 1003, 1, 19, 2003, 1));
        List<Element> iinfo = info.interpret(gtype, ordinates);
        assertEquals(4, iinfo.size());
        PositionSequence<G2D> positions = iinfo.get(0).linearizedPositions(gtype, WGS84);
        assertEquals(1, positions.size());
        assertEquals(new G2D(10, 5), positions.first());
        assertFalse(iinfo.get(0).isCompound());
        assertEquals(ElementType.POINT, iinfo.get(0).getElementType());

        //second element
        positions = iinfo.get(1).linearizedPositions(gtype, WGS84);
        assertEquals(3, positions.size());
        assertEquals(new G2D(10, 10), positions.first());
        assertFalse(iinfo.get(1).isCompound());
        assertEquals(ElementType.POINT_CLUSTER, iinfo.get(1).getElementType());

        positions = iinfo.get(2).linearizedPositions(gtype, WGS84);
        assertEquals(5, positions.size());
        assertEquals(new G2D(0, 0), positions.first());
        assertEquals(ElementType.EXTERIOR_RING_STRAIGHT_SEGMENTS, iinfo.get(2).getElementType());

        positions = iinfo.get(3).linearizedPositions(gtype, WGS84);
        assertEquals(5, positions.size());
        assertEquals(new G2D(1, 1), positions.first());
        assertEquals(ElementType.INTERIOR_RING_STRAIGHT_SEGMENTS, iinfo.get(3).getElementType());
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


