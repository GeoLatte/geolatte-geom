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

package org.geolatte.geom.geojson.to;

/**
 * Abstract parentclass for all geojson transfer objects
 *
 * @author Yves Vandewoude
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
public abstract class GeoJsonTo {

    private CrsTo crs;

    /**
     * @return According to the geojson specification, the value of the type member
     *         must be one of: "Point", "MultiPoint", "LineString", "MultiLineString", "Polygon", "MultiPolygon",
     *         "GeometryCollection", "Feature", or "FeatureCollection". The case of the type member values must be as shown here.
     */
    public abstract String getType();

    /**
     * @return whether the transfer object corresponds with a valid GeoJson entity
     */
    public abstract boolean isValid();

    /**
     * @return the crs which is specified in this to.
     */
    public CrsTo getCrs() {
        return crs;
    }

    /**
     * @param crs the crs to set for this geometry
     */
    public void setCrs(CrsTo crs) {
        this.crs = crs;
    }

    /**
     * Convenience method to create a named crs to.
     *
     * @param crsName the name of the crs to use, if null, the default crs for geojson is used.
     * @return the transfer object that corresponds with the default Crs. According to the GeoJson spec
     *         The default CRS is a geographic coordinate reference system, using the WGS84 datum, and with longitude and latitude units of decimal degrees.
     */
    public static CrsTo createCrsTo(String crsName) {
        String nameToUse = crsName == null ? "EPSG:4326" : crsName;
        CrsTo result = new CrsTo();
        NamedCrsPropertyTo property = new NamedCrsPropertyTo();
        property.setName(nameToUse);
        result.setProperties(property);
        return result;
    }

    /**
     * This method computes the boundingbox of a list of points (such as the coordinates of a multipoint or linestring)
     *
     * @param input a list of lists that each contain two or three doubles (x, y and optional z coordinates)
     * @return a list with doubles. the result is a 2*n array where n is the number of dimensions represented
     *         in the input, with the lowest values for all axes followed by the highest values.
     */
    public static double[] createBoundingBox(double[][] input) {
        double[] result = new double[input[0].length];
        for (int i = 0; i < result.length / 2; i++) {
            result[i] = Double.MAX_VALUE;
        }
        for (int i = result.length / 2; i < result.length; i++) {
            result[i] = Double.MIN_VALUE;
        }
        for (double[] coord : input) {
            for (int i = 0; i < coord.length; i++) {
                result[i] = Math.min(coord[i], result[i]);
                result[i + result.length] = Math.max(coord[i + result.length], result[i + result.length]);
            }
        }
        return result;
    }

    /**
     * This method computes the boundingbox of a threedimensional list of doubles, which are to be interpreted as
     * a list of lists of coordinates (such as the coordinates of a multilinestring or polygon)
     *
     * @param input a list of lists that each contain two or three doubles (x, y and optional z coordinates)
     * @return a list with doubles. the result is a 2*n array where n is the number of dimensions represented
     *         in the input, with the lowest values for all axes followed by the highest values.
     */
    public static double[] createBoundingBox(double[][][] input) {
        double[] bbox = createBoundingBox(input[0]);
        for (int i = 1; i < input.length; i++) {
            double[] current = createBoundingBox(input[i]);
            for (int j = 0; j < bbox.length / 2; j++) {
                bbox[j] = Math.min(bbox[j], current[j]);
                bbox[j + bbox.length / 2] = Math.max(bbox[j + bbox.length / 2], current[j + bbox.length / 2]);
            }
        }
        return bbox;
    }

    /**
     * This method computes the boundingbox of a fourdimensional list of doubles, which are to be interpreted as
     * a list of items that each represent list of lists of coordinates (such as the coordinates of a multilipolygon,
     * which contains lists of polygons, each containing lists of linearrings that contain coordinates)
     *
     * @param input a list of lists that each contain two or three doubles (x, y and optional z coordinates)
     * @return a list with doubles. the result is a 2*n array where n is the number of dimensions represented
     *         in the input, with the lowest values for all axes followed by the highest values.
     */
    public static double[] createBoundingBox(double[][][][] input) {
        double[] bbox = createBoundingBox(input[0]);
        for (int i = 1; i < input.length; i++) {
            double[] current = createBoundingBox(input[i]);
            for (int j = 0; j < bbox.length / 2; j++) {
                bbox[j] = Math.min(bbox[j], current[j]);
                bbox[j + bbox.length / 2] = Math.max(bbox[j + bbox.length / 2], current[j + bbox.length / 2]);
            }
        }
        return bbox;

    }
}
