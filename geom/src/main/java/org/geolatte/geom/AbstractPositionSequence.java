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

package org.geolatte.geom;

import java.io.Serializable;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
abstract class AbstractPositionSequence<P extends Position> implements PositionSequence<P>, Serializable {

    private static final long serialVersionUID = 6884205871950410216L;

    private final PositionFactory<P> factory;

    public AbstractPositionSequence(PositionFactory<P> factory) {
        this.factory = factory;
    }

    @SuppressWarnings("unchecked")
    public Class<P> getPositionClass() {
        return factory.forClass();
    }

    @Override
    public PositionFactory<P> getPositionFactory() {
        return factory;
    }

    @Override
    public P getPositionN(int index) {
        double[] co = new double[getCoordinateDimension()];
        getCoordinates(index, co);
        return factory.mkPosition(co);
    }

    @Override
    public int getCoordinateDimension() {
        return this.factory.getCoordinateDimension();
    }

    @Override
    public abstract PositionSequence<P> clone();

    @Override
    abstract public void accept(PositionVisitor<P> visitor);

}
