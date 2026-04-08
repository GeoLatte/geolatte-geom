package org.geolatte.geom.json.jackson2;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.Setting;
import org.geolatte.geom.json.test.MapperLike;
import org.geolatte.geom.json.test.MultiPointSpec;

import java.util.Map;

public class Jackson2MultiPointTest extends MultiPointSpec {
    @Override
    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
        return Jackson2MapperFactory.create(defaultCrs, settings);
    }
}
