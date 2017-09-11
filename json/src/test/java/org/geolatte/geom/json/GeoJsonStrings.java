package org.geolatte.geom.json;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class GeoJsonStrings {

    //Points
    static String emptyPointText = "{\"type\":\"Point\",\"coordinates\":[]}";
    static String pointText = "{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}";
    static String pointText3D = "{\"type\":\"Point\",\"coordinates\":[1.0,2.0,3.0]}";
    static String pointTextWithCrs = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[1.0,2.0]}";
    static String pointTextWithCrs3D = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[1.0,2.0,3.0]}";
    static String pointTextWithCrs34D = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[1.0,2.0,3.0,4].0}";
    static String pointTextWithCrsAndBbox = " {\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"bbox\":[1.000000000000000,2.000000000000000,1.000000000000000,2.000000000000000],\"coordinates\":[1,2]}";


    //LineStrings
    static String emptyLineString = "{\"type\":\"LineString\",\"coordinates\":[]}";
    static String simpleLineString = "{\"type\":\"LineString\",\"coordinates\":[[1.0,2.0],[3.0,4.0]]}";
    static String lineStringWithCrs = "{\"type\":\"LineString\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[1.0,2.0],[3.0,4.0]]}";


    //Polygons
    static String emptyPolygon = "{\"type\":\"Polygon\",\"coordinates\":[]}";
    static String polygon = "{\"type\":\"Polygon\",\"coordinates\":[[[1.0,1.0],[1.0,2.0],[2.0,2.0],[2.0,1.0],[1.0,1.0]]]}";
    static String polygonWithCrs = "{\"type\":\"Polygon\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[[1.0,1.0],[1.0,2.0],[2.0,2.0],[2.0,1.0],[1.0,1.0]]]}";

    //MultiPoints
    static String emptyMultiPoint = "{\"type\":\"MultiPoint\",\"coordinates\":[]}";
    static String multiPoint = "{\"type\":\"MultiPoint\",\"coordinates\":[[1.0,2.0],[3.0,4.0]]}";
    static String multiPointWithCrs = "{\"type\":\"MultiPoint\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[1.0,2.0],[3.0,4.0]]}";


    //MultiLineStrings
    static String emptyMultiLineString = "{\"type\":\"MultiLineString\",\"coordinates\":[]}";
    static String multiLineString = "{\"type\":\"MultiLineString\",\"coordinates\":[[[1.0,1.0],[1.0,2.0]],[[3.0,4.0],[5.0,6.0]]]}";
    static String multiLineStringWithCrs = "{\"type\":\"MultiLineString\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[[1.0,1.0],[1.0,2.0]],[[3.0,4.0],[5.0,6.0]]]}";

    //MultiPolygon
    static String emptyMultiPolygon = "{\"type\":\"MultiPolygon\",\"coordinates\":[]}";
    static String multiPolygon = "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[1.0,1.0],[1.0,2.0],[2.0,2.0],[2.0,1.0],[1.0,1.0]]],[[[3.0,3.0],[3.0,5.0],[5.0,5.0],[5.0,3.0],[3.0,3.0]]]]}";
    static String multiPolygonWithCrs = "{\"type\":\"MultiPolygon\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[[[1.0,1.0],[1.0,2.0],[2.0,2.0],[2.0,1.0],[1.0,1.0]]],[[[3.0,3.0],[3.0,5.0],[5.0,5.0],[5.0,3.0],[3.0,3.0]]]]}";

}
