package org.geolatte.geom.codec;

public class Sfa110WktDialect extends WktDialect {

    public static final Sfa110WktDialect INSTANCE = new Sfa110WktDialect();

    @Override
    boolean writeMultiPointAsListOfPositions() {
        return true;
    }

    @Override
    boolean isLimitedTo2D() {
        return true;
    }
}
