package org.geolatte.geom.codec.db.oracle;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interpreted triplet for a single element
 *
 * Created by Karel Maesen, Geovise BVBA on 15/02/2020.
 */
abstract class ElemInfoTriplet {

    private final int startingOffset;
    private final ElementType elementType;

    static ElemInfoTriplet parse(BigDecimal[] triplet) {
        int sOffset = triplet[0].intValue();
        int etype = triplet[1].intValue();
        int interp = triplet[2].intValue();
        ElementType et = ElementType.parseType(etype, interp);
        if(et.isCompound()) {
            return new CompoundIElemInfoTriplet(sOffset, et);
        } else {
            return new SimpleIElemInfoTriplet(sOffset, et);
        }
    }



    ElemInfoTriplet(int sOffset, ElementType et) {
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

    abstract ElemInfoTriplet shiftStartingOffset(int offset);

    void addTo(List<BigDecimal> list) {
        list.add(BigDecimal.valueOf(startingOffset));
        list.add(BigDecimal.valueOf(elementType.getEType()));
        list.add(BigDecimal.valueOf(elementType.getInterpretation()));
    }

}

class SimpleIElemInfoTriplet extends ElemInfoTriplet {

    SimpleIElemInfoTriplet(int sOffset, ElementType et) {
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

    ElemInfoTriplet shiftStartingOffset(int offset) {
        return new SimpleIElemInfoTriplet(this.getStartingOffset() + offset, this.getElementType());
    }
}


class CompoundIElemInfoTriplet extends ElemInfoTriplet {

    CompoundIElemInfoTriplet(int sOffset, ElementType et) {
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

    ElemInfoTriplet shiftStartingOffset(int offset) {
        return new CompoundIElemInfoTriplet(this.getStartingOffset() + offset, this.getElementType());
    }
}
