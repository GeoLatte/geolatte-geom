package org.geolatte.geom.json.jackson2;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.Setting;
import org.geolatte.geom.json.test.MapperLike;
import org.geolatte.geom.json.test.MultiPolygonSpec;

import java.util.Map;

public class Jackson2MultiPolygonTest extends MultiPolygonSpec {
    @Override
    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
        return Jackson2MapperFactory.create(defaultCrs, settings);
    }
}
