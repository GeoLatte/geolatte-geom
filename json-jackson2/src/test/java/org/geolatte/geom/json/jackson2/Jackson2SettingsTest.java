package org.geolatte.geom.json.jackson2;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.Setting;
import org.geolatte.geom.json.test.MapperLike;
import org.geolatte.geom.json.test.SettingsSpec;

import java.util.Map;

public class Jackson2SettingsTest extends SettingsSpec {
    @Override
    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
        return Jackson2MapperFactory.create(defaultCrs, settings);
    }
}
