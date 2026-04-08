package org.geolatte.geom.json;

/**
 * Canonical GeoJSON strings used as fixtures in the test suite.
 *
 * <p>Lives in {@code geolatte-geojson-core}'s test sources so it can be shared between
 * the Jackson 2 and Jackson 3 adapter modules via the test-jar.</p>
 *
 * <p>Created by Karel Maesen, Geovise BVBA on 09/09/17.</p>
 */
public class GeoJsonStrings {

    //Points
    public static final String emptyPointText = "{\"type\":\"Point\",\"coordinates\":[]}";
    public static final String pointText = "{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}";
    public static final String pointText3D = "{\"type\":\"Point\",\"coordinates\":[1.0,2.0,3.0]}";
    public static final String pointText2DM = "{\"type\":\"Point\",\"coordinates\":[1.0,2.0,4.0]}";
    public static final String pointText3DM = "{\"type\":\"Point\",\"coordinates\":[1.0,2.0,3.0, 4.0]}";
    public static final String pointTextWithCrs = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[1.0,2.0]}";
    public static final String pointTextWithUrnCrs = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:EPSG::31370\"}},\"coordinates\":[1.0,2.0]}";
    public static final String pointTextWithCrs3D = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[1.0,2.0,3.0]}";
    public static final String pointTextWithCrs34D = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[1.0,2.0,3.0,4.0]}";
    public static final String pointTextWithCrsAndBbox = " {\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"bbox\":[1.000000000000000,2.000000000000000,1.000000000000000,2.000000000000000],\"coordinates\":[1,2]}";


    //LineStrings
    public static final String emptyLineString = "{\"type\":\"LineString\",\"coordinates\":[]}";
    public static final String simpleLineString = "{\"type\":\"LineString\",\"coordinates\":[[1.0,2.0],[3.0,4.0]]}";
    public static final String lineString2DM = "{\"type\":\"LineString\",\"coordinates\":[[1.0,2.0, 3.0],[10.0, 20.0, 30.0]]}";
    public static final String lineStringWithCrs = "{\"type\":\"LineString\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[1.0,2.0],[3.0,4.0]]}";


    //Polygons
    public static final String emptyPolygon = "{\"type\":\"Polygon\",\"coordinates\":[]}";
    public static final String polygon = "{\"type\":\"Polygon\",\"coordinates\":[[[1.0,1.0],[1.0,2.0],[2.0,2.0],[2.0,1.0],[1.0,1.0]]]}";
    public static final String polygonWithCrs = "{\"type\":\"Polygon\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[[1.0,1.0],[1.0,2.0],[2.0,2.0],[2.0,1.0],[1.0,1.0]]]}";

    //MultiPoints
    public static final String emptyMultiPoint = "{\"type\":\"MultiPoint\",\"coordinates\":[]}";
    public static final String multiPoint = "{\"type\":\"MultiPoint\",\"coordinates\":[[1.0,2.0],[3.0,4.0]]}";
    public static final String multiPointWithCrs = "{\"type\":\"MultiPoint\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[1.0,2.0],[3.0,4.0]]}";


    //MultiLineStrings
    public static final String emptyMultiLineString = "{\"type\":\"MultiLineString\",\"coordinates\":[]}";
    public static final String multiLineString = "{\"type\":\"MultiLineString\",\"coordinates\":[[[1.0,1.0],[1.0,2.0]],[[3.0,4.0],[5.0,6.0]]]}";
    public static final String multiLineStringWithCrs = "{\"type\":\"MultiLineString\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[[1.0,1.0],[1.0,2.0]],[[3.0,4.0],[5.0,6.0]]]}";

    //MultiPolygon
    public static final String emptyMultiPolygon = "{\"type\":\"MultiPolygon\",\"coordinates\":[]}";
    public static final String multiPolygon = "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[1.0,1.0],[1.0,2.0],[2.0,2.0],[2.0,1.0],[1.0,1.0]]],[[[3.0,3.0],[3.0,5.0],[5.0,5.0],[5.0,3.0],[3.0,3.0]]]]}";
    public static final String multiPolygonWithCrs = "{\"type\":\"MultiPolygon\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[[[[1.0,1.0],[1.0,2.0],[2.0,2.0],[2.0,1.0],[1.0,1.0]]],[[[3.0,3.0],[3.0,5.0],[5.0,5.0],[5.0,3.0],[3.0,3.0]]]]}";

    // GeometryCollection
    public static final String emptyGeometryCollection = "{\"type\":\"GeometryCollection\",\"geometries\":[]}";
    public static final String geometryCollection = "{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"LineString\",\"coordinates\":[[1.0,1.0],[1.0,2.0]]},{\"type\":\"Point\",\"coordinates\":[5.0,6.0]}]}";
    public static final String geometryCollectionWithCrs = "{\"type\":\"GeometryCollection\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"geometries\":[{\"type\":\"LineString\",\"coordinates\":[[1.0,1.0],[1.0,2.0]]},{\"type\":\"Point\",\"coordinates\":[5.0,6.0]}]}";
    public static final String geometryCollectionWithCrs3D = "{\"type\":\"GeometryCollection\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"geometries\":[{\"type\":\"LineString\",\"coordinates\":[[1.0,1.0,1.0],[1.0,2.0,3.0]]},{\"type\":\"Point\",\"coordinates\":[5.0,6.0,7.0]}]}";

    // Features
    public static final String feature = "{\"type\":\"Feature\",\"id\":\"1\",\"geometry\":{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}},\"coordinates\":[1.0,2.0]},\"properties\":{\"a\":1}}";
    public static final String featureWithBBox = "{\"type\":\"Feature\",\"id\":\"1\",\"bbox\":[1.0,2.0,1.0,2.0],\"geometry\":{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}},\"coordinates\":[1.0,2.0]},\"properties\":{\"a\":1}}";
    public static final String featureWithLineString = "{\"type\":\"Feature\",\"id\":\"1\",\"bbox\":[1.0,2.0,3.0,4.0],\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[1.0,2.0],[3.0,4.0]]},\"properties\":{\"a\":1}}";


    public static final String featureIntId = "{\"type\" : \"Feature\", \"id\": 1, \"geometry\":  {\"type\":\"Point\",\"coordinates\":[1.0,2.0]}, " +
            "\"properties\": { \"a\" : 1 }}";
    public static final String featureNullGeometry = "{\"type\":\"Feature\",\"id\":\"1\",\"geometry\":null,\"properties\":{\"a\":1}}";
    public static final String featureEmptyPolygon = "{\"type\":\"Feature\",\"id\":\"1\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[]},\"properties\":{\"a\":1}}";

    // crs
    public static final String crswgs84 = "{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}";
    public static final String crswgs84TextWithUrnCrs = "{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:EPSG::4326\"}}";
    public static final String crslambert72 = "{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}}";
    public static final String crslambert72TextWithUrnCrs = "{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:EPSG::31370\"}}";
    public static final String crswgs84WithLink = "{\"type\":\"link\",\"properties\":{\"href\":\"http://wwww.opengis.net/def/crs/EPSG/4326\"}}";

    //FeatureCollection

    public static final String featureCollection = "{\"type\":\"FeatureCollection\",\"bbox\":[102.0,0.0,105.0,1.0],\"features\":[{\"type\":\"Feature\",\"id\":\"1\",\"geometry\":{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}},\"coordinates\":[102.0,0.5]},\"properties\":{\"prop0\":\"value0\"}},{\"type\":\"Feature\",\"id\":\"2\",\"geometry\":{\"type\":\"LineString\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}},\"coordinates\":[[102.0,0.0],[103.0,1.0],[104.0,0.0],[105.0,1.0]]},\"properties\":{\"prop1\":0.0,\"prop0\":\"value0\"}}]}";
    public static final String featureCollectionNoBbox = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"id\":\"1\",\"geometry\":{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}},\"coordinates\":[102.0,0.5]},\"properties\":{\"prop0\":\"value0\"}},{\"type\":\"Feature\",\"id\":\"2\",\"geometry\":{\"type\":\"LineString\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}},\"coordinates\":[[102.0,0.0],[103.0,1.0],[104.0,0.0],[105.0,1.0]]},\"properties\":{\"prop1\":0.0,\"prop0\":\"value0\"}}]}";
    public static final String emptyFeatureCollection = "{\"type\":\"FeatureCollection\",\"features\":[]}";
}
