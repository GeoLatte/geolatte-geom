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
 * Copyright (C) 2010 - 2014 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/18/14
 */
@SuppressWarnings("unchecked")
public class DefaultGeometryOperationsFactory {

    final static private Map map = new HashMap(7);

    //TODO --- G* and GC* classes can't have JTSGeometryOperations ==> define NoOpsGeometryOperations, and GeographicGeometryOperations
    static {
        map.put(G2D.class, new JTSGeometryOperations<G2D>());
        map.put(G3D.class, new JTSGeometryOperations<G3D>());
        map.put(G2DM.class, new JTSGeometryOperations<G2DM>());
        map.put(G3DM.class, new JTSGeometryOperations<G3DM>());
        map.put(GC3D.class, new JTSGeometryOperations<GC3D>());
        map.put(P2D.class, new JTSGeometryOperations<P2D>());
        map.put(P3D.class, new JTSGeometryOperations<P3D>());
        map.put(P2DM.class, new JTSGeometryOperations<P2DM>());
        map.put(P3DM.class, new JTSGeometryOperations<P3DM>());
    }

    public static <P extends Position<P>> GeometryOperations<P> getOperations(Class<? extends P> pType) {
        return (GeometryOperations<P>) map.get(pType);
    }
}
