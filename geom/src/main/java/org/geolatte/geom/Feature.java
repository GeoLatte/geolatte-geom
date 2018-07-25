package org.geolatte.geom;

import java.util.Map;

/**
 * Models a spatial feature
 *
 * Created by Karel Maesen, Geovise BVBA on 13/07/2018.
 */
public interface Feature< P extends Position, ID> {

    final static String TYPE = "Feature";

    Geometry<P> getGeometry();

    ID getId();

    Map<String, Object> getProperties();

    default String getType(){
        return TYPE;
    }

    default Object getProperty(String propertyName) {
        return getProperties().get(propertyName);
    }
}
