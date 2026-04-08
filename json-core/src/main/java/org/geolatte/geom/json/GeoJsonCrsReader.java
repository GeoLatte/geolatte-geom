package org.geolatte.geom.json;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.json.spi.JsonTreeNode;

/**
 * Jackson-free reader for the GeoJSON {@code crs} member.
 *
 * <p>Recognises both "name" and "link" CRS objects, and resolves the resulting EPSG
 * code through {@link CrsRegistry}, falling back to a default CRS when the input
 * lacks a CRS or {@link Setting#IGNORE_CRS} is set.</p>
 */
public final class GeoJsonCrsReader {

    private final CoordinateReferenceSystem<?> defaultCrs;
    private final Settings settings;

    public GeoJsonCrsReader(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.defaultCrs = defaultCrs;
        this.settings = settings;
    }

    public CoordinateReferenceSystem<?> getDefaultCrs() {
        return defaultCrs;
    }

    /**
     * Resolves the CRS from a GeoJSON {@code crs} member node (or returns the default CRS
     * if the input is null/UNDEFINED or {@link Setting#IGNORE_CRS} is set).
     */
    public CoordinateReferenceSystem<?> resolve(JsonTreeNode crsNode) {
        CrsId id = readCrsId(crsNode);
        return id.equals(CrsId.UNDEFINED) || settings.isSet(Setting.IGNORE_CRS) ?
                this.defaultCrs :
                CrsRegistry.getCoordinateReferenceSystemForEPSG(id.getCode(), defaultCrs);
    }

    /**
     * Parses a CRS object node into its {@link CrsId}. Accepts both "name" and "link"
     * CRS objects per the GeoJSON 2008 spec.
     */
    public CrsId readCrsId(JsonTreeNode crs) {
        if (crs == null) return CrsId.UNDEFINED;

        String type = crs.get("type").asText();

        if (type.equalsIgnoreCase("name")) {
            String text = crs.get("properties").get("name").asText();
            return CrsId.parse(text);
        }

        if (type.equalsIgnoreCase("link")) {
            String text = crs.get("properties").get("href").asText();
            String[] components = text.split("/");
            int last = components.length - 1;
            return CrsId.valueOf(components[last - 1], Integer.decode(components[last]));
        }

        throw new GeoJsonException("Can parse only named crs elements");
    }
}
