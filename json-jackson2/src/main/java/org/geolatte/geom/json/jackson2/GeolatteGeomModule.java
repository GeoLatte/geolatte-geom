package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.geolatte.geom.Box;
import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Point;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.Setting;
import org.geolatte.geom.json.Settings;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

/**
 * A Jackson 2 databind {@link SimpleModule} that registers serializers and
 * deserializers for Geolatte geometries against a Jackson 2 ObjectMapper.
 */
@SuppressWarnings("rawtypes")
public class GeolatteGeomModule extends SimpleModule {

    private final Settings settings = new Settings();

    private final Map<Class, JsonDeserializer> dezers = new HashMap<>();

    private final GeometrySerializer geometrySerializer;
    private final CrsSerializer crsSerializer;

    public GeolatteGeomModule() {
        this(WGS84);
    }

    @SuppressWarnings("unchecked")
    public <P extends Position> GeolatteGeomModule(CoordinateReferenceSystem<P> defaultCrs) {

        super("GeolatteGeomModule", new Version(2, 0, 0, "", "org.geolatte", "geolatte-geojson-jackson2"));

        geometrySerializer = new GeometrySerializer(settings);
        FeatureSerializer featureSerializer = new FeatureSerializer(settings);
        addSerializer(Feature.class, featureSerializer);
        FeatureCollectionSerializer featureCollectionSerializer = new FeatureCollectionSerializer(settings);
        GeometryDeserializer parser = new GeometryDeserializer(defaultCrs, settings);
        addSerializer(Geometry.class, geometrySerializer);
        crsSerializer = new CrsSerializer<>(defaultCrs, settings);
        addSerializer(CoordinateReferenceSystem.class, crsSerializer);
        BoxSerializer boxSerializer = new BoxSerializer<>();
        addSerializer(Box.class, boxSerializer);
        addSerializer(FeatureCollection.class, featureCollectionSerializer);
        dezers.put(Geometry.class, parser);
        dezers.put(Point.class, parser);
        dezers.put(LineString.class, parser);
        dezers.put(Polygon.class, parser);
        dezers.put(MultiPoint.class, parser);
        dezers.put(MultiLineString.class, parser);
        dezers.put(MultiPolygon.class, parser);
        dezers.put(GeometryCollection.class, parser);
        dezers.put(Feature.class, new FeatureDeserializer(defaultCrs, settings));
        dezers.put(FeatureCollection.class, new FeatureCollectionDeserializer(defaultCrs, settings));
        dezers.put(CoordinateReferenceSystem.class, new CrsDeserializer(defaultCrs, settings));

        dezers.forEach(this::addDeserializer);
    }

    public void set(Setting setting, boolean value) {
        settings.override(setting, value);
    }

    public GeometrySerializer getGeometrySerializer() {
        return this.geometrySerializer;
    }

    /**
     * Returns the GeoJson deserializers for the Geometry classes.
     *
     * <p>This method is provided for interoperability reasons.</p>
     */
    public Map<Class, JsonDeserializer> getGeometryDeserializers() {
        return unmodifiableMap(dezers);
    }

    public void copyToModule(SimpleModule other) {
        other.addSerializer(getGeometrySerializer());
        other.addSerializer(this.crsSerializer);
        getGeometryDeserializers().forEach(other::addDeserializer);
    }
}
