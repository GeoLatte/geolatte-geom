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

import org.geolatte.geom.*;
import org.geolatte.geom.geojson.to.*;

import java.io.IOException;

/**
 * Factory which allows conversion between GeoJsonTo objects on the one hand, and Geolatte-geometries or JTS
 * geometries on the other hand.
 *
 * @author Yves Vandewoude
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
public class GeoJsonToFactory {

    public static final int DEFAULT_SRID = 4326;

    /**
     * Factorymethod that creates the correct to starting from a geolatte geometry
     *
     * @param geometry the geometry to convert
     * @return a to that, once serialized, results in a valid geojson representation of the geometry
     */
    public GeoJsonTo toTo(Geometry geometry) {
        if (geometry instanceof Point) {
            return PointToTo((Point) geometry);
        } else if (geometry instanceof LineString) {
            return lineStringToTo((LineString) geometry);
        } else if (geometry instanceof MultiPoint) {
            return multiPointToTo((MultiPoint) geometry);
        } else if (geometry instanceof MultiLineString) {
            return multiLineStringToTo((MultiLineString) geometry);
        } else if (geometry instanceof Polygon) {
            return polygonToTo((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            return multiLinePolygonToTo((MultiPolygon) geometry);
        }
        return null;
    }

    /**
     * Creates a geolatte geometry object starting from a geojsonto.
     * @param input the geojson to to start from
     * @return the corresponding geometry
     * @throws IOException If the transfer object is invalid or missing information
     */
    public Geometry fromTo(GeoJsonTo input) throws IOException {
        if (input == null) {
            return null;
        }
        Integer srid = getSrid(input);
        int sridValue = srid == null ? DEFAULT_SRID : srid;

        if ("Point".equals(input.getType())) {
            return createPoint(((PointTo) input).getCoordinates(), sridValue);
        } else if ("MultiPoint".equals(input.getType())) {
            MultiPointTo to = (MultiPointTo) input;
            Point[] points = new Point[to.getCoordinates().length];
            for (int i=0; i< points.length; i++) {
                points[i] = createPoint(to.getCoordinates()[i], sridValue);
            }
            return MultiPoint.create(points, sridValue);
        } else if ("LineString".equals(input.getType())) {
            LineStringTo to = (LineStringTo) input;
            return LineString.create(createPointSequence(to.getCoordinates()), sridValue);
        } else if ("MultiLineString".equals(input.getType())) {
            MultiLineStringTo to = (MultiLineStringTo) input;
            LineString[] lineStrings = new LineString[to.getCoordinates().length];
            for (int i =0; i < lineStrings.length;i++) {
                lineStrings[i] = LineString.create(createPointSequence(to.getCoordinates()[i]), sridValue);
            }
            return MultiLineString.create(lineStrings, sridValue);
        } else if ("Polygon".equals(input.getType())) {
            PolygonTo to = (PolygonTo) input;
            return createPolygon(to.getCoordinates(), sridValue);
        } else if ("MultiPolygon".equals(input.getType())) {
            MultiPolygonTo to = (MultiPolygonTo) input;
            Polygon[] polygons = new Polygon[to.getCoordinates().length];
            for (int i=0; i < polygons.length; i ++) {
                polygons[i] = createPolygon(to.getCoordinates()[i], sridValue);
            }
            return MultiPolygon.create(polygons, sridValue);
        }
        return null;
    }

    /**
     * Creates a polygon starting from its geojson coordinate array
     * @param coordinates the geojson coordinate array
     * @param sridValue the srid of the crs to use
     * @return a geolatte polygon instance
     */
    private Polygon createPolygon(double[][][] coordinates, int sridValue) {
        LinearRing[] rings = new LinearRing[coordinates.length];
        for (int i=0; i < coordinates.length;i++) {
            rings[i] = LinearRing.create(createPointSequence(coordinates[i]), sridValue);
        }
        return Polygon.create(rings, sridValue);
    }

    /**
     * Helpermethod that creates a geolatte pointsequence starting from an array containing coordinate arrays
     * @param coordinates an array containing coordinate arrays
     * @return a geolatte pointsequence or null if the coordinatesequence was null
     */
    private PointSequence createPointSequence(double[][] coordinates) {
        if (coordinates == null) {
            return null;
        } else if (coordinates.length == 0) {
            return new PointSequenceFactory().createEmpty();
        }
        DimensionalFlag df = coordinates[0].length == 3 ? DimensionalFlag.XYZ : DimensionalFlag.XY;
        VariableSizePointSequenceBuilder psb = new VariableSizePointSequenceBuilder(df);
        for (double[] point: coordinates) {
              psb.add(point);
        }
        return psb.toPointSequence();
    }

    /**
     * Helpermethod that creates a point starting from its geojsonto coordinate array
     * @param input the coordinate array to convert to a point
     * @param sridValue the sridvalue of the crs in which the point is defined
     * @return an instance of a geolatte point corresponding to the given to or null if the given array is null
     */
    private Point createPoint(double[] input, int sridValue) {
        if (input == null) {
            return null;
        }
        DimensionalFlag df = input.length == 2 ? DimensionalFlag.XY : DimensionalFlag.XYZ;
        return Point.create(input, df, sridValue);
    }

    /**
     * Creates a geojson to from a multipolygon
     * @param input the multipolygon
     * @return the corresponding geojsonto
     */
    private MultiPolygonTo multiLinePolygonToTo(MultiPolygon input) {
        MultiPolygonTo result = new MultiPolygonTo();
        double[][][][] coordinates = new double[input.getNumGeometries()][][][];
        for (int i=0; i < input.getNumGeometries();i++) {
            coordinates[i] = polygonToTo(input.getGeometryN(i)).getCoordinates();
        }
        result.setCoordinates(coordinates);
        result.setCrs(GeoJsonTo.createCrsTo("EPSG:" + input.getSRID()));
        return result;
    }

    /**
     * Converts a polygon to its corresponding to
     * @param input a polygon object
     * @return a transfer object, that will result into a valid geojson string when serialized by a json serializer
     */
    private PolygonTo polygonToTo(Polygon input) {
        PolygonTo result = new PolygonTo();
        result.setCrs(GeoJsonTo.createCrsTo("EPSG:" + input.getSRID()));
        double[][][] rings = new double[input.getNumInteriorRing()+1][][];
        // Exterior ring:
        rings[0] = getPoints(input.getExteriorRing());
        // Interior rings!
        for (int i=0; i < input.getNumInteriorRing(); i++) {
            rings[i+1] = getPoints(input.getInteriorRingN(i));
        }
        result.setCoordinates(rings);
        return result;
    }


    /**
     * Generates the to for a multilinestring
     * @param input the multilinestring
     * @return a to which serialized corresponds to a valid geojson representation of a multilinestring
     */
    private MultiLineStringTo multiLineStringToTo(MultiLineString input) {
        MultiLineStringTo result = new MultiLineStringTo();
        double[][][] resultCoordinates = new double[input.getNumGeometries()][][];
        for (int i = 0; i < input.getNumGeometries(); i++) {
            resultCoordinates[i] = getPoints(input.getGeometryN(i));
        }
        result.setCoordinates(resultCoordinates);
        result.setCrs(GeoJsonTo.createCrsTo("EPSG:" + input.getSRID()));
        return result;
    }

    /**
     * Converts a multipoint to a to
     *
     * @param input the multipoint
     * @return the corresponding to
     */
    private MultiPointTo multiPointToTo(MultiPoint input) {
        MultiPointTo result = new MultiPointTo();
        result.setCrs(GeoJsonTo.createCrsTo("EPSG:" + input.getSRID()));
        result.setCoordinates(getPoints(input));
        return result;
    }

    /**
     * Convert a point to the corresponding to
     *
     * @param input the point object
     * @return the to corresponding with a point
     */
    private PointTo PointToTo(Point input) {
        PointTo result = new PointTo();
        result.setCrs(GeoJsonTo.createCrsTo("EPSG:" + input.getSRID()));
        result.setCoordinates(input.is3D() ? new double[]{input.getX(), input.getY(), input.getZ()}
                : new double[]{input.getX(), input.getY()});
        return result;
    }

    /**
     * Convert a linestring to a to
     *
     * @param input the linestring object to convert
     * @return a linestringto
     */
    private LineStringTo lineStringToTo(LineString input) {
        LineStringTo result = new LineStringTo();
        result.setCrs(GeoJsonTo.createCrsTo("EPSG:" + input.getSRID()));
        result.setCoordinates(getPoints(input));
        return result;
    }


    /**
     * Serializes all points of the input into a list of their coordinates
     * @param input a geometry whose points are to be converted to a list of coordinates
     * @return an array containing arrays with x,y and optionally z values.
     */
    private double[][] getPoints(Geometry input) {
        double[][] result = new double[input.getNumPoints()][];
        int i = 0;
        for (Point p: input.getPoints()) {
            result[i] = input.is3D() ? new double[]{p.getX(), p.getY(), p.getZ()} : new double[]{p.getX(), p.getY()};
            i++;
        }
        return result;
    }

   /**
     * If an srid (crs) is specified in the json object, it is returned. If no srid is found in the current parameter-map
     * null is returned. This is a simplified version from the actual GeoJSON specification, since the GeoJSON specification
     * allows for relative links to either URLS or local files in which the crs should be defined. This implementation
     * only supports named crs's: namely:
     * <pre>
     *  "crs": {
     *       "type": "name",
     *       "properties": {
     *             "name": "<yourcrsname>"
     *       }
     * }
     * </pre>
     * Besides the fact that only named crs is permitted for deserialization, the given name must either be of the form:
     * <pre>
     *  urn:ogc:def:EPSG:x.y:4326
     * </pre>
     * (with x.y the version of the EPSG) or of the form:
     * <pre>
     * EPSG:4326
     * </pre>
     * @return the SRID value of the crs system in the json if it is present, null otherwise.
     * @throws java.io.IOException If a crs object is present, but deserialization is not possible
     */
    protected Integer getSrid(GeoJsonTo to) throws IOException {
        if (to.getCrs() == null) {
            return null;
        } else {
            if (to.getCrs().getType() == null || !"name".equals(to.getCrs().getType())) {
                throw new IOException("If the crs is specified the type must be specified. Currently, only named crses are supported.");
            }
            if (to.getCrs().getProperties() == null || to.getCrs().getProperties().getName() == null) {
                throw new IOException("A crs specification requires a properties value containing a name value.");
            }
            String sridString = to.getCrs().getProperties().getName();
            if (sridString.startsWith("EPSG:")) {
                Integer srid = parseDefault(sridString.substring(5), null);
                if (srid == null) {
                    throw new IOException("Unable to derive SRID from crs name");
                } else {
                    return srid;
                }
            } else if (sridString.startsWith("urn:ogc:def:crs:EPSG:")) {
                String[] splits = sridString.split(":");
                if (splits.length != 7) {
                    throw new IOException("Unable to derive SRID from crs name");
                } else {
                    Integer srid = parseDefault(splits[6], null);
                    if (srid == null) {
                        throw new IOException("Unable to derive SRID from crs name");
                    }
                    return srid;
                }
            } else {
                throw new IOException("Unable to derive SRID from crs name");
            }
        }
    }

    /**
     * Convenience method. Parses a string into a double. If it can no be converted to a double, the
     * defaultvalue is returned. Depending on your choice, you can allow null as output or assign it some value
     * and have very convenient syntax such as: double d = parseDefault(myString, 0.0); which is a lot shorter
     * than dealing with all kinds of numberformatexceptions.
     *
     * @param input        The inputstring
     * @param defaultValue The value to assign in case of error
     * @return A double corresponding with the input, or defaultValue if no double can be extracted
     */
    public Integer parseDefault(String input, Integer defaultValue) {
        if (input == null) {
            return defaultValue;
        }
        Integer answer = defaultValue;
        try {
            answer = Integer.parseInt(input);
        } catch (NumberFormatException ignored) {
        }
        return answer;
    }
}
