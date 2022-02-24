package org.geolatte.geom.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.geolatte.geom.Box;
import org.geolatte.geom.Feature;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Created by Karel Maesen, Geovise BVBA on 13/07/2018.
 */
public class GeoJsonFeature<P extends Position, ID> implements Feature<P, ID> {


    final private Geometry<P> geometry;

    final private ID id;

    final private Map<String, Object> properties;

    public GeoJsonFeature(Geometry<P> geometry, ID id, Map<String, Object> properties) {
        this.geometry = geometry;
        this.id = id;
        this.properties = properties;
    }

    @Override
    public Geometry<P> getGeometry() {
        return geometry;
    }

    @Override
    public ID getId() {
        return id;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    @JsonInclude(NON_EMPTY)
    public Box<P> getBbox() {
        return geometry != null ? geometry.getBoundingBox() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoJsonFeature<?, ?> that = (GeoJsonFeature<?, ?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GeoJsonFeature{" +
                "geometry=" + geometry +
                ", id=" + id +
                ", properties=" + properties +
                '}';
    }
}

