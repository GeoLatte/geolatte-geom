package org.geolatte.geom.json.test;

import org.geolatte.geom.AbstractGeometryCollection;
import org.geolatte.geom.GeometryCollection;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.geometrycollection;
import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.Crss.lambert72Z;
import static org.geolatte.geom.json.GeoJsonStrings.emptyGeometryCollection;
import static org.geolatte.geom.json.GeoJsonStrings.geometryCollection;
import static org.geolatte.geom.json.GeoJsonStrings.geometryCollectionWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.geometryCollectionWithCrs3D;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON {@link GeometryCollection} encode/decode behaviour.
 */
public abstract class GeometryCollectionSpec extends AbstractGeoJsonContract {

    // ─── Serialization ──────────────────────────────────────────────────────

    @Test
    public void serializeEmptyGeometryCollection() {
        GeometryCollection<?> geom = new GeometryCollection<>(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(geom);
        assertEquals(emptyGeometryCollection, rec);
    }

    @Test
    public void serializeSimpleGeometryCollection() {
        AbstractGeometryCollection<?, ?> geom = geometrycollection(
                linestring(WGS84, g(1, 1), g(1, 2)),
                point(WGS84, g(5, 6))
        );
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(geom);
        assertEquals(geometryCollection, rec);
    }

    @Test
    public void serializeGeometryCollectionWithCrs() {
        AbstractGeometryCollection<?, ?> geom = geometrycollection(
                linestring(lambert72, c(1, 1), c(1, 2)),
                point(lambert72, c(5, 6))
        );
        String rec = newMapper().writeAsString(geom);
        assertEquals(geometryCollectionWithCrs, rec);
    }

    @Test
    public void serializeGeometryCollectionWithCrs3D() {
        AbstractGeometryCollection<?, ?> geom = geometrycollection(lambert72Z,
                linestring(c(1, 1, 1), c(1, 2, 3)),
                point(c(5, 6, 7))
        );
        String rec = newMapper().writeAsString(geom);
        assertEquals(geometryCollectionWithCrs3D, rec);
    }

    // ─── Deserialization ────────────────────────────────────────────────────

    @Test
    public void deserializeEmptyGeometryCollection() {
        GeometryCollection<?> rec = newMapper().readValue(emptyGeometryCollection, GeometryCollection.class);
        GeometryCollection<?> exp = new GeometryCollection<>(WGS84);
        assertEquals(exp, rec);
    }

    @Test
    public void deserializeSimpleGeometryCollection() {
        GeometryCollection<?> rec = newMapper().readValue(geometryCollection, GeometryCollection.class);
        GeometryCollection<?> expected = geometrycollection(
                linestring(WGS84, g(1, 1), g(1, 2)),
                point(WGS84, g(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeGeometryCollectionWithCrs() {
        GeometryCollection<?> rec = newMapper().readValue(geometryCollectionWithCrs, GeometryCollection.class);
        GeometryCollection<?> expected = geometrycollection(
                linestring(lambert72, c(1, 1), c(1, 2)),
                point(lambert72, c(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeGeometryCollectionWithCrs3D() {
        GeometryCollection<?> rec = newMapper().readValue(geometryCollectionWithCrs3D, GeometryCollection.class);
        GeometryCollection<?> expected = geometrycollection(lambert72Z,
                linestring(c(1, 1, 1), c(1, 2, 3)),
                point(c(5, 6, 7))
        );
        assertEquals(expected, rec);
    }
}
