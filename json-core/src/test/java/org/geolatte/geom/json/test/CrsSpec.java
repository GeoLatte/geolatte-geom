package org.geolatte.geom.json.test;

import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.GeoJsonStrings.crslambert72;
import static org.geolatte.geom.json.GeoJsonStrings.crslambert72TextWithUrnCrs;
import static org.geolatte.geom.json.GeoJsonStrings.crswgs84;
import static org.geolatte.geom.json.GeoJsonStrings.crswgs84TextWithUrnCrs;
import static org.geolatte.geom.json.GeoJsonStrings.crswgs84WithLink;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithUrnCrs;
import static org.geolatte.geom.json.Setting.SERIALIZE_CRS_AS_URN;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON CRS encode/decode behaviour (URN format,
 * deserialization of "name" and "link" CRS objects).
 */
public abstract class CrsSpec extends AbstractGeoJsonContract {

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
}
