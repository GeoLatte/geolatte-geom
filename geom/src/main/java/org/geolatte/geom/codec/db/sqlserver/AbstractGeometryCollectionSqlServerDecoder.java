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

import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractGeometryCollectionSqlServerDecoder extends AbstractSqlServerDecoder {

    @Override
    protected OpenGisType getOpenGisType() {
        return OpenGisType.GEOMETRYCOLLECTION;
    }

    @Override
    protected Geometry<?> createNullGeometry(CoordinateReferenceSystem<?> crs) {
        return createGeometry( new ArrayList<Geometry<?>>(), null);
    }

    @Override
    protected Geometry<?> createGeometry(SqlServerGeometry nativeGeom) {
        return createGeometry(nativeGeom, 0);
    }

    @Override
    protected Geometry<?> createGeometry(SqlServerGeometry nativeGeom, int shapeIndex) {
        int startChildIdx = shapeIndex + 1;
        List<Geometry<?>> geometries = new ArrayList<Geometry<?>>(nativeGeom.getNumShapes());
        for (int childIdx = startChildIdx; childIdx < nativeGeom.getNumShapes(); childIdx++) {
            if (!nativeGeom.isParentShapeOf(shapeIndex, childIdx)) {
                continue;
            }
            AbstractSqlServerDecoder decoder = (AbstractSqlServerDecoder) Decoders.decoderFor(
                    nativeGeom.getOpenGisTypeOfShape(
                            childIdx
                    )
            );
            Geometry<?> geometry = decoder.createGeometry(nativeGeom, childIdx);
            geometries.add(geometry);
        }
        return createGeometry(geometries, nativeGeom);
    }

    abstract protected Geometry<?> createGeometry(List geometries, SqlServerGeometry nativeGeom);


}
