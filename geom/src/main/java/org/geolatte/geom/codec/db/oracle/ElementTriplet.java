package org.geolatte.geom.codec.db.oracle;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interpreted triplet for a single element
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 15/02/2020.
 */
@Deprecated
abstract class ElementTriplet {

    private final int startingOffset;
    private final ElementType elementType;

    static ElementTriplet parse(BigDecimal[] triplet) {
        int sOffset = triplet[0].intValue();
        int etype = triplet[1].intValue();
        int interp = triplet[2].intValue();
        ElementType et = ElementType.parseType(etype, interp);
        if (et.isCompound()) {
            return new CompoundElementTriplet(sOffset, et);
        } else {
            return new SimpleElementTriplet(sOffset, et);
        }
    }


    ElementTriplet(int sOffset, ElementType et) {
        startingOffset = sOffset;
        elementType = et;
    }

    /**
     * Starting offset in ordinates array for this element (1-based!)
     *
     * @return
     */
    int getStartingOffset() {
        return startingOffset;
    }

    abstract boolean isSimple();

    abstract boolean isCompound();

    ElementType getElementType() {
        return elementType;
    }

    abstract ElementTriplet shiftStartingOffset(int offset);

    void addTo(List<BigDecimal> list) {
        list.add(BigDecimal.valueOf(startingOffset));
        list.add(BigDecimal.valueOf(elementType.getEType()));
        list.add(BigDecimal.valueOf(elementType.getInterpretation()));
    }

}

@Deprecated
class SimpleElementTriplet extends ElementTriplet {

    SimpleElementTriplet(int sOffset, ElementType et) {
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

    ElementTriplet shiftStartingOffset(int offset) {
        return new SimpleElementTriplet(this.getStartingOffset() + offset, this.getElementType());
    }
}

@Deprecated
class CompoundElementTriplet extends ElementTriplet {

    CompoundElementTriplet(int sOffset, ElementType et) {
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

    int numParts() {
        return getElementType().getInterpretation();
    }

    @Deprecated
    ElementTriplet shiftStartingOffset(int offset) {
        return new CompoundElementTriplet(this.getStartingOffset() + offset, this.getElementType());
    }
}
