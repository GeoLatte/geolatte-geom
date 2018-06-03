package org.geolatte.geom.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

/**
 * A Jackson databind Module for Geolatte Geometries.
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class GeolatteGeomModule extends SimpleModule {


    private final Settings settings = new Settings();

    private final Map<Class, JsonDeserializer> dezers = new HashMap<>();

    private  GeometrySerializer geometrySerializer;

    public GeolatteGeomModule() {
        this(WGS84);
    }

    @SuppressWarnings("unchecked")
    public <P extends Position> GeolatteGeomModule(CoordinateReferenceSystem<P> defaultCrs) {

        super("GeolatteGeomModule", new Version(1, 0, 0, "", "org.geolatte", "geolatte-json"));


        geometrySerializer = new GeometrySerializer(defaultCrs, settings);

        addSerializer(Geometry.class, geometrySerializer); //use raw to get this compiled
        dezers.put(Geometry.class, new GeometryParser<>( defaultCrs,  settings));
        dezers.put(Point.class, new PointParser<>(defaultCrs,  settings));
        dezers.put(LineString.class, new LineStringParser<>(defaultCrs,  settings));
        dezers.put(Polygon.class, new PolygonParser<>(defaultCrs,  settings));
        dezers.put(MultiPoint.class, new MultiPointParser<>(defaultCrs,  settings));
        dezers.put(MultiLineString.class, new MultiLineStringParser<>(defaultCrs,  settings));
        dezers.put(MultiPolygon.class, new MultiPolygonParser<>(defaultCrs,  settings));
        dezers.put(GeometryCollection.class, new GeometryCollectionParser<>(defaultCrs,  settings));

        dezers.forEach(this::addDeserializer);
    }

    public void set(Setting setting, boolean value) {
        settings.override(setting, value);
    }


    public GeometrySerializer getGeometrySerializer(){
        return this.geometrySerializer;
    }

    /**
     * Return the the GeoJson deserializers for Geometry class
     *
     * This method is provided for interoperability reasons
     */
    public Map<Class, JsonDeserializer> getGeometryDeserializers() {
        return unmodifiableMap(dezers);
    }


    public void copyToModule(SimpleModule other) {
        other.addSerializer(getGeometrySerializer());
        getGeometryDeserializers().forEach(other::addDeserializer);
    }

}
