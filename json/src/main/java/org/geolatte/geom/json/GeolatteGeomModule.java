package org.geolatte.geom.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import org.geolatte.geom.crs.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * A Jackson databind Module for Geolatte Geometries.
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class GeolatteGeomModule extends SimpleModule {


    private final Features features = new Features();



    public GeolatteGeomModule() {
        this(WGS84);
    }

    public <P extends Position> GeolatteGeomModule(CoordinateReferenceSystem<P> defaultCrs) {

        super("GeolatteGeomModule", new Version(1, 0, 0, "", "org.geolatte", "geolatte-json"));

        addSerializer(Geometry.class, new GeometrySerializer<>(new Context(defaultCrs, this.features)));

//        addDeserializer(Geometry.class, new GeometryDeserializer<Geometry>(this, Geometry.class));
        addDeserializer(Point.class, new PointDeserializer<>( new Context(defaultCrs, this.features)));
        addDeserializer(LineString.class, new LineStringDeserializer<>( new Context(defaultCrs, this.features)));
//        addDeserializer(LineString.class, new GeometryDeserializer<LineString>(this, LineString.class));
//        addDeserializer(MultiPoint.class, new GeometryDeserializer<MultiPoint>(this, MultiPoint.class));
//        addDeserializer(MultiLineString.class, new GeometryDeserializer<MultiLineString>(this, MultiLineString.class));
//        addDeserializer(Polygon.class, new GeometryDeserializer<Polygon>(this, Polygon.class));
//        addDeserializer(MultiPolygon.class, new GeometryDeserializer<MultiPolygon>(this, MultiPolygon.class));
//        addDeserializer(GeometryCollection.class, new GeometryDeserializer<GeometryCollection>(this, GeometryCollection.class));
//        addDeserializer(Feature.class, new FeatureDeserializer(this));
//        addDeserializer(FeatureCollection.class, new FeatureCollectionDeserializer(this));

    }

    public void setFeature(Feature feature, boolean value) {
        features.override(feature, value);
    }


}
