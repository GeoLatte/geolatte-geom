/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Esperantolaan 4 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.geojson;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.codehaus.jackson.map.ObjectMapper;
import org.geolatte.geom.geojson.to.GeoJsonTo;
import org.geolatte.geom.jts.JTS;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the entire sequence from JTS -> Geolatte Geom -> To -> Json string
 * Although the first part is not strictly necessary in this testsuite, i have included it in the testscope since
 * the JTS -> geojson sequence is often used by users of the library
 * The actual serialization from to to string is done by a standard json serializer (jackson)
 *
 * @author Yves Vandewoude
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
public class JsonSerializationTest {

    private static GeometryFactory geomFactory;
    private static GeoJsonToFactory factory;
    private static ObjectMapper mapper;

    @BeforeClass
    public static void setupSuite() {
        geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 900913);
        factory = new GeoJsonToFactory();
        mapper = new ObjectMapper();
    }

    @Test
    public void testPointSerialization() throws IOException {
        Point p = new Point(new CoordinateArraySequence(new Coordinate[]{new Coordinate(2.0, 3.0)}), geomFactory);
        GeoJsonTo to = factory.toTo(JTS.from(p));
        String geoJsonOutput = mapper.writeValueAsString(to);

        try {
            Map map = mapper.readValue(geoJsonOutput, HashMap.class);
            Assert.assertEquals("Point", map.get("type"));
            Assert.assertEquals("EPSG:900913", getFromMap("crs.properties.name", map));
            Assert.assertEquals("name", getFromMap("crs.type", map));
            List<Double> coords = (List<Double>) map.get("coordinates");
            Assert.assertNotNull(coords);
            Assert.assertEquals(2.0, coords.get(0), 0.00001);
            Assert.assertEquals(3.0, coords.get(1), 0.00001);

        } catch (Exception ignored) {
            Assert.fail("No exception expected");
        }
    }

    /**
     * Test serialization of a linestring.
     *
     * @throws java.io.IOException If an unexpected exception is thrown, this will result in a testfailure
     */
    @Test
    public void testLineStringSerializer() throws IOException {
        LineString l = new LineString(new CoordinateArraySequence(new Coordinate[]{new Coordinate(2.0, 3.0), new Coordinate(3.0, 4.0), new Coordinate(2.5, 5.0)}), geomFactory);
        GeoJsonTo to = factory.toTo(JTS.from(l));
        String output = mapper.writeValueAsString(to);
        HashMap map = mapper.readValue(output, HashMap.class);
        Assert.assertEquals("LineString", map.get("type"));
        Assert.assertEquals("EPSG:900913", getFromMap("crs.properties.name", map));
        Assert.assertEquals("name", getFromMap("crs.type", map));
        List<List<Double>> coords = (List<List<Double>>) map.get("coordinates");
        Assert.assertNotNull(coords);
        Assert.assertEquals(2.0, coords.get(0).get(0), 0.00001);
        Assert.assertEquals(3.0, coords.get(0).get(1), 0.00001);
        Assert.assertEquals(3.0, coords.get(1).get(0), 0.00001);
        Assert.assertEquals(4.0, coords.get(1).get(1), 0.00001);
        Assert.assertEquals(2.5, coords.get(2).get(0), 0.00001);
        Assert.assertEquals(5.0, coords.get(2).get(1), 0.00001);
        List<Double> bbox = (List<Double>) map.get("bbox");
        Assert.assertNotNull(coords);
        Assert.assertEquals(2.0, bbox.get(0), 0.00001);
        Assert.assertEquals(3.0, bbox.get(1), 0.00001);
        Assert.assertEquals(3.0, bbox.get(2), 0.00001);
        Assert.assertEquals(5.0, bbox.get(3), 0.00001);
    }

    /**
     * Tests serialization of a multipoint
     *
     * @throws java.io.IOException If an unexpected exception is thrown. This will result in a testfailure
     */
    @Test
    public void testMultiPointSerializer() throws IOException {
        Point p = new Point(new CoordinateArraySequence(new Coordinate[]{new Coordinate(2.0, 3.0)}), geomFactory);
        Point p2 = new Point(new CoordinateArraySequence(new Coordinate[]{new Coordinate(3.0, 4.0)}), geomFactory);
        Point p3 = new Point(new CoordinateArraySequence(new Coordinate[]{new Coordinate(2.5, 5.0)}), geomFactory);
        MultiPoint l = new MultiPoint(new Point[]{p, p2, p3}, geomFactory);
        String output = mapper.writeValueAsString(factory.toTo(JTS.from(l)));
        HashMap map = mapper.readValue(output, HashMap.class);
        Assert.assertEquals("MultiPoint", map.get("type"));
        Assert.assertEquals("EPSG:900913", getFromMap("crs.properties.name", map));
        Assert.assertEquals("name", getFromMap("crs.type", map));
        List<List<Double>> coords = (List<List<Double>>) map.get("coordinates");
        Assert.assertNotNull(coords);
        Assert.assertEquals(2.0, coords.get(0).get(0), 0.00001);
        Assert.assertEquals(3.0, coords.get(0).get(1), 0.00001);
        Assert.assertEquals(3.0, coords.get(1).get(0), 0.00001);
        Assert.assertEquals(4.0, coords.get(1).get(1), 0.00001);
        Assert.assertEquals(2.5, coords.get(2).get(0), 0.00001);
        Assert.assertEquals(5.0, coords.get(2).get(1), 0.00001);
        List<Double> bbox = (List<Double>) map.get("bbox");
        Assert.assertNotNull(coords);
        Assert.assertEquals(2.0, bbox.get(0), 0.00001);
        Assert.assertEquals(3.0, bbox.get(1), 0.00001);
        Assert.assertEquals(3.0, bbox.get(2), 0.00001);
        Assert.assertEquals(5.0, bbox.get(3), 0.00001);
    }

    /**
     * Test serialization of a multiline
     *
     * @throws java.io.IOException If an unexpected exception is thrown. This will result in a testfailure.
     */
    @Test
    public void testMultiLineStringSerializer() throws IOException {
        LineString l = new LineString(new CoordinateArraySequence(new Coordinate[]{new Coordinate(2.0, 3.0), new Coordinate(3.0, 4.0)}), geomFactory);
        LineString l2 = new LineString(new CoordinateArraySequence(new Coordinate[]{new Coordinate(12.0, 13.0), new Coordinate(13.0, 14.0)}), geomFactory);
        LineString l3 = new LineString(new CoordinateArraySequence(new Coordinate[]{new Coordinate(24.0, 5.0), new Coordinate(19.0, 3.0)}), geomFactory);
        MultiLineString mls = new MultiLineString(new LineString[]{l, l2, l3}, geomFactory);
        String output = mapper.writeValueAsString(factory.toTo(JTS.from(mls)));
        HashMap map = mapper.readValue(output, HashMap.class);
        Assert.assertEquals("MultiLineString", map.get("type"));
        Assert.assertEquals("EPSG:900913", getFromMap("crs.properties.name", map));
        Assert.assertEquals("name", getFromMap("crs.type", map));
        List<List<List<Double>>> coords = (List<List<List<Double>>>) map.get("coordinates");
        Assert.assertNotNull(coords);
        Assert.assertEquals(2.0, coords.get(0).get(0).get(0), 0.00001);
        Assert.assertEquals(3.0, coords.get(0).get(0).get(1), 0.00001);
        Assert.assertEquals(3.0, coords.get(0).get(1).get(0), 0.00001);
        Assert.assertEquals(4.0, coords.get(0).get(1).get(1), 0.00001);

        Assert.assertEquals(12.0, coords.get(1).get(0).get(0), 0.00001);
        Assert.assertEquals(13.0, coords.get(1).get(0).get(1), 0.00001);
        Assert.assertEquals(13.0, coords.get(1).get(1).get(0), 0.00001);
        Assert.assertEquals(14.0, coords.get(1).get(1).get(1), 0.00001);

        Assert.assertEquals(24.0, coords.get(2).get(0).get(0), 0.00001);
        Assert.assertEquals(5.0, coords.get(2).get(0).get(1), 0.00001);
        Assert.assertEquals(19.0, coords.get(2).get(1).get(0), 0.00001);
        Assert.assertEquals(3.0, coords.get(2).get(1).get(1), 0.00001);
        List<Double> bbox = (List<Double>) map.get("bbox");
        // lowx, lowy, highx, highy
        Assert.assertNotNull(coords);
        Assert.assertEquals(2.0, bbox.get(0), 0.00001);
        Assert.assertEquals(3.0, bbox.get(1), 0.00001);
        Assert.assertEquals(24.0, bbox.get(2), 0.00001);
        Assert.assertEquals(14.0, bbox.get(3), 0.00001);
    }

    /**
     * Serialization of a polygon
     */
    /**
     * Test serialization of a multiline
     *
     * @throws java.io.IOException If any unexpected exception is thrown. this will result in a testfailure
     */
    @Test
    public void testPolygonSerializer() throws IOException {
        LinearRing l = new LinearRing(new CoordinateArraySequence(new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0), new Coordinate(0.0, 1.0), new Coordinate(0.0, 0.0)}), geomFactory);
        LinearRing l2 = new LinearRing(new CoordinateArraySequence(new Coordinate[]{new Coordinate(0.1, 0.1), new Coordinate(0.25, 0.1),
                new Coordinate(0.25, 0.25), new Coordinate(0.1, 0.25), new Coordinate(0.1, 0.1)}), geomFactory);
        LinearRing l3 = new LinearRing(new CoordinateArraySequence(new Coordinate[]{new Coordinate(0.7, 0.7), new Coordinate(0.95, 0.7),
                new Coordinate(0.95, 0.95), new Coordinate(0.7, 0.95), new Coordinate(0.7, 0.7)}), geomFactory);
        Polygon mls = new Polygon(l, new LinearRing[]{l2, l3}, geomFactory);
        org.geolatte.geom.Geometry geom = JTS.from(mls);
        String output = mapper.writeValueAsString(factory.toTo(geom));

        HashMap map = mapper.readValue(output, HashMap.class);
        Assert.assertEquals("Polygon", map.get("type"));
        Assert.assertEquals("EPSG:900913", getFromMap("crs.properties.name", map));
        Assert.assertEquals("name", getFromMap("crs.type", map));
        List<List<List<Double>>> coords = (List<List<List<Double>>>) map.get("coordinates");
        Assert.assertNotNull(coords);
        // First array must be the exterior ring! Three rings in total, order not specified
        Assert.assertEquals(0.0, coords.get(0).get(0).get(0), 0.00001);
        Assert.assertEquals(0.0, coords.get(0).get(0).get(1), 0.00001);
        Assert.assertEquals(1.0, coords.get(0).get(1).get(0), 0.00001);
        Assert.assertEquals(0.0, coords.get(0).get(1).get(1), 0.00001);

        Assert.assertEquals(3, coords.size());
        Assert.assertEquals(5, coords.get(1).size());
        Assert.assertEquals(5, coords.get(2).size());

        List<Double> bbox = (List<Double>) map.get("bbox");
        // lowx, lowy, highx, highy
        Assert.assertNotNull(bbox);
        Assert.assertEquals(0.0, bbox.get(0), 0.00001);
        Assert.assertEquals(0.0, bbox.get(1), 0.00001);
        Assert.assertEquals(1.0, bbox.get(2), 0.00001);
        Assert.assertEquals(1.0, bbox.get(3), 0.00001);
    }

    /**
     * Serialization of a multipolygon
     *
     * @throws IOException if an unexpected exception is thrown, this will result in a testfailure
     */
    @Test
    public void testMultiPolygonSerializer() throws IOException {
        LinearRing l = new LinearRing(new CoordinateArraySequence(new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0), new Coordinate(0.0, 1.0), new Coordinate(0.0, 0.0)}), geomFactory);
        LinearRing l2 = new LinearRing(new CoordinateArraySequence(new Coordinate[]{new Coordinate(0.1, 0.1), new Coordinate(0.25, 0.1),
                new Coordinate(0.25, 0.25), new Coordinate(0.1, 0.25), new Coordinate(0.1, 0.1)}), geomFactory);
        LinearRing l3 = new LinearRing(new CoordinateArraySequence(new Coordinate[]{new Coordinate(0.7, 0.7), new Coordinate(0.95, 0.7),
                new Coordinate(0.95, 0.95), new Coordinate(0.7, 0.95), new Coordinate(0.7, 0.7)}), geomFactory);
        LinearRing l4 = new LinearRing(new CoordinateArraySequence(new Coordinate[]{new Coordinate(0.1, 0.1), new Coordinate(0.9, 0.1),
                new Coordinate(0.9, 0.9), new Coordinate(0.1, 0.9), new Coordinate(0.1, 0.1)}), geomFactory);
        Polygon p1 = new Polygon(l, new LinearRing[]{l2, l3}, geomFactory);
        Polygon p2 = new Polygon(l, new LinearRing[]{l4}, geomFactory);
        MultiPolygon mp = new MultiPolygon(new Polygon[]{p1, p2}, geomFactory);
        org.geolatte.geom.Geometry geom = JTS.from(mp);
        String output = mapper.writeValueAsString(factory.toTo(geom));
        HashMap map = mapper.readValue(output, HashMap.class);
        Assert.assertEquals("MultiPolygon", map.get("type"));
        Assert.assertEquals("EPSG:900913", getFromMap("crs.properties.name", map));
        Assert.assertEquals("name", getFromMap("crs.type", map));
        // an array of polygons = array of array of linerings = array of array of array of coordinates
        // is an array of array of array of array of doubles!
        List<List<List<List<Double>>>> coords = (List<List<List<List<Double>>>>) map.get("coordinates");
        Assert.assertNotNull(coords);
        // First array is the first polygon. We check the external ring! and we check that there are a total of 3
        // rings!
        Assert.assertEquals(0.0, coords.get(0).get(0).get(0).get(0), 0.00001);
        Assert.assertEquals(0.0, coords.get(0).get(0).get(0).get(1), 0.00001);
        Assert.assertEquals(1.0, coords.get(0).get(0).get(1).get(0), 0.00001);
        Assert.assertEquals(0.0, coords.get(0).get(0).get(1).get(1), 0.00001);
        Assert.assertEquals(2, coords.size());
        Assert.assertEquals(3, coords.get(0).size());
        Assert.assertEquals(2, coords.get(1).size());

        Assert.assertEquals(0.0, coords.get(1).get(0).get(0).get(0), 0.00001);
        Assert.assertEquals(0.0, coords.get(1).get(0).get(0).get(1), 0.00001);
        Assert.assertEquals(1.0, coords.get(1).get(0).get(1).get(0), 0.00001);
        Assert.assertEquals(0.0, coords.get(1).get(0).get(1).get(1), 0.00001);
        // inner ring of the second polygon!
        Assert.assertEquals(0.1, coords.get(1).get(1).get(0).get(0), 0.00001);
        Assert.assertEquals(0.1, coords.get(1).get(1).get(0).get(1), 0.00001);
        Assert.assertEquals(0.9, coords.get(1).get(1).get(1).get(0), 0.00001);
        Assert.assertEquals(0.1, coords.get(1).get(1).get(1).get(1), 0.00001);

        Assert.assertEquals(5, coords.get(0).get(1).size());
        Assert.assertEquals(5, coords.get(0).get(2).size());
        Assert.assertEquals(5, coords.get(1).get(1).size());

        List<Double> bbox = (List<Double>) map.get("bbox");
        // lowx, lowy, highx, highy
        Assert.assertNotNull(coords);
        Assert.assertEquals(0.0, bbox.get(0), 0.00001);
        Assert.assertEquals(0.0, bbox.get(1), 0.00001);
        Assert.assertEquals(1.0, bbox.get(2), 0.00001);
        Assert.assertEquals(1.0, bbox.get(3), 0.00001);
    }

    /**
     * Retrieves the content from a hierarchical map using a path defined as a string denoting the different accesskeys
     * seperated by a dot. Eg, if called on a map with key a.b.c, the method will retrieve the contents of
     * map.get(a).get(b).get(c). If any of the intermediate structures is not a map, or if any other error prevents
     * the method from returning the requested value, an IOException is thrown.
     *
     * @param path       path to use to retrieve the value
     * @param properties the map of properties to retrieve the value from.
     * @return the value that corresponds with the element situated in a hierarchical map as described above.
     * @throws java.io.IOException if the value could not be retrieved
     */
    private Object getFromMap(String path, Map properties)
            throws IOException {
        if (properties == null || path == null) {
            throw new IOException("Map or path invalid");
        }
        String[] parts = path.split("\\.");
        Map current = properties;
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map) current.get(parts[i]);
            if (current == null) {
                throw new IOException("Value does not exist!");
            }
        }
        return current.get(parts[parts.length - 1]);
    }


}
