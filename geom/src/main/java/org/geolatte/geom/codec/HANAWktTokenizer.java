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

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A tokenizer for the HANA WKT representation.
 * <p/>
 * <p>
 * The variant of WKT that this tokenizer recognizes is determined by the {@link WktVariant} that is passed upon
 * construction.
 * </p>
 *
 * @author Jonathan Bregler, SAP
 */
class HANAWktTokenizer extends WktTokenizer {

	/**
	 * A Tokenizer for the specified WKT string
	 *
	 * @param wkt the string to tokenize
	 * @param variant the list of words to recognize as separate variant
	 * @param baseCRS the <code>CoordinateReferenceSystem</code> for the points in the WKT representation.
	 */
	protected HANAWktTokenizer(CharSequence wkt, WktVariant variant, CoordinateReferenceSystem<?> baseCRS, boolean forceToCRS) {
		super( wkt, variant, baseCRS, forceToCRS );
	}
	
	protected HANAWktTokenizer(CharSequence wkt, WktVariant variant, CoordinateReferenceSystem<?> baseCRS) {
		super( wkt, variant, baseCRS );
	}

	@Override
	protected WktToken readToken() {
		int endPos = this.currentPos;
		while ( endPos < wkt.length() && isWordChar( wkt.charAt( endPos ) ) ) {
			endPos++;
		}

		if ( endPos < wkt.length() && wkt.charAt( endPos ) == ' ' ) {
			int tmpPos = endPos;
			// skip whitespace
			while ( tmpPos < wkt.length() && Character.isWhitespace( wkt.charAt( tmpPos ) ) ) {
				tmpPos++;
			}

			// read Z and/or M
			if ( tmpPos < wkt.length() ) {
				if (wkt.charAt( tmpPos ) == 'Z') {
					tmpPos++;
					if (tmpPos < wkt.length() && wkt.charAt( tmpPos ) == 'M') {
						tmpPos++;
					}
					endPos = tmpPos;
				}
				else if (wkt.charAt( tmpPos ) == 'M') {
					tmpPos++;
					endPos = tmpPos;
				}
			}
		}

		WktToken nextToken = matchKeyword( currentPos, endPos );
		currentPos = endPos;
		return nextToken;
	}

}
