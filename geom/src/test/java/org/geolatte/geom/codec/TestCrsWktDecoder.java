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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class TestCrsWktDecoder {

    private static final String WKT_4326 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";

    //this is not a real EPSG entry, but modified to have a testcase for optional entities
    private static final String WKT_4326_SPHEROID_NO_AUTHORITY = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"4326\"]]";

    //Lambert 72
    private static final String WKT_31370 = "PROJCS[\"Belge 1972 / Belgian Lambert 72\",GEOGCS[\"Belge 1972\",DATUM[\"Reseau_National_Belge_1972\",SPHEROID[\"International 1924\",6378388,297,AUTHORITY[\"EPSG\",\"7022\"]],TOWGS84[-106.8686,52.2978,-103.7239,0.3366,-0.457,1.8422,-1.2747],AUTHORITY[\"EPSG\",\"6313\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4313\"]],PROJECTION[\"Lambert_Conformal_Conic_2SP\"],PARAMETER[\"standard_parallel_1\",51.16666723333333],PARAMETER[\"standard_parallel_2\",49.8333339],PARAMETER[\"latitude_of_origin\",90],PARAMETER[\"central_meridian\",4.367486666666666],PARAMETER[\"false_easting\",150000.013],PARAMETER[\"false_northing\",5400088.438],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH],AUTHORITY[\"EPSG\",\"31370\"]]";

    private static final String WKT_3031 = "PROJCS[\"WGS 84 / Antarctic Polar Stereographic\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Polar_Stereographic\"],PARAMETER[\"latitude_of_origin\",-71],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"3031\"]]";

    private static final String WKT_3409 = "PROJCS[\"unnamed\",GEOGCS[\"unnamed ellipse\",DATUM[\"unknown\",SPHEROID[\"unnamed\",6371228,0]],PRIMEM[\"Greenwich\",0],UNIT[\"degree\",0.0174532925199433]],PROJECTION[\"Lambert_Azimuthal_Equal_Area\"],PARAMETER[\"latitude_of_center\",-90],PARAMETER[\"longitude_of_center\",0],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"Meter\",1],AUTHORITY[\"EPSG\",\"3409\"]]";

    private static final String WKT_6190 = "COMPD_CS[\"Belge 1972 / Belgian Lambert 72 + Ostend height\",PROJCS[\"Belge 1972 / Belgian Lambert 72\",GEOGCS[\"Belge 1972\",DATUM[\"Reseau_National_Belge_1972\",SPHEROID[\"International 1924\",6378388,297,AUTHORITY[\"EPSG\",\"7022\"]],TOWGS84[-106.8686,52.2978,-103.7239,0.3366,-0.457,1.8422,-1.2747],AUTHORITY[\"EPSG\",\"6313\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4313\"]],PROJECTION[\"Lambert_Conformal_Conic_2SP\"],PARAMETER[\"standard_parallel_1\",51.16666723333333],PARAMETER[\"standard_parallel_2\",49.8333339],PARAMETER[\"latitude_of_origin\",90],PARAMETER[\"central_meridian\",4.367486666666666],PARAMETER[\"false_easting\",150000.013],PARAMETER[\"false_northing\",5400088.438],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH],AUTHORITY[\"EPSG\",\"31370\"]],VERT_CS[\"Ostend height\",VERT_DATUM[\"Ostend\",2005,AUTHORITY[\"EPSG\",\"5110\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP],AUTHORITY[\"EPSG\",\"5710\"]],AUTHORITY[\"EPSG\",\"6190\"]]";

    private static final String WKT_5710 = "VERT_CS[\"Ostend height\",VERT_DATUM[\"Ostend\",2005,AUTHORITY[\"EPSG\",\"5110\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP],AUTHORITY[\"EPSG\",\"5710\"]]";

    private static final String WKT_WITH_EXTENSION = "COMPD_CS[\"WGS 84 / World Mercator +  EGM2008 height\",PROJCS[\"WGS 84 / World Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"3395\"]],VERT_CS[\"EGM2008 geoid height\",VERT_DATUM[\"EGM2008 geoid\",2005,EXTENSION[\"PROJ4_GRIDS\",\"egm08_25.gtx\"],AUTHORITY[\"EPSG\",\"1027\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP],AUTHORITY[\"EPSG\",\"3855\"]],AUTHORITY[\"EPSG\",\"6893\"]]";

    private static final String WKT_PROJCS_WITH_EXTENSION = "PROJCS[\"WGS 84 / Pseudo-Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH],EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs\"],AUTHORITY[\"EPSG\",\"3857\"]]";

    private static final String WKT_WITH_EXT_2 = "COMPD_CS[\"WGS 84 / Pseudo-Mercator +  EGM2008 geoid height\",PROJCS[\"WGS 84 / Pseudo-Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH],EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs\"],AUTHORITY[\"EPSG\",\"3857\"]],VERT_CS[\"EGM2008 geoid height\",VERT_DATUM[\"EGM2008 geoid\",2005,EXTENSION[\"PROJ4_GRIDS\",\"egm08_25.gtx\"],AUTHORITY[\"EPSG\",\"1027\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP],AUTHORITY[\"EPSG\",\"3855\"]],AUTHORITY[\"EPSG\",\"6871\"]]";

    private static final String WKT_4362 = "GEOCCS[\"NAD83(HARN) (geocentric)\",DATUM[\"NAD83_High_Accuracy_Reference_Network\",SPHEROID[\"GRS 1980\",6378137,298.257222101,AUTHORITY[\"EPSG\",\"7019\"]],AUTHORITY[\"EPSG\",\"6152\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Geocentric X\",OTHER],AXIS[\"Geocentric Y\",OTHER],AXIS[\"Geocentric Z\",NORTH],AUTHORITY[\"EPSG\",\"4362\"]]";

    @Test
    public void testDecodeWGS84() {
        CrsWktDecoder decoder = new CrsWktDecoder();
        CoordinateReferenceSystem system = decoder.decode(WKT_4326, 4326);
        assertNotNull(system);
        assertTrue(system instanceof Geographic2DCoordinateReferenceSystem);
        Geographic2DCoordinateReferenceSystem geoCRS = (Geographic2DCoordinateReferenceSystem) system;
        assertEquals("WGS 84", geoCRS.getName());

        //verify the datum
        Datum datum = geoCRS.getDatum();
        assertEquals("WGS_1984", datum.getName());
        assertEquals(6326, datum.getCrsId().getCode());

        //verify the ellipsoid
        Ellipsoid ellipsoid = datum.getEllipsoid();
        assertEquals("WGS 84", ellipsoid.getName());
        assertEquals(6378137, ellipsoid.getSemiMajorAxis(), Math.ulp(100));
        assertEquals(298.257223563, ellipsoid.getInverseFlattening(), Math.ulp(100));
        assertEquals(7030, ellipsoid.getCrsId().getCode());

        //verify the prime meridian
        assertEquals(new PrimeMeridian(CrsId.parse("8901"), "Greenwich", 0d), geoCRS.getPrimeMeridian());

        //verify the angular units
        assertTrue(geoCRS.getUnit().isAngular());
        assertEquals(0.01745329251994328, geoCRS.getUnit().getConversionFactor(), Math.ulp(1));
        assertEquals(Unit.DEGREE, geoCRS.getUnit());

        //verify the Axis
        assertEquals(new GeodeticLongitudeCSAxis("Lon", Unit.DEGREE), geoCRS.getAxis(0));
        assertEquals(new GeodeticLatitudeCSAxis("Lat", Unit.DEGREE), geoCRS.getAxis(1));

        //verify the srid code
        assertEquals(4326, geoCRS.getCrsId().getCode());

    }

    @Test
    public void testDecodeWGS84SpheroidNoAuthority() {
        CrsWktDecoder decoder = new CrsWktDecoder();
        CoordinateReferenceSystem system = decoder.decode(WKT_4326_SPHEROID_NO_AUTHORITY, 4326);
        assertNotNull(system);
        assertTrue(system instanceof Geographic2DCoordinateReferenceSystem);
        Geographic2DCoordinateReferenceSystem geoCRS = (Geographic2DCoordinateReferenceSystem) system;

        //verify the ellipsoid
        Ellipsoid ellipsoid = geoCRS.getDatum().getEllipsoid();
        assertEquals("WGS 84", ellipsoid.getName());
        assertEquals(6378137, ellipsoid.getSemiMajorAxis(), Math.ulp(100));
        assertEquals(298.257223563, ellipsoid.getInverseFlattening(), Math.ulp(100));
        assertEquals(-1, ellipsoid.getCrsId().getCode());

        //verify the Axis
        assertEquals(new GeodeticLongitudeCSAxis("Easting", Unit.DEGREE), geoCRS.getAxis(0));
        assertEquals(new GeodeticLatitudeCSAxis("Northing", Unit.DEGREE), geoCRS.getAxis(1));

        assertEquals(4326, geoCRS.getCrsId().getCode());


    }

    @Test
    public void testDecodeLambert72() {
        CrsWktDecoder decoder = new CrsWktDecoder();
        CoordinateReferenceSystem<?> system = decoder.decode(WKT_31370, 31370);
        assertNotNull(system);
        assertTrue(system instanceof ProjectedCoordinateReferenceSystem);
        ProjectedCoordinateReferenceSystem projCRS = (ProjectedCoordinateReferenceSystem) system;

        //check the geo-CrsRegistry
        assertEquals(4313, projCRS.getGeographicCoordinateSystem().getCrsId().getCode());
        GeographicCoordinateReferenceSystem geoCRS = projCRS.getGeographicCoordinateSystem();
        double[] expected = new double[]{-106.8686, 52.2978, -103.7239, 0.3366, -0.457, 1.8422, -1.2747};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], geoCRS.getDatum().getToWGS84()[i], Math.ulp(100d));
        }

        //check the projection
        assertEquals(new Projection(CrsId.UNDEFINED, "Lambert_Conformal_Conic_2SP"), projCRS.getProjection());
        List<CrsParameter> parameters = projCRS.getParameters();
        List<CrsParameter> expectedParameters = new ArrayList<>();
        expectedParameters.add(new CrsParameter("standard_parallel_1", 51.16666723333333));
        expectedParameters.add(new CrsParameter("standard_parallel_2", 49.8333339));
        expectedParameters.add(new CrsParameter("latitude_of_origin", 90));
        expectedParameters.add(new CrsParameter("central_meridian", 4.367486666666666));
        expectedParameters.add(new CrsParameter("false_easting", 150000.013));
        expectedParameters.add(new CrsParameter("false_northing", 5400088.438));
        assertArrayEquals(expectedParameters.toArray(), parameters.toArray());

        //check the authority
        assertEquals(31370, projCRS.getCrsId().getCode());

        //check the axes
        assertEquals(new StraightLineAxis("X", CoordinateSystemAxisDirection.EAST, Unit.METER), projCRS.getCoordinateSystem().getAxis(0));
        assertEquals(new StraightLineAxis("Y", CoordinateSystemAxisDirection.NORTH, Unit.METER), projCRS.getCoordinateSystem().getAxis(1));

    }

    @Test
    public void testDecodeWKT3031() {
        CrsWktDecoder decoder = new CrsWktDecoder();
        ProjectedCoordinateReferenceSystem system = (ProjectedCoordinateReferenceSystem) decoder.decode(WKT_3031, 3031);
        assertNotNull(system);
        assertEquals(new StraightLineAxis("Easting", CoordinateSystemAxisDirection.EAST, Unit.METER), system.getCoordinateSystem().getAxis(0));
        assertEquals(new StraightLineAxis("Northing", CoordinateSystemAxisDirection.NORTH, Unit.METER), system.getCoordinateSystem().getAxis(1));
    }


    @Test
    public void testDecodeWKT3409() {
        CrsWktDecoder decoder = new CrsWktDecoder();
        ProjectedCoordinateReferenceSystem system = (ProjectedCoordinateReferenceSystem) decoder.decode(WKT_3409, 3409);
        assertNotNull(system);
//        assertEquals(new CoordinateSystemAxis("Easting", CoordinateSystemAxisDirection.UNKNOWN_LINEAR, Unit.METER) , system.getDimensionalFlag().getAxis(0));
//        assertEquals(new CoordinateSystemAxis("Northing", CoordinateSystemAxisDirection.UNKNOWN_LINEAR, Unit.METER) , system.getDimensionalFlag().getAxis(1));
    }

    @Test
    public void testVertCS() {
        CrsWktDecoder decoder = new CrsWktDecoder();
        VerticalCoordinateReferenceSystem system = (VerticalCoordinateReferenceSystem) decoder.decode(WKT_5710, 5710);
        assertNotNull(system);
        assertEquals(2005, system.getDatum().getDatumType());
        assertEquals(LinearUnit.METER, system.getUnit());
        assertEquals(5710, system.getCrsId().getCode());
    }

    @Test
    public void testDecodeCompoundCS() {

        CrsWktDecoder decoder = new CrsWktDecoder();
        CompoundCoordinateReferenceSystem<?> system = (CompoundCoordinateReferenceSystem<?>) decoder.decode(WKT_6190, 6190);
        assertNotNull(system);
        assertEquals(new CrsId("EPSG", 6190), system.getCrsId());
        CoordinateReferenceSystem<?> lambert = new CrsWktDecoder().decode(WKT_31370, 31370);
        CoordinateReferenceSystem<?> vertical = new CrsWktDecoder().decode(WKT_5710, 5710);
        assertEquals(lambert, system.headCs());
        assertEquals(vertical, system.lastCs());


    }

    @Test
    public void testDecodePROJCSWithExtension(){
        CrsWktDecoder decoder = new CrsWktDecoder();
        ProjectedCoordinateReferenceSystem system = (ProjectedCoordinateReferenceSystem) decoder.decode(WKT_PROJCS_WITH_EXTENSION, 3857);
        assertNotNull(system);
        assertEquals("PROJ4", system.getExtension().getName());
        assertEquals("+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs", system.getExtension().getValue());
    }

    @Test
    public void testDecodeCompoundCSWithExtension() {

        CrsWktDecoder decoder = new CrsWktDecoder();
        CompoundCoordinateReferenceSystem<?> system = (CompoundCoordinateReferenceSystem<?>) decoder.decode(WKT_WITH_EXTENSION, 6893);
        assertNotNull(system);

        CompoundCoordinateReferenceSystem<?> system2 = (CompoundCoordinateReferenceSystem<?>) decoder.decode(WKT_WITH_EXT_2, 6893);
        assertNotNull(system2);
    }

    @Test
    public void testDecode4362(){
        CrsWktDecoder decoder = new CrsWktDecoder();
        GeocentricCartesianCoordinateReferenceSystem system = (GeocentricCartesianCoordinateReferenceSystem) decoder.decode(WKT_4362, 4362);
        assertNotNull(system);
        assertEquals("NAD83(HARN) (geocentric)", system.getName());
        assertEquals("GRS 1980", system.getDatum().getEllipsoid().getName());
        assertEquals(0.0, system.getPrimeMeridian().getLongitude(), 0.00001);
    }



}
