package org.geolatte.geom.codec;

public class SFA110WktDialect extends WktDialect {

    @Override
    boolean writeMultiPointAsListOfPositions() {
        return true;
    }

    @Override
    boolean isLimitedTo2D() {
        return true;
    }
}
