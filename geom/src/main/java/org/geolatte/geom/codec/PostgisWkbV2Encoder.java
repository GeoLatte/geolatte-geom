package org.geolatte.geom.codec;

/**
 * A Postgis WKB Encoder that encodes empty points as points with NaN coordinates.
 *
 * <p>The convention to encode empty points as points with NaN coordinates was
 * introduced in Postgis v2.2.2</p>
 */
public class PostgisWkbV2Encoder extends PostgisWkbEncoder {

    public PostgisWkbV2Encoder() {
        super(PostgisWkbV2Dialect.INSTANCE);
    }
}