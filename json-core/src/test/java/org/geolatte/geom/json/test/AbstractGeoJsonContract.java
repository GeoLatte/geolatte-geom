package org.geolatte.geom.json.test;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.Setting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

/**
 * Behavioural contract for the GeoJSON encode/decode pipeline.
 *
 * <p>This abstract base class is shared (via the {@code geolatte-geojson-core}
 * test-jar) by both the Jackson 2 and Jackson 3 adapter modules. It does not
 * itself contain any {@code @Test} methods — those live in the per-geometry-type
 * specifications: {@link PointSpec}, {@link LineStringSpec}, {@link PolygonSpec},
 * {@link MultiPointSpec}, {@link MultiLineStringSpec}, {@link MultiPolygonSpec},
 * {@link GeometryCollectionSpec}, {@link GeometrySpec}, {@link FeatureSpec},
 * {@link FeatureCollectionSpec}, {@link CrsSpec}, {@link SettingsSpec},
 * {@link InvariantSpec}.</p>
 *
 * <p>Each adapter provides one concrete subclass per spec, all delegating to a
 * single static factory helper. Together the two adapters run the full suite
 * against their native {@code ObjectMapper}, so any divergence in behaviour
 * between the Jackson 2 and Jackson 3 stacks surfaces as a failed test in one
 * of them.</p>
 */
public abstract class AbstractGeoJsonContract {

    /**
     * The single factory method that adapter subclasses must implement. All
     * convenience overloads delegate to this one.
     */
    protected abstract MapperLike newMapper(
            CoordinateReferenceSystem<?> defaultCrs,
            Map<Setting, Boolean> settings);

    protected MapperLike newMapper() {
        return newMapper(WGS84, Collections.emptyMap());
    }

    protected MapperLike newMapper(Map<Setting, Boolean> settings) {
        return newMapper(WGS84, settings);
    }

    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs) {
        return newMapper(defaultCrs, Collections.emptyMap());
    }

    protected MapperLike newMapper(Setting setting, boolean value) {
        Map<Setting, Boolean> map = new HashMap<>();
        map.put(setting, value);
        return newMapper(WGS84, map);
    }

    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs, Setting setting, boolean value) {
        Map<Setting, Boolean> map = new HashMap<>();
        map.put(setting, value);
        return newMapper(defaultCrs, map);
    }
}
