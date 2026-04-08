package org.geolatte.geom.json.test;

import org.geolatte.geom.AbstractGeometryCollection;
import org.geolatte.geom.C2D;
import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.G2D;
import org.geolatte.geom.G2DM;
import org.geolatte.geom.G3D;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Point;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.Unit;
import org.geolatte.geom.json.GeoJsonFeature;
import org.geolatte.geom.json.GeoJsonFeatureCollection;
import org.geolatte.geom.json.GeoJsonStrings;
import org.geolatte.geom.json.Setting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.gM;
import static org.geolatte.geom.builder.DSL.geometrycollection;
import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.builder.DSL.multilinestring;
import static org.geolatte.geom.builder.DSL.multipoint;
import static org.geolatte.geom.builder.DSL.multipolygon;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.builder.DSL.ring;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.Crss.lambert72Z;
import static org.geolatte.geom.json.Crss.lambert72ZM;
import static org.geolatte.geom.json.Crss.wgs2DM;
import static org.geolatte.geom.json.Crss.wgs3D;
import static org.geolatte.geom.json.GeoJsonStrings.crslambert72;
import static org.geolatte.geom.json.GeoJsonStrings.crslambert72TextWithUrnCrs;
import static org.geolatte.geom.json.GeoJsonStrings.crswgs84;
import static org.geolatte.geom.json.GeoJsonStrings.crswgs84TextWithUrnCrs;
import static org.geolatte.geom.json.GeoJsonStrings.crswgs84WithLink;
import static org.geolatte.geom.json.GeoJsonStrings.emptyFeatureCollection;
import static org.geolatte.geom.json.GeoJsonStrings.emptyGeometryCollection;
import static org.geolatte.geom.json.GeoJsonStrings.emptyLineString;
import static org.geolatte.geom.json.GeoJsonStrings.emptyMultiLineString;
import static org.geolatte.geom.json.GeoJsonStrings.emptyMultiPoint;
import static org.geolatte.geom.json.GeoJsonStrings.emptyMultiPolygon;
import static org.geolatte.geom.json.GeoJsonStrings.emptyPointText;
import static org.geolatte.geom.json.GeoJsonStrings.emptyPolygon;
import static org.geolatte.geom.json.GeoJsonStrings.feature;
import static org.geolatte.geom.json.GeoJsonStrings.featureCollection;
import static org.geolatte.geom.json.GeoJsonStrings.featureCollectionNoBbox;
import static org.geolatte.geom.json.GeoJsonStrings.featureEmptyPolygon;
import static org.geolatte.geom.json.GeoJsonStrings.featureIntId;
import static org.geolatte.geom.json.GeoJsonStrings.featureNullGeometry;
import static org.geolatte.geom.json.GeoJsonStrings.featureWithBBox;
import static org.geolatte.geom.json.GeoJsonStrings.featureWithLineString;
import static org.geolatte.geom.json.GeoJsonStrings.geometryCollection;
import static org.geolatte.geom.json.GeoJsonStrings.geometryCollectionWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.geometryCollectionWithCrs3D;
import static org.geolatte.geom.json.GeoJsonStrings.lineString2DM;
import static org.geolatte.geom.json.GeoJsonStrings.lineStringWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.multiLineString;
import static org.geolatte.geom.json.GeoJsonStrings.multiLineStringWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.multiPoint;
import static org.geolatte.geom.json.GeoJsonStrings.multiPointWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.multiPolygon;
import static org.geolatte.geom.json.GeoJsonStrings.multiPolygonWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.pointText;
import static org.geolatte.geom.json.GeoJsonStrings.pointText2DM;
import static org.geolatte.geom.json.GeoJsonStrings.pointText3D;
import static org.geolatte.geom.json.GeoJsonStrings.pointText3DM;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithCrs34D;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithCrs3D;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithUrnCrs;
import static org.geolatte.geom.json.GeoJsonStrings.polygonWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.simpleLineString;
import static org.geolatte.geom.json.Setting.FORCE_DEFAULT_CRS_DIMENSION;
import static org.geolatte.geom.json.Setting.IGNORE_CRS;
import static org.geolatte.geom.json.Setting.SERIALIZE_CRS_AS_URN;
import static org.geolatte.geom.json.Setting.SERIALIZE_FEATURE_BBOX;
import static org.geolatte.geom.json.Setting.SERIALIZE_FEATURE_COLLECTION_BBOX;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Behavioural contract for the GeoJSON encode/decode pipeline.
 *
 * <p>This abstract test class is shared (via the {@code geolatte-geojson-core} test-jar)
 * by both the Jackson 2 and Jackson 3 adapter modules. Each adapter provides a concrete
 * subclass that implements {@link #newMapper(CoordinateReferenceSystem, Map)} by
 * constructing a native {@code ObjectMapper} with the corresponding
 * {@code GeolatteGeomModule} installed.</p>
 *
 * <p>Both adapters run the full suite, so any divergence in behaviour between the
 * Jackson 2 and Jackson 3 stacks will surface as a failed test in one of them.</p>
 */
public abstract class AbstractGeoJsonContract {

    // ─── Factory methods ────────────────────────────────────────────────────

    /**
     * The single factory method that adapter subclasses must implement. All other
     * {@code newMapper} overloads delegate to this one.
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

    // ─── Point serialization ────────────────────────────────────────────────

    @Test
    public void serializeEmptyPoint() {
        Point<?> pnt = new Point(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(pnt);
        assertEquals(emptyPointText, rec);
    }

    @Test
    public void serializeSimplePoint() {
        Point<?> pnt = point(WGS84, g(1, 2));
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(pnt);
        assertEquals(pointText, rec);
    }

    @Test
    public void serializeSimplePoint3D() {
        Point<?> pnt = point(wgs3D, g(1, 2, 3));
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(pnt);
        assertEquals(pointText3D, rec);
    }

    @Test
    public void serializePointWithCrs() {
        Point<?> pnt = point(lambert72Z, c(1, 2, 3));
        String rec = newMapper().writeAsString(pnt);
        assertEquals(pointTextWithCrs3D, rec);
    }

    // ─── Point deserialization ──────────────────────────────────────────────

    @Test
    public void deserializePointText() {
        Point<?> pnt = newMapper().readValue(pointText, Point.class);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointText3D() {
        Point<?> pnt = newMapper().readValue(pointText3D, Point.class);
        Point<G3D> expected = point(wgs3D, g(1, 2, 3));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointTextWithSpecificCRS() {
        Point<?> pnt = newMapper(WGS84).readValue(pointText, Point.class);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointTextWithWildCard() {
        Point<?> pnt = newMapper().readValue(pointText, Point.class);
        Point<?> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointTextWithCrs() {
        Point<?> pnt = newMapper().readValue(pointTextWithCrs, Point.class);
        Point<C2D> expected = point(lambert72, c(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointTextWithCrs3D() {
        Point<?> pnt = newMapper().readValue(pointTextWithCrs3D, Point.class);
        Point<?> expected = point(lambert72Z, c(1, 2, 3));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointTextWithNoLimitToCrs() {
        Point<?> pnt = newMapper(wgs3D).readValue(pointText, Point.class);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointTextWithLimitToCrs() {
        Point<?> pnt = newMapper(wgs3D, IGNORE_CRS, true).readValue(pointText, Point.class);
        Point<G3D> expected = point(wgs3D, g(1, 2, 0));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointTextWithLimitToCrsReduce() {
        Point<?> pnt = newMapper(WGS84, IGNORE_CRS, true).readValue(pointText3D, Point.class);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointTextWithLimitToCrs2DM() {
        CoordinateReferenceSystem<G2DM> crs = WGS84.addLinearSystem(Unit.METER, G2DM.class);
        Point<?> pnt = newMapper(crs, IGNORE_CRS, true).readValue(pointText2DM, Point.class);
        Point<G2DM> expected = point(crs, gM(1, 2, 4));
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializeEmptyPoint() {
        Point<?> pnt = newMapper().readValue(emptyPointText, Point.class);
        Point<?> expected = new Point<>(WGS84);
        assertEquals(expected, pnt);
    }

    @Test
    public void deserializePointTextWithCrsURN() {
        Point<?> pnt = newMapper().readValue(pointTextWithUrnCrs, Point.class);
        Point<C2D> expected = point(lambert72, c(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void force3DMTo2DMPoint() {
        Point<?> pnt = newMapper(wgs2DM, IGNORE_CRS, true).readValue(pointTextWithCrs3D, Point.class);
        Point<?> expected = point(wgs2DM, gM(1, 2, 3.0));
        assertEquals(expected, pnt);
    }

    // ─── LineString serialization ───────────────────────────────────────────

    @Test
    public void serializeEmptyLineString() {
        LineString<?> ln = new LineString(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(ln);
        assertEquals(emptyLineString, rec);
    }

    @Test
    public void serializeSimpleLineString() {
        LineString<?> ln = linestring(WGS84, g(1, 2), g(3, 4));
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(ln);
        assertEquals(simpleLineString, rec);
    }

    @Test
    public void serializeLineStringWithCrs() {
        LineString<?> ln = linestring(lambert72, c(1, 2), c(3, 4));
        String rec = newMapper().writeAsString(ln);
        assertEquals(lineStringWithCrs, rec);
    }

    // ─── LineString deserialization ─────────────────────────────────────────

    @Test
    public void deserializeEmptyLineString() {
        LineString<?> rec = newMapper().readValue(emptyLineString, LineString.class);
        LineString<?> expected = new LineString(WGS84);
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeSimpleLineString() {
        LineString<?> rec = newMapper().readValue(simpleLineString, LineString.class);
        LineString<?> expected = linestring(WGS84, g(1, 2), g(3, 4));
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeLineStringWithCrs() {
        LineString<?> rec = newMapper().readValue(lineStringWithCrs, LineString.class);
        LineString<?> expected = linestring(lambert72, c(1, 2), c(3, 4));
        assertEquals(expected, rec);
    }

    @Test
    public void force3DMTo2DMLineString() {
        LineString<?> rec = newMapper(wgs2DM, FORCE_DEFAULT_CRS_DIMENSION, true)
                .readValue(lineString2DM, LineString.class);
        LineString<?> expected = linestring(wgs2DM, gM(1, 2, 3), gM(10, 20, 30));
        assertEquals(expected, rec);
    }

    // ─── Polygon serialization ──────────────────────────────────────────────

    @Test
    public void serializeEmptyPolygon() {
        Polygon<?> p = Geometries.mkEmptyPolygon(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(p);
        assertEquals(emptyPolygon, rec);
    }

    @Test
    public void serializeSimplePolygon() {
        Polygon<?> p = polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1)));
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(p);
        assertEquals(GeoJsonStrings.polygon, rec);
    }

    @Test
    public void serializePolygonWithCrs() {
        Polygon<?> p = polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1)));
        String rec = newMapper().writeAsString(p);
        assertEquals(polygonWithCrs, rec);
    }

    // ─── Polygon deserialization ────────────────────────────────────────────

    @Test
    public void deserializeEmptyPolygon() {
        Polygon<?> rec = newMapper().readValue(emptyPolygon, Polygon.class);
        Polygon<?> exp = new Polygon<>(WGS84);
        assertEquals(exp, rec);
    }

    @Test
    public void deserializeSimplePolygon() {
        Polygon<?> rec = newMapper().readValue(GeoJsonStrings.polygon, Polygon.class);
        Polygon<?> expected = polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1)));
        assertEquals(expected, rec);
    }

    @Test
    public void deserializePolygonWithCrs() {
        Polygon<?> rec = newMapper().readValue(polygonWithCrs, Polygon.class);
        Polygon<?> expected = polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1)));
        assertEquals(expected, rec);
    }

    // ─── MultiPoint serialization ───────────────────────────────────────────

    @Test
    public void serializeEmptyMultiPoint() {
        MultiPoint<?> mp = multipoint(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mp);
        assertEquals(emptyMultiPoint, rec);
    }

    @Test
    public void serializeSimpleMultiPoint() {
        MultiPoint<?> mp = multipoint(WGS84, g(1, 2), g(3, 4));
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mp);
        assertEquals(multiPoint, rec);
    }

    @Test
    public void serializeMultiPointWithCrs() {
        MultiPoint<?> mp = multipoint(lambert72, c(1, 2), c(3, 4));
        String rec = newMapper().writeAsString(mp);
        assertEquals(multiPointWithCrs, rec);
    }

    // ─── MultiPoint deserialization ─────────────────────────────────────────

    @Test
    public void deserializeEmptyMultiPoint() {
        MultiPoint<?> rec = newMapper().readValue(emptyMultiPoint, MultiPoint.class);
        MultiPoint<?> expected = new MultiPoint(WGS84);
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeSimpleMultiPoint() {
        MultiPoint<?> rec = newMapper().readValue(multiPoint, MultiPoint.class);
        MultiPoint<?> expected = multipoint(WGS84, g(1, 2), g(3, 4));
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeMultiPointWithCrs() {
        MultiPoint<?> rec = newMapper().readValue(multiPointWithCrs, MultiPoint.class);
        MultiPoint<?> expected = multipoint(lambert72, c(1, 2), c(3, 4));
        assertEquals(expected, rec);
    }

    // ─── MultiLineString serialization ──────────────────────────────────────

    @Test
    public void serializeEmptyMultiLineString() {
        MultiLineString<?> mls = new MultiLineString(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mls);
        assertEquals(emptyMultiLineString, rec);
    }

    @Test
    public void serializeSimpleMultiLineString() {
        MultiLineString<?> mls = multilinestring(
                linestring(WGS84, g(1, 1), g(1, 2)),
                linestring(WGS84, g(3, 4), g(5, 6))
        );
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mls);
        assertEquals(multiLineString, rec);
    }

    @Test
    public void serializeMultiLineStringWithCrs() {
        MultiLineString<?> mls = multilinestring(
                linestring(lambert72, c(1, 1), c(1, 2)),
                linestring(lambert72, c(3, 4), c(5, 6))
        );
        String rec = newMapper().writeAsString(mls);
        assertEquals(multiLineStringWithCrs, rec);
    }

    // ─── MultiLineString deserialization ────────────────────────────────────

    @Test
    public void deserializeEmptyMultiLineString() {
        MultiLineString<?> rec = newMapper().readValue(emptyMultiLineString, MultiLineString.class);
        MultiLineString<?> expected = new MultiLineString<>(WGS84);
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeSimpleMultiLineString() {
        MultiLineString<?> rec = newMapper().readValue(multiLineString, MultiLineString.class);
        MultiLineString<?> expected = multilinestring(
                linestring(WGS84, g(1, 1), g(1, 2)),
                linestring(WGS84, g(3, 4), g(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeMultiLineStringWithCrs() {
        MultiLineString<?> rec = newMapper().readValue(multiLineStringWithCrs, MultiLineString.class);
        MultiLineString<?> expected = multilinestring(
                linestring(lambert72, c(1, 1), c(1, 2)),
                linestring(lambert72, c(3, 4), c(5, 6))
        );
        assertEquals(expected, rec);
    }

    // ─── MultiPolygon serialization ─────────────────────────────────────────

    @Test
    public void serializeEmptyMultiPolygon() {
        MultiPolygon<?> mp = multipolygon(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mp);
        assertEquals(emptyMultiPolygon, rec);
    }

    @Test
    public void serializeSimpleMultiPolygon() {
        MultiPolygon<?> mp = multipolygon(
                polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1))),
                polygon(WGS84, ring(g(3, 3), g(3, 5), g(5, 5), g(5, 3), g(3, 3)))
        );
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mp);
        assertEquals(multiPolygon, rec);
    }

    @Test
    public void serializeMultiPolygonWithCrs() {
        MultiPolygon<?> mp = multipolygon(
                polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1))),
                polygon(lambert72, ring(c(3, 3), c(3, 5), c(5, 5), c(5, 3), c(3, 3)))
        );
        String rec = newMapper().writeAsString(mp);
        assertEquals(multiPolygonWithCrs, rec);
    }

    // ─── MultiPolygon deserialization ───────────────────────────────────────

    @Test
    public void deserializeEmptyMultiPolygon() {
        MultiPolygon<?> rec = newMapper().readValue(emptyMultiPolygon, MultiPolygon.class);
        MultiPolygon<?> exp = new MultiPolygon(WGS84);
        assertEquals(exp, rec);
    }

    @Test
    public void deserializeSimpleMultiPolygon() {
        MultiPolygon<?> rec = newMapper().readValue(multiPolygon, MultiPolygon.class);
        MultiPolygon<?> expected = multipolygon(
                polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1))),
                polygon(WGS84, ring(g(3, 3), g(3, 5), g(5, 5), g(5, 3), g(3, 3)))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeMultiPolygonWithCrs() {
        MultiPolygon<?> rec = newMapper().readValue(multiPolygonWithCrs, MultiPolygon.class);
        MultiPolygon<?> expected = multipolygon(
                polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1))),
                polygon(lambert72, ring(c(3, 3), c(3, 5), c(5, 5), c(5, 3), c(3, 3)))
        );
        assertEquals(expected, rec);
    }

    // ─── GeometryCollection serialization ───────────────────────────────────

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

    // ─── GeometryCollection deserialization ─────────────────────────────────

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

    // ─── Generic Geometry deserialization ───────────────────────────────────

    @Test
    public void deserializeMultiLineStringAsGeometry() {
        Geometry<?> rec = newMapper().readValue(multiLineString, Geometry.class);
        Geometry<?> expected = multilinestring(
                linestring(WGS84, g(1, 1), g(1, 2)),
                linestring(WGS84, g(3, 4), g(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void deserializePointAsGeometry() {
        Geometry<?> rec = newMapper().readValue(pointTextWithCrs34D, Geometry.class);
        Geometry<?> expected = point(lambert72ZM, c(1, 2, 3, 4));
        assertEquals(expected, rec);
    }

    // ─── Feature serialization ──────────────────────────────────────────────

    @Test
    public void serializeFeature() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(point(WGS84, g(1, 2)), "1", map);
        String rec = newMapper().writeAsString(f);
        assertEquals(feature, rec);
    }

    @Test
    public void serializeFeatureWithBBox() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(point(WGS84, g(1, 2)), "1", map);
        String rec = newMapper(SERIALIZE_FEATURE_BBOX, true).writeAsString(f);
        assertEquals(featureWithBBox, rec);
    }

    @Test
    public void serializeLineStringFeature() {
        Map<Setting, Boolean> settingsMap = new HashMap<>();
        settingsMap.put(SUPPRESS_CRS_SERIALIZATION, true);
        settingsMap.put(SERIALIZE_FEATURE_BBOX, true);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(linestring(WGS84, g(1, 2), g(3, 4)), "1", map);
        String rec = newMapper(settingsMap).writeAsString(f);
        assertEquals(featureWithLineString, rec);
    }

    @Test
    public void serializeFeatureWithNullGeometry() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(null, "1", map);
        String rec = newMapper().writeAsString(f);
        assertEquals(featureNullGeometry, rec);
    }

    @Test
    public void serializeFeatureWithEmptyPolygonGeometry() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(Geometries.mkEmptyPolygon(WGS84), "1", map);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(f);
        assertEquals(featureEmptyPolygon, rec);
    }

    // ─── Feature deserialization ────────────────────────────────────────────

    @Test
    public void deserializeFeature() {
        Feature<?, ?> rec = newMapper().readValue(feature, Feature.class);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> expected = new GeoJsonFeature<>(point(WGS84, g(1, 2)), "1", map);
        assertEquals(expected, rec);
        assertEquals(expected.getId(), rec.getId());
        assertEquals(expected.getType(), rec.getType());
        assertEquals(expected.getGeometry(), rec.getGeometry());
        assertEquals(expected.getProperties(), rec.getProperties());
    }

    @Test
    public void deserializeFeatureWithIntId() {
        Feature<?, ?> rec = newMapper().readValue(featureIntId, Feature.class);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> expected = new GeoJsonFeature<>(point(WGS84, g(1, 2)), 1L, map);
        assertEquals(expected, rec);
        assertEquals(expected.getId(), rec.getId());
        assertEquals(expected.getType(), rec.getType());
        assertEquals(expected.getGeometry(), rec.getGeometry());
        assertEquals(expected.getProperties(), rec.getProperties());
    }

    @Test
    public void deserializeFeatureWithNullGeometry() {
        Feature<?, ?> rec = newMapper().readValue(featureNullGeometry, Feature.class);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> expected = new GeoJsonFeature<>(null, "1", map);
        assertEquals(expected, rec);
        assertEquals(expected.getId(), rec.getId());
        assertEquals(expected.getType(), rec.getType());
        assertEquals(expected.getGeometry(), rec.getGeometry());
        assertEquals(expected.getProperties(), rec.getProperties());
    }

    // ─── FeatureCollection serialization ────────────────────────────────────

    @Test
    public void serializeFeatureCollection() {
        FeatureCollection<?, ?> fc = mkExampleFeatureCollection();
        String rec = newMapper().writeAsString(fc);
        assertEquals(featureCollectionNoBbox, rec);
    }

    @Test
    public void serializeEmptyFeatureCollection() {
        FeatureCollection<?, ?> fc = new GeoJsonFeatureCollection<>(new ArrayList<>());
        String rec = newMapper().writeAsString(fc);
        assertEquals(emptyFeatureCollection, rec);
    }

    @Test
    public void serializeFeatureCollectionWithBbox() {
        String rec = newMapper(SERIALIZE_FEATURE_COLLECTION_BBOX, true)
                .writeAsString(mkExampleFeatureCollection());
        assertEquals(featureCollection, rec);
    }

    private static FeatureCollection<?, ?> mkExampleFeatureCollection() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("prop0", "value0");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("prop1", 0.0d);
        map2.put("prop0", "value0");
        java.util.List<Feature<G2D, String>> features = new ArrayList<>();
        features.add(new GeoJsonFeature<>(point(WGS84, g(102, 0.5)), "1", map1));
        features.add(new GeoJsonFeature<>(linestring(WGS84, g(102, 0), g(103, 1),
                g(104, 0), g(105, 1)), "2", map2));
        return new GeoJsonFeatureCollection<>(features);
    }

    // ─── FeatureCollection deserialization ──────────────────────────────────

    @Test
    public void deserializeFeatureCollection() {
        FeatureCollection<?, ?> rec = newMapper().readValue(featureCollection, FeatureCollection.class);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("prop0", "value0");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("prop1", 0.0d);
        map2.put("prop0", "value0");
        java.util.List<Feature<G2D, String>> features = new ArrayList<>();
        features.add(new GeoJsonFeature<>(point(WGS84, g(102, 0.5)), "1", map1));
        features.add(new GeoJsonFeature<>(linestring(WGS84, g(102, 0), g(103, 1),
                g(104, 0), g(105, 1)), "2", map2));
        FeatureCollection<?, ?> expected = new GeoJsonFeatureCollection<>(features);
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeEmptyFeatureCollection() {
        FeatureCollection<?, ?> rec = newMapper().readValue(emptyFeatureCollection, FeatureCollection.class);
        FeatureCollection<?, ?> expected = new GeoJsonFeatureCollection<>(new ArrayList<>());
        assertEquals(expected, rec);
    }

    // ─── CRS support ────────────────────────────────────────────────────────

    @Test
    public void serializeCrsInUrnFormat() {
        MapperLike mapper = newMapper(SERIALIZE_CRS_AS_URN, true);
        assertEquals(crswgs84TextWithUrnCrs, mapper.writeAsString(WGS84));
        assertEquals(crslambert72TextWithUrnCrs, mapper.writeAsString(lambert72));
    }

    @Test
    public void serializeCrsDefault() {
        MapperLike mapper = newMapper();
        assertEquals(crswgs84, mapper.writeAsString(WGS84));
        assertEquals(crslambert72, mapper.writeAsString(lambert72));
    }

    @Test
    public void deserializeCrs() {
        MapperLike mapper = newMapper();
        CoordinateReferenceSystem<?> wgs84rec = mapper.readValue(crswgs84, CoordinateReferenceSystem.class);
        assertEquals(WGS84, wgs84rec);

        CoordinateReferenceSystem<?> lambert72rec = mapper.readValue(crslambert72, CoordinateReferenceSystem.class);
        assertEquals(lambert72, lambert72rec);
    }

    @Test
    public void deserializeCrsLinkType() {
        CoordinateReferenceSystem<?> received = newMapper().readValue(crswgs84WithLink, CoordinateReferenceSystem.class);
        assertEquals(WGS84, received);
    }

    @Test
    public void serializePointWithCrsInUrnFormat() {
        Point<?> pnt = point(lambert72, c(1, 2));
        assertEquals(pointTextWithUrnCrs,
                newMapper(SERIALIZE_CRS_AS_URN, true).writeAsString(pnt));
    }

    // ─── Settings: IGNORE_CRS / FORCE_DEFAULT_CRS_DIMENSION ─────────────────

    @Test
    public void ignoreCrsDeserializesAgainstDefault() {
        Point<?> pnt = point(WGS84, g(1, 2));
        assertEquals(pnt,
                newMapper(IGNORE_CRS, true).readValue(pointTextWithUrnCrs, Geometry.class));
    }

    @Test
    public void forceCrsToDefaultDeserializesAgainstDefault() {
        Point<?> pnt = point(WGS84, g(1, 2));
        assertEquals(pnt,
                newMapper(FORCE_DEFAULT_CRS_DIMENSION, true).readValue(pointTextWithUrnCrs, Geometry.class));
    }

    @Test
    public void forceCrsToDefault3DM() {
        Point<G2D> pnt = point(WGS84, g(1, 2));
        assertEquals(pnt,
                newMapper(FORCE_DEFAULT_CRS_DIMENSION, true).readValue(pointText3DM, Geometry.class));
    }

    // ─── CRS dynamic registration ───────────────────────────────────────────

    /**
     * Verifies that an extended CRS is created once and reused for subsequent
     * deserializations from the same mapper, by checking instance identity.
     */
    @Test
    public void crsDynamicRegistration() {
        MapperLike mapper = newMapper();
        Point<?> pnt1 = mapper.readValue(pointText3D, Point.class);
        Point<?> pnt2 = mapper.readValue(pointText3D, Point.class);
        assertSame(pnt1.getCoordinateReferenceSystem(), pnt2.getCoordinateReferenceSystem());
    }

    // ─── Round-trip invariants ──────────────────────────────────────────────

    @Test
    public void coordinateDimensionInvariant() {
        Point<G2D> geom = point(WGS84, g(1, 2));
        MapperLike mapper = newMapper();
        assertEquals(geom.getCoordinateDimension(),
                mapper.readValue(mapper.writeAsString(geom), Geometry.class).getCoordinateDimension());
    }
}
