package org.geolatte.geom.json.test;

import org.geolatte.geom.C2D;
import org.geolatte.geom.G2D;
import org.geolatte.geom.G2DM;
import org.geolatte.geom.G3D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.Unit;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.gM;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.Crss.lambert72Z;
import static org.geolatte.geom.json.Crss.wgs2DM;
import static org.geolatte.geom.json.Crss.wgs3D;
import static org.geolatte.geom.json.GeoJsonStrings.emptyPointText;
import static org.geolatte.geom.json.GeoJsonStrings.pointText;
import static org.geolatte.geom.json.GeoJsonStrings.pointText2DM;
import static org.geolatte.geom.json.GeoJsonStrings.pointText3D;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithCrs3D;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithUrnCrs;
import static org.geolatte.geom.json.Setting.IGNORE_CRS;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON {@link Point} encode/decode behaviour.
 */
public abstract class PointSpec extends AbstractGeoJsonContract {

    // ─── Serialization ──────────────────────────────────────────────────────

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

    // ─── Deserialization ────────────────────────────────────────────────────

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
}
