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

package org.geolatte.geom.curve;

import junit.framework.Assert;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.P2D;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.junit.Test;

/**
 * Unit test for {@link MortonContext}
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/21/13
 */
public class MortonContextTest {

    CoordinateReferenceSystem<P2D> crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);

    P2D p0 = new P2D(0, 0);
    P2D p1 = new P2D(10, 10);
    Envelope<P2D> envelope = new Envelope<>(p0, p1, crs);

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorThrowsIllegalArgumentIfLevelExceedsLimit() {
        new MortonContext<>(envelope, 32);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorThrowsIllegalArgumentExcOnNullExtent() {
        new MortonContext<P2D>(null, 4);
    }

    /**
     * Tests the properties of a MortonContext.
     */
    @Test
    public void testProperties() {
        MortonContext<P2D> context = new MortonContext<>(envelope, 3);

        Assert.assertEquals(p0.getX(), context.getMinX());
        Assert.assertEquals(p0.getY(), context.getMinY());
        Assert.assertEquals(p1.getX(), context.getMaxX());
        Assert.assertEquals(p1.getY(), context.getMaxY());
        Assert.assertEquals(3, context.getDepth());
        Assert.assertEquals(3, context.getDepth());
        Assert.assertEquals(1.25, context.getLeafHeight());
        Assert.assertEquals(1.25, context.getLeafWidth());
    }

}
