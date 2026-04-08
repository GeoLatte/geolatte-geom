package org.geolatte.geom.json;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.test.MapperLike;
import org.geolatte.geom.json.test.PointSpec;

import java.util.Map;

public class Jackson3PointTest extends PointSpec {
    @Override
    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
        return Jackson3MapperFactory.create(defaultCrs, settings);
    }
}
