package org.geolatte.geom.codec;

interface WktPositionEncoder {
    void addPoint(StringBuffer buffer, double[] coords);
}
