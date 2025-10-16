package org.geolatte.geom.json;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ValueDeserializer;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

/**
 * A Jackson databind Module for Geolatte Geometries.
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
@SuppressWarnings("rawtypes")
public class GeolatteGeomModule extends SimpleModule {

    private final Settings settings = new Settings();

    private final Map<Class, ValueDeserializer> dezers = new HashMap<>();

    private final GeometrySerializer geometrySerializer;
    private final CrsSerializer crsSerializer;

    public GeolatteGeomModule() {
        this(WGS84);
    }

    @SuppressWarnings("unchecked")
    public <P extends Position> GeolatteGeomModule(CoordinateReferenceSystem<P> defaultCrs) {

        super("GeolatteGeomModule", new Version(2, 0, 0, "", "org.geolatte", "geolatte-json"));

        geometrySerializer = new GeometrySerializer(settings);
        FeatureSerializer featureSerializer = new FeatureSerializer(settings);
        addSerializer(Feature.class, featureSerializer);
        FeatureCollectionSerializer featureCollectionSerializer = new FeatureCollectionSerializer(settings);
        GeometryDeserializer parser = new GeometryDeserializer(defaultCrs, settings);
        addSerializer(Geometry.class, geometrySerializer); //use raw to get this compiled
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
     * Return the the GeoJson deserializers for Geometry class
     * <p>
     * This method is provided for interoperability reasons
     */
    public Map<Class, ValueDeserializer> getGeometryDeserializers() {
        return unmodifiableMap(dezers);
    }

    public void copyToModule(SimpleModule other) {
        other.addSerializer(getGeometrySerializer());
        other.addSerializer(this.crsSerializer);
        getGeometryDeserializers().forEach(other::addDeserializer);
    }
}
