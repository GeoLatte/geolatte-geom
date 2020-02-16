package org.geolatte.geom.codec.db.oracle;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interpreted triplet for a single element
 *
 * Created by Karel Maesen, Geovise BVBA on 15/02/2020.
 */
abstract class InterpretedElemInfo {

    private final int startingOffset;
    private final ElementType elementType;

    static InterpretedElemInfo parse(BigDecimal[] triplet) {
        int sOffset = triplet[0].intValue();
        int etype = triplet[1].intValue();
        int interp = triplet[2].intValue();
        ElementType et = ElementType.parseType(etype, interp);
        if(et.isCompound()) {
            return new CompoundIElemInfo(sOffset, et);
        } else {
            return new SimpleIElemInfo(sOffset, et);
        }
    }



    InterpretedElemInfo(int sOffset, ElementType et) {
        startingOffset = sOffset;
        elementType = et;
    }

    /**
     * Starting offset in ordinates array for this element (1-based!)
     * @return
     */
    int getStartingOffset(){
        return startingOffset;
    }

    abstract boolean isSimple();

    abstract boolean isCompound();

    ElementType getElementType() {
        return elementType;
    }

    abstract InterpretedElemInfo shiftStartingOffset(int offset);

    void addTo(List<BigDecimal> list) {
        list.add(BigDecimal.valueOf(startingOffset));
        list.add(BigDecimal.valueOf(elementType.getEType()));
        list.add(BigDecimal.valueOf(elementType.getInterpretation()));
    }

}

class SimpleIElemInfo extends InterpretedElemInfo {

    SimpleIElemInfo(int sOffset, ElementType et) {
        super(sOffset, et);
    }

    @Override
    boolean isSimple() {
        return true;
    }

    @Override
    boolean isCompound() {
        return false;
    }

    InterpretedElemInfo shiftStartingOffset(int offset) {
        return new SimpleIElemInfo(this.getStartingOffset() + offset, this.getElementType());
    }
}


class CompoundIElemInfo extends InterpretedElemInfo {

    CompoundIElemInfo(int sOffset, ElementType et) {
        super(sOffset, et);
    }

    @Override
    boolean isSimple() {
        return false;
    }

    @Override
    boolean isCompound() {
        return true;
    }

    int numParts(){
        return getElementType().getInterpretation();
    }

    InterpretedElemInfo shiftStartingOffset(int offset) {
        return new CompoundIElemInfo(this.getStartingOffset() + offset, this.getElementType());
    }
}
