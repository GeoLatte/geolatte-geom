package org.geolatte.geom.codec;

/**
 * A WKT decoder for the format specified in Simple Feature Access, version 1.1.0
 */
class Sfa110WktDialect extends WktDialect {

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
