package org.geolatte.geom.codec;

public class SFA110WktDialect extends WktDialect {

    public static final SFA110WktDialect INSTANCE = new SFA110WktDialect();

    @Override
    boolean writeMultiPointAsListOfPositions() {
        return true;
    }

    @Override
    boolean isLimitedTo2D() {
        return true;
    }
}
