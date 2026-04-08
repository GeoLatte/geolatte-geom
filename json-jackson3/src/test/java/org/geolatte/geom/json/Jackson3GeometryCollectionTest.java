package org.geolatte.geom.json;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.test.GeometryCollectionSpec;
import org.geolatte.geom.json.test.MapperLike;

import java.util.Map;

public class Jackson3GeometryCollectionTest extends GeometryCollectionSpec {
    @Override
    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
        return Jackson3MapperFactory.create(defaultCrs, settings);
    }
}
