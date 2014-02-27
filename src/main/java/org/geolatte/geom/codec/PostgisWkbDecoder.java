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


import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LengthUnit;

/**
 * A Wkb Decoder for PostGIS EWKB
 * <p>This WKBDecoder supports the EWKB dialect of PostGIS versions 1.0 tot 1.5+.</p>
 * <p>This implementation is not thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Nov 11, 2010
 */
class PostgisWkbDecoder extends AbstractWkbDecoder {


    @Override
    protected void prepare(ByteBuffer byteBuffer) {
        //do nothing
    }


    @Override
    protected void readCrs(ByteBuffer byteBuffer, int typeCode) {
        //don't override a CRS, once set.
        if (getCoordinateReferenceSystem() != null) return;

                CoordinateReferenceSystem < ?>crs;
        if (hasSrid(typeCode)) {
            int srid = byteBuffer.getInt();
            crs = CrsRegistry.getCoordinateRefenceSystemForEPSG(srid,
                    CrsRegistry.getUndefinedProjectedCoordinateReferenceSystem());

        } else {
            crs = CrsRegistry.getUndefinedProjectedCoordinateReferenceSystem();
        }
        boolean hasM = (typeCode & PostgisWkbTypeMasks.M_FLAG) == PostgisWkbTypeMasks.M_FLAG;
        boolean hasZ = (typeCode & PostgisWkbTypeMasks.Z_FLAG) == PostgisWkbTypeMasks.Z_FLAG;

        //TODO -- this creates a new CRS for each non-2D geometry!!
        //TODO -- memoize compoundCrs methods
        if (hasZ) {
            crs = crs.addVerticalAxis(LengthUnit.METER);
        }
        if (hasM) {
            crs = crs.addMeasureAxis(LengthUnit.METER);
        }
        setCoordinateReferenceSystem(crs);
    }

    @Override
    protected boolean hasSrid(int typeCode) {
        return (typeCode & PostgisWkbTypeMasks.SRID_FLAG) == PostgisWkbTypeMasks.SRID_FLAG;
    }

}
