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
 * Punctuation and keywords for HANA EWKT/WKT representations.
 *
 * @author Jonathan Bregler, SAP
 */
class HANAWktVariant extends PostgisWktVariant {
	
	private final static List<HANAWktGeometryToken> GEOMETRIES = new ArrayList<HANAWktGeometryToken>();

	private final static Set<WktKeywordToken> KEYWORDS;

	protected HANAWktVariant() {
	}

	static {

		// TODO -- this doesn't work well with LinearRings (geometry type doesn't match 1-1 to WKT types)
		// register the geometry tokens
		add( GeometryType.POINT, false, false, "POINT" );
		add( GeometryType.POINT, true, false, "POINT M" );
		add( GeometryType.POINT, false, true, "POINT Z" );
		add( GeometryType.POINT, true, true, "POINT ZM" );
		add( GeometryType.LINESTRING, true, false, "LINESTRING M" );
		add( GeometryType.LINESTRING, false, false, "LINESTRING" );
		add( GeometryType.LINESTRING, true, true, "LINESTRING ZM" );
		add( GeometryType.LINESTRING, false, true, "LINESTRING Z" );
		add( GeometryType.POLYGON, false, false, "POLYGON" );
		add( GeometryType.POLYGON, true, false, "POLYGON M" );
		add( GeometryType.POLYGON, false, true, "POLYGON Z" );
		add( GeometryType.POLYGON, true, true, "POLYGON ZM" );
		add( GeometryType.MULTIPOINT, true, false, "MULTIPOINT M" );
		add( GeometryType.MULTIPOINT, false, false, "MULTIPOINT" );
		add( GeometryType.MULTIPOINT, true, true, "MULTIPOINT ZM" );
		add( GeometryType.MULTIPOINT, false, true, "MULTIPOINT Z" );
		add( GeometryType.MULTILINESTRING, false, false, "MULTILINESTRING" );
		add( GeometryType.MULTILINESTRING, true, false, "MULTILINESTRING M" );
		add( GeometryType.MULTILINESTRING, false, true, "MULTILINESTRING Z" );
		add( GeometryType.MULTILINESTRING, true, true, "MULTILINESTRING ZM" );
		add( GeometryType.MULTIPOLYGON, false, false, "MULTIPOLYGON" );
		add( GeometryType.MULTIPOLYGON, true, false, "MULTIPOLYGON M" );
		add( GeometryType.MULTIPOLYGON, false, true, "MULTIPOLYGON Z" );
		add( GeometryType.MULTIPOLYGON, true, true, "MULTIPOLYGON ZM" );
		add( GeometryType.GEOMETRYCOLLECTION, false, false, "GEOMETRYCOLLECTION" );
		add( GeometryType.GEOMETRYCOLLECTION, true, false, "GEOMETRYCOLLECTION M" );
		add( GeometryType.GEOMETRYCOLLECTION, false, true, "GEOMETRYCOLLECTION Z" );
		add( GeometryType.GEOMETRYCOLLECTION, true, true, "GEOMETRYCOLLECTION ZM" );
		// create an unmodifiable set of all pattern tokens
		Set<WktKeywordToken> allTokens = new HashSet<WktKeywordToken>();
		allTokens.addAll( GEOMETRIES );
		allTokens.add( EMPTY );
		KEYWORDS = Collections.unmodifiableSet( allTokens );
	}

	private static void add(GeometryType type, boolean isMeasured, boolean is3D, String word) {
		GEOMETRIES.add( new HANAWktGeometryToken( word, type, isMeasured, is3D ) );
	}

	@Override
	public String wordFor(@SuppressWarnings("rawtypes") Geometry geometry, boolean ignoreMeasureMarker) {
		for ( HANAWktGeometryToken candidate : GEOMETRIES ) {
			if ( sameGeometryType( candidate, geometry ) && hasSameMeasuredAndZAxisSuffixInWkt( candidate, geometry, ignoreMeasureMarker ) ) {
				return candidate.getPattern().toString();
			}
		}
		throw new IllegalStateException(
				String.format(
						"Geometry type %s not recognized.",
						geometry.getClass().getName() ) );
	}

	@Override
	protected Set<WktKeywordToken> getWktKeywords() {
		return KEYWORDS;
	}

	/**
	 * Determines whether the candidate has the same measured 'M' suffix and Z axis as the geometry in WKT.
	 * <p/>
	 * POINT(x y): 2D point, 
	 * POINT Z(x y z): 3D point, 
	 * POINT M(x y m): 2D measured point, 
	 * POINT ZM(x y z m): 3D measured point
	 *
	 * @param candidate The candidate wkt geometry token
	 * @param geometry The geometry to check the candidate wkt geometry token for
	 * @param ignoreMeasureMarker when set to true, this method returns true iff the candidate token is not measured
	 * @return The candidate is measured if and only if the geometry is measured and not 3D
	 */
	private boolean hasSameMeasuredAndZAxisSuffixInWkt(HANAWktGeometryToken candidate, Geometry<?> geometry, boolean ignoreMeasureMarker) {
		if ( ignoreMeasureMarker ) {
			return !candidate.isMeasured();
		}
		CoordinateReferenceSystem<?> crs = geometry.getCoordinateReferenceSystem();
		if ( hasMeasureAxis( crs ) ) {
			if ( hasVerticalAxis( crs ) ) {
				return candidate.isMeasured() && candidate.is3D();
			}
			else {
				return candidate.isMeasured() && !candidate.is3D();
			}
		}
		else {
			if ( hasVerticalAxis( crs ) ) {
				return !candidate.isMeasured() && candidate.is3D();
			}
			else {
				return !candidate.isMeasured() && !candidate.is3D();
			}
		}
	}
}
