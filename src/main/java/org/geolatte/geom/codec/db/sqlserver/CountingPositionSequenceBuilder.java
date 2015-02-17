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
 * Copyright (C) 2010 - 2013 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.codec.db.sqlserver;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 10/19/12
 */
public class CountingPositionSequenceBuilder<P extends Position> implements PositionSequenceBuilder<P> {

    final private PositionSequenceBuilder<P> delegate;
    final private int dim;
    int num = 0;

    public CountingPositionSequenceBuilder(CoordinateReferenceSystem<P> crs) {
        delegate = PositionSequenceBuilders.variableSized(crs.getPositionClass());
        dim = crs.getCoordinateDimension();
    }

    @Override
    public PositionSequenceBuilder<P> add(double... coordinates) {
        num++;
        return delegate.add(coordinates);
    }

    public PositionSequenceBuilder<P> add(P position) {
        num++;
        return delegate.add(position);
    }

    @Override
    public PositionSequence<P> toPositionSequence() {
        return delegate.toPositionSequence();
    }


    @Override
    public int getNumAdded(){
        return num;
    }

    public int getCoordinateDimension() {
        return dim;
    }
}
