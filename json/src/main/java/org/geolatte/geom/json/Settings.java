package org.geolatte.geom.json;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
class Settings {

    private final Map<Setting, Boolean> overrides = new HashMap<Setting, Boolean>();

    public boolean isSet(Setting setting) {
        Boolean override = overrides.get(setting);
        return  override == null ? setting.isSetByDefault() : override;
    }


    public void override(Setting setting, boolean value) {
        if(setting.isSetByDefault() != value) {
            overrides.put(setting, value);
        }
    }

}
