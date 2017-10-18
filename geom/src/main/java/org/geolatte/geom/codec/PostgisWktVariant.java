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
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.*;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasMeasureAxis;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasVerticalAxis;

/**
 * Punctuation and keywords for Postgis EWKT/WKT representations.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class PostgisWktVariant extends WktVariant {

    protected static final WktEmptyGeometryToken EMPTY = new WktEmptyGeometryToken();

    private final static List<WktGeometryToken> GEOMETRIES = new ArrayList<WktGeometryToken>();

    private final static Set<WktKeywordToken> KEYWORDS;

    protected PostgisWktVariant() {
        super('(', ')', ',');
    }

    static {

        //TODO -- this doesn't work well with LinearRings (geometry type doesn't match 1-1 to WKT types)
        //register the geometry tokens
        add(GeometryType.POINT, false, "POINT");
        add(GeometryType.POINT, true, "POINTM");
        add(GeometryType.LINESTRING, true, "LINESTRINGM");
        add(GeometryType.LINESTRING, false, "LINESTRING");
        add(GeometryType.POLYGON, false, "POLYGON");
        add(GeometryType.POLYGON, true, "POLYGONM");
        add(GeometryType.MULTIPOINT, true, "MULTIPOINTM");
        add(GeometryType.MULTIPOINT, false, "MULTIPOINT");
        add(GeometryType.MULTILINESTRING, false, "MULTILINESTRING");
        add(GeometryType.MULTILINESTRING, true, "MULTILINESTRINGM");
        add(GeometryType.MULTIPOLYGON, false, "MULTIPOLYGON");
        add(GeometryType.MULTIPOLYGON, true, "MULTIPOLYGONM");
        add(GeometryType.GEOMETRYCOLLECTION, false, "GEOMETRYCOLLECTION");
        add(GeometryType.GEOMETRYCOLLECTION, true, "GEOMETRYCOLLECTIONM");
        //create an unmodifiable set of all pattern tokens
        Set<WktKeywordToken> allTokens = new HashSet<WktKeywordToken>();
        allTokens.addAll(GEOMETRIES);
        allTokens.add(EMPTY);
        KEYWORDS = Collections.unmodifiableSet(allTokens);
    }

    private static void add(GeometryType type, boolean isMeasured, String word) {
        GEOMETRIES.add(new WktGeometryToken(word, type, isMeasured));
    }

    public String wordFor(Geometry geometry, boolean ignoreMeasureMarker) {
        for (WktGeometryToken candidate : GEOMETRIES) {
            if (sameGeometryType(candidate, geometry) && hasSameMeasuredSuffixInWkt(candidate, geometry, ignoreMeasureMarker)) {
                return candidate.getPattern().toString();
            }
        }
        throw new IllegalStateException(
                String.format(
                        "Geometry type %s not recognized.",
                        geometry.getClass().getName()
                )
        );
    }

    @Override
    protected Set<WktKeywordToken> getWktKeywords() {
        return KEYWORDS;
    }

    public WktKeywordToken getEmpty() {
        return EMPTY;
    }

    protected boolean sameGeometryType(WktGeometryToken token, Geometry geometry) {
        //TODO Need better handling for difference WKT/Geometry types. See comment above .
        return token.getType() == geometry.getGeometryType() ||
                (token.getType().equals(GeometryType.LINESTRING) &&
                geometry.getGeometryType().equals(GeometryType.LINEARRING));
    }

    /**
     * Determines whether the candidate has the same measured 'M' suffix as the geometry in WKT.
     * The suffix is only added when the geometry is measured and not 3D.
     * <p/>
     * POINT(x y): 2D point,
     * POINT(x y z): 3D point,
     * POINTM(x y m): 2D measured point (with 'M' suffix),
     * POINT(x y z m): 3D measured point (without 'M' suffix)
     *
     *
     * @param candidate The candidate wkt geometry token
     * @param geometry  The geometry to check the candidate wkt geometry token for
     * @param ignoreMeasureMarker when set to true, this method returns true iff the candidate token is not measured
     * @return The candidate is measured if and only if the geometry is measured and not 3D
     */
    private boolean hasSameMeasuredSuffixInWkt(WktGeometryToken candidate, Geometry geometry, boolean ignoreMeasureMarker) {
        if (ignoreMeasureMarker) {
            return !candidate.isMeasured();
        }
        CoordinateReferenceSystem<?> crs = geometry.getCoordinateReferenceSystem();
        if (hasMeasureAxis(crs) && ! hasVerticalAxis(crs)) {
            return candidate.isMeasured();
        } else {
            return !candidate.isMeasured();
        }
    }
}
