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

import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A <code>WktToken</code> for point sequences in the input text.
 * <p>Note that the tokenizer returns in a {@code WktPointSequenceToken} a different {@code CoordinateReferenceSystem}
 * then the one declared in the WKT for the case of 3D and/or measured coordinate system. </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/19/11
 */
class WktPointSequenceToken<P extends Position> implements WktToken {

    private final PositionSequence<P> positions;
    private final CoordinateReferenceSystem<P> crs;

    public WktPointSequenceToken(PositionSequence<P> positions, CoordinateReferenceSystem<P> crs) {
        this.positions = positions;
        this.crs = crs;
    }

    public PositionSequence<P> getPositions() {
        return this.positions;
    }

    public CoordinateReferenceSystem<P> getCoordinateReferenceSystem() {
        return this.crs;
    }


    public String toString() {
        return positions.toString();
    }

}
