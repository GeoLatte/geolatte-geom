package org.geolatte.geom.codec;

public class PostgisWkb2Encoder extends PostgisWkbEncoder {

    public PostgisWkb2Encoder() {
        super(PostgisWkbV2Dialect.INSTANCE);
    }
}