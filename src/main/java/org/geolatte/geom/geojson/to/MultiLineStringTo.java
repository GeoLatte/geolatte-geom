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
 * This class represents a transfer object which, if serialized by a standard JSON serializer, leads to a valid
 * GeoJSON representation of a MultiLineString
 *
 * @author Yves Vandewoude
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
public class MultiLineStringTo extends GeoJsonTo {

    private double[][][] coordinates;
    private double[] bbox;


    public MultiLineStringTo() {
    }

    public String getType() {
        return "MultiLineString";
    }

    @Override
    public boolean isValid() {
        if (coordinates == null || coordinates.length == 0) {
            return false;
        }
        for (double[][] lineString: coordinates) {
            if (lineString == null || lineString.length < 2) {
                return false;
            }
            int length = lineString[0].length;
            if (length != 2 && length != 3) {
                return false;
            }
            for (double[] points: lineString) {
                if (points.length != length) {
                    return false;
                }
            }
        }
        return true;
    }

    public double[][][] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[][][] coordinates) {
        this.coordinates = coordinates;
        if (isValid()) {
            bbox = createBoundingBox(coordinates);
        }
    }

    /**
     * @return the bbox for this geometry as it is to be serialized.
     */
    public double[] getBbox() {
        return bbox;
    }


}
