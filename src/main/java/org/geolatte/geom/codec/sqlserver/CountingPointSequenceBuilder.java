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

package org.geolatte.geom.codec.sqlserver;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CrsId;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 10/19/12
 */
public class CountingPointSequenceBuilder implements PointSequenceBuilder {

    final private PointSequenceBuilder delegate;
    private int num = 0;

    public CountingPointSequenceBuilder(DimensionalFlag df, CrsId crsId) {
        delegate = PointSequenceBuilders.variableSized(df, crsId);
    }

    @Override
    public PointSequenceBuilder add(double[] coordinates) {
        num++;
        return delegate.add(coordinates);
    }

    @Override
    public PointSequenceBuilder add(double x, double y) {
        num++;
        return delegate.add(x, y);
    }

    @Override
    public PointSequenceBuilder add(double x, double y, double zOrm) {
        num++;
        return delegate.add(x, y, zOrm);
    }

    @Override
    public PointSequenceBuilder add(double x, double y, double z, double m) {
        num++;
        return delegate.add(x, y, z, m);
    }

    @Override
    public PointSequenceBuilder add(Point pnt) {
        num++;
        return delegate.add(pnt);
    }

    @Override
    public DimensionalFlag getDimensionalFlag() {
        return delegate.getDimensionalFlag();
    }

	@Override
	public CrsId getCrsId() {
		return delegate.getCrsId();
	}

	@Override
    public PointSequence toPointSequence() {
        return delegate.toPointSequence();
    }

    public int getNumAdded(){
        return num;
    }
}
