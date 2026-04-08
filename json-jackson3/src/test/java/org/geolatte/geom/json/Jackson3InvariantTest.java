package org.geolatte.geom.json;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.test.InvariantSpec;
import org.geolatte.geom.json.test.MapperLike;

import java.util.Map;

public class Jackson3InvariantTest extends InvariantSpec {
    @Override
    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
        return Jackson3MapperFactory.create(defaultCrs, settings);
    }
}
