package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;

/**
 * Are Geolatte Geoms serializable?
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 09/11/15.
 */
public class SerializableTest {

    CoordinateReferenceSystem<C2D> crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);

    @Test
    public void testSerializePoint() throws Exception {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        Geometry<C2D> geom = point(crs, c(3, 4));
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutput);
        outputStream.writeObject(geom);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        Geometry<C2D> geomOut = (Geometry<C2D>) inputStream.readObject();

        assertEquals(geom, geomOut);
    }

    @Test
    public void testSerializeLineString() throws Exception {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        Geometry<C2D> geom = linestring(crs, c(3, 4), c(5, 5));
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutput);
        outputStream.writeObject(geom);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        Geometry<C2D> geomOut = (Geometry<C2D>) inputStream.readObject();

        assertEquals(geom, geomOut);
    }


    @Test
    public void testSerializePolygon() throws Exception {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        Polygon<C2D> geom = polygon(crs, ring(c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0)));
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutput);
        outputStream.writeObject(geom);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        Polygon<C2D> geomOut = (Polygon<C2D>) inputStream.readObject();

        assertEquals(geom, geomOut);
    }

    @Test
    public void testSerializeGeometryCollection() throws Exception {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        GeometryCollection<C2D, Geometry<C2D>> geom = geometrycollection(crs, point(c(4, 4)), linestring(c(3, 4), c(5, 5)));
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutput);
        outputStream.writeObject(geom);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        Geometry<C2D> geomOut = (Geometry<C2D>) inputStream.readObject();

        assertEquals(geom, geomOut);
    }

    @Test
    public void testSerializeMultiLinestring() throws Exception {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        MultiLineString<C2D> geom = multilinestring(crs, linestring(c(1, 1), c(4, 4)), linestring(c(3, 4), c(5, 5)));
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutput);
        outputStream.writeObject(geom);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        Geometry<C2D> geomOut = (Geometry<C2D>) inputStream.readObject();

        assertEquals(geom, geomOut);
    }

    @Test
    public void testSerializeJtsPoint() throws Exception {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        Geometry<C2D> geom = point(crs, c(3, 4));
        com.vividsolutions.jts.geom.Point jtsPoint = (com.vividsolutions.jts.geom.Point) JTS.to(geom);
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutput);
        outputStream.writeObject(jtsPoint);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        com.vividsolutions.jts.geom.Point geomOut = (com.vividsolutions.jts.geom.Point) inputStream.readObject();

        assertEquals(jtsPoint, geomOut);
    }


    @Test
    public void testSerializeJtsLineString() throws Exception {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        com.vividsolutions.jts.geom.LineString jts = (com.vividsolutions.jts.geom.LineString) JTS.to(linestring(crs, c(3, 4), c(5, 5)));
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutput);
        outputStream.writeObject(jts);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        com.vividsolutions.jts.geom.LineString geomOut = (com.vividsolutions.jts.geom.LineString) inputStream.readObject();

        assertEquals(jts, geomOut);
    }

    @Test
    public void testSerializeJtsMultiLineString() throws Exception {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        com.vividsolutions.jts.geom.MultiLineString jts = (com.vividsolutions.jts.geom.MultiLineString) JTS.to(
                multilinestring(crs, linestring(c(1, 1), c(4, 4)), linestring(c(3, 4), c(5, 5)))
        );
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutput);
        outputStream.writeObject(jts);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        com.vividsolutions.jts.geom.MultiLineString geomOut = (com.vividsolutions.jts.geom.MultiLineString) inputStream.readObject();

        assertEquals(jts, geomOut);
    }


    @Test
    public void testSerializeJtsPolygon() throws Exception {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        com.vividsolutions.jts.geom.Polygon jts = (com.vividsolutions.jts.geom.Polygon) JTS.to(
                polygon(crs, ring(c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0)))
        );
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutput);
        outputStream.writeObject(jts);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        com.vividsolutions.jts.geom.Polygon geomOut = (com.vividsolutions.jts.geom.Polygon) inputStream.readObject();

        assertEquals(jts, geomOut);
    }


}
