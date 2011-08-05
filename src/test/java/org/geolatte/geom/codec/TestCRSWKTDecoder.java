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

import org.geolatte.geom.crs.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class TestCRSWKTDecoder {

    private static final String WKT_4326 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";

    //this is not a real EPSG entry, but modified to have a testcase for optional entities
    private static final String WKT_4326_SPHEROID_NO_AUTHORITY = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"4326\"]]";


    private static final String WKT_31370 = "PROJCS[\"Belge 1972 / Belgian Lambert 72\",GEOGCS[\"Belge 1972\",DATUM[\"Reseau_National_Belge_1972\",SPHEROID[\"International 1924\",6378388,297,AUTHORITY[\"EPSG\",\"7022\"]],TOWGS84[106.869,-52.2978,103.724,-0.33657,0.456955,-1.84218,1],AUTHORITY[\"EPSG\",\"6313\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4313\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Lambert_Conformal_Conic_2SP\"],PARAMETER[\"standard_parallel_1\",51.16666723333333],PARAMETER[\"standard_parallel_2\",49.8333339],PARAMETER[\"latitude_of_origin\",90],PARAMETER[\"central_meridian\",4.367486666666666],PARAMETER[\"false_easting\",150000.013],PARAMETER[\"false_northing\",5400088.438],AUTHORITY[\"EPSG\",\"31370\"],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH]]";


    @Test
    public void testDecodeWGS84() {
        CRSWKTDecoder decoder = new CRSWKTDecoder();
        CoordinateReferenceSystem system = decoder.decode(WKT_4326);
        assertNotNull(system);
        assertTrue (system instanceof GeographicCoordinateReferenceSystem);
        GeographicCoordinateReferenceSystem geoCRS = (GeographicCoordinateReferenceSystem)system;
        assertEquals("WGS 84", geoCRS.getName());

        //verify the datum
        GeodeticDatum datum = geoCRS.getDatum();
        assertEquals("WGS_1984", datum.getName());
        assertEquals(6326, datum.getSRID());

        //verify the ellipsoid
        Ellipsoid ellipsoid = datum.getEllipsoid();
        assertEquals("WGS 84", ellipsoid.getName());
        assertEquals(6378137, ellipsoid.getSemiMajorAxis(), Math.ulp(100));
        assertEquals(298.257223563, ellipsoid.getInverseFlattening(), Math.ulp(100));
        assertEquals(7030, ellipsoid.getSRID());

        //verify the prime meridian
        assertEquals(new PrimeMeridian(8901, "Greenwich", 0d), geoCRS.getPrimeMeridian());

        //verify the angular units
        assertTrue(geoCRS.getUnit().isAngular());
        assertEquals(0.01745329251994328, geoCRS.getUnit().getConversionFactor(), Math.ulp(1));
        assertEquals(Unit.DEGREE,geoCRS.getUnit());

        //verify the Axis
        assertEquals(new CoordinateSystemAxis("Lon", CoordinateSystemAxisDirection.EAST, Unit.DEGREE), geoCRS.getAxes()[0]);
        assertEquals(new CoordinateSystemAxis("Lat", CoordinateSystemAxisDirection.NORTH, Unit.DEGREE), geoCRS.getAxes()[1]);

        //verify the srid code
        assertEquals(4326, geoCRS.getSRID());

    }

    @Test
    public void testDecodeWGS84SpheroidNoAuthority() {
        CRSWKTDecoder decoder = new CRSWKTDecoder();
        CoordinateReferenceSystem system = decoder.decode(WKT_4326_SPHEROID_NO_AUTHORITY);
        assertNotNull(system);
        assertTrue (system instanceof GeographicCoordinateReferenceSystem);
        GeographicCoordinateReferenceSystem geoCRS = (GeographicCoordinateReferenceSystem)system;

        //verify the ellipsoid
        Ellipsoid ellipsoid = geoCRS.getDatum().getEllipsoid();
        assertEquals("WGS 84", ellipsoid.getName());
        assertEquals(6378137, ellipsoid.getSemiMajorAxis(), Math.ulp(100));
        assertEquals(298.257223563, ellipsoid.getInverseFlattening(), Math.ulp(100));
        assertEquals(-1, ellipsoid.getSRID());

        //verify the Axis
        assertEquals(new CoordinateSystemAxis("Easting", CoordinateSystemAxisDirection.EAST, Unit.DEGREE), geoCRS.getAxes()[0]);
        assertEquals(new CoordinateSystemAxis("Northing", CoordinateSystemAxisDirection.NORTH, Unit.DEGREE), geoCRS.getAxes()[1]);


    }

    //TODO -- test TOWGS84 on datum (datum for lambert72)
}
