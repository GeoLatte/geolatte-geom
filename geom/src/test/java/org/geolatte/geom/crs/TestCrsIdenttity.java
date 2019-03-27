package org.geolatte.geom.crs;

import org.geolatte.geom.G2D;
import org.geolatte.geom.G2DM;
import org.geolatte.geom.G3D;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.geolatte.geom.crs.CoordinateSystemAxisDirection.*;
import static org.geolatte.geom.crs.Unit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/2018.
 */
public class TestCrsIdenttity {

    @Test
    public void testIdentGeographic2D() {
        Geographic2DCoordinateReferenceSystem crs1 = getGeographic2DCoordinateReferenceSystem();
        GeographicCoordinateReferenceSystem<G2D> crs2 = getGeographic2DCoordinateReferenceSystem();

        assertEquals(crs1, crs2);

    }

    @Test
    public void testIdentGeographic3DMConstructors() {
        GeographicCoordinateReferenceSystem<G3D> crs1 = getG3DGeographicCoordinateReferenceSystem();
        Geographic3DCoordinateReferenceSystem crs2 = getG3DGeographicCoordinateReferenceSystem();
        assertEquals(crs1, crs2);
    }

    @Test
    public void testDiffGeogarphic3DMConstructors() {
        GeographicCoordinateReferenceSystem<G3D> crs1 = getG3DGeographicCoordinateReferenceSystem();
        Geographic3DCoordinateReferenceSystem crs2 = getG3DGeographicCoordinateReferenceSystemAlt();
        assertTrue(!crs1.equals(crs2));
    }

    @Test
    public void testIdentGeocentric() {
        GeocentricCartesianCoordinateReferenceSystem gcc1 = getGeocentricCartesianCoordinateReferenceSystem();
        GeocentricCartesianCoordinateReferenceSystem gcc2 = getGeocentricCartesianCoordinateReferenceSystem();
        assertEquals(gcc1, gcc2);
    }

    @Test
    public void testDiffGeocentric() {
        Ellipsoid ecs = new Ellipsoid(CrsId.parse("EPSG:201"), "ellipsoid", 60000.1, 0.123);
        Datum datum = new Datum(CrsId.parse("EPSG:5"), ecs, "datum1", new double[]{1.0, 3.0, 4.0});
        PrimeMeridian primeMeridian = new PrimeMeridian(CrsId.parse("EPSG:51"), "pmem", 4.0);
        GeocentricCartesianCoordinateReferenceSystem gcc1 = getGeocentricCartesianCoordinateReferenceSystem();
        GeocentricCartesianCoordinateReferenceSystem gcc2 = getGeocentricCartesianCoordinateReferenceSystem(datum, primeMeridian);
        assertTrue(!gcc1.equals(gcc2));
    }


    @Test
    public void testIdentLinearCrs() {
        LinearCoordinateReferenceSystem lrs1 = getLinearCoordinateReferenceSystem();
        LinearCoordinateReferenceSystem lrs2 = getLinearCoordinateReferenceSystem();
        assertEquals(lrs1, lrs2);
    }

    @Test
    public void testdiffLinearCrs() {
        LinearCoordinateReferenceSystem lrs1 = getLinearCoordinateReferenceSystem();
        LinearCoordinateReferenceSystem lrs2 = getLinearCoordinateReferenceSystem(UNKNOWN_LINEAR);
        assertTrue(!lrs1.equals(lrs2));
    }

    @Test
    public void testVerticalCrs() {
        VerticalCoordinateReferenceSystem vcrs1 = getVerticalCoordinateReferenceSystem();
        VerticalCoordinateReferenceSystem vcrs2 = getVerticalCoordinateReferenceSystem();
        assertEquals(vcrs1, vcrs2);

    }

    @Test
    public void testProjectedCrs() {
        ProjectedCoordinateReferenceSystem pcrs1 = getProjectedCoordinateReferenceSystem();
        ProjectedCoordinateReferenceSystem pcrs2 = getProjectedCoordinateReferenceSystem();
        assertEquals(pcrs1, pcrs2);
    }

    @Test
    public void testcompoundG2DCRS() {
        CompoundCoordinateReferenceSystem<G2DM> cplx1 = getCompoundGeodeticCoordinateReferenceSystem();
        CompoundCoordinateReferenceSystem<G2DM> cplx2 = getCompoundGeodeticCoordinateReferenceSystem();
        assertEquals(cplx1, cplx2);
    }

    @Test
    public void testcompoundDiffG2DCRS() {
        CompoundCoordinateReferenceSystem<G2DM> cplx1 = getCompoundGeodeticCoordinateReferenceSystem();
        CompoundCoordinateReferenceSystem<G2DM> cplx2 = getCompoundGeodeticCoordinateReferenceSystemAlt();
        assertTrue(!cplx1.equals(cplx2));
    }


    private Geographic2DCoordinateReferenceSystem getGeographic2DCoordinateReferenceSystem() {
        EllipsoidalCoordinateSystem2D ecs = new EllipsoidalCoordinateSystem2D(new GeodeticLongitudeCSAxis("ax", DEGREE),
                new GeodeticLatitudeCSAxis("bx", DEGREE));

        return new Geographic2DCoordinateReferenceSystem(CrsId.parse("EPSG:200"), "test", ecs);
    }

    private Geographic3DCoordinateReferenceSystem getG3DGeographicCoordinateReferenceSystem() {
        EllipsoidalCoordinateSystem3D ecs = new EllipsoidalCoordinateSystem3D(new GeodeticLongitudeCSAxis("ax", DEGREE),
                new GeodeticLatitudeCSAxis("bx", DEGREE),
                new VerticalStraightLineAxis("height", METER)
        );

        return new Geographic3DCoordinateReferenceSystem(CrsId.parse("EPSG:200"), "test", ecs);
    }

    private Geographic3DCoordinateReferenceSystem getG3DGeographicCoordinateReferenceSystemAlt() {
        EllipsoidalCoordinateSystem3D ecs = new EllipsoidalCoordinateSystem3D(new GeodeticLongitudeCSAxis("Ax", DEGREE),
                new GeodeticLatitudeCSAxis("bx", DEGREE),
                new VerticalStraightLineAxis("height", METER)
        );

        return new Geographic3DCoordinateReferenceSystem(CrsId.parse("EPSG:200"), "test", ecs);
    }

    private GeocentricCartesianCoordinateReferenceSystem getGeocentricCartesianCoordinateReferenceSystem() {
        Ellipsoid ecs = new Ellipsoid(CrsId.parse("EPSG:20"), "ellipsoid", 60000.1, 0.123);
        Datum datum = new Datum(CrsId.parse("EPSG:5"), ecs, "datum1", new double[]{1.0, 3.0, 4.0});
        PrimeMeridian primeMeridian = new PrimeMeridian(CrsId.parse("EPSG:51"), "pmem", 4.0);
        return getGeocentricCartesianCoordinateReferenceSystem(datum, primeMeridian);
    }

    private GeocentricCartesianCoordinateReferenceSystem getGeocentricCartesianCoordinateReferenceSystem(Datum datum, PrimeMeridian primeMeridian) {

        CartesianCoordinateSystem3D crs = new CartesianCoordinateSystem3D(
                new StraightLineAxis("x", GeocentricX, METER),
                new StraightLineAxis("y", GeocentricY, METER),
                new StraightLineAxis("z", GeocentricZ, METER)
        );
        return new GeocentricCartesianCoordinateReferenceSystem(
                CrsId.parse("EPSG:100"), "3d name", datum, primeMeridian, crs);
    }


    private LinearCoordinateReferenceSystem getLinearCoordinateReferenceSystem(LinearUnit unit) {
        return new LinearCoordinateReferenceSystem("dist", new MeasureStraightLineAxis("dist", unit));
    }

    private LinearCoordinateReferenceSystem getLinearCoordinateReferenceSystem() {
        return getLinearCoordinateReferenceSystem(METER);
    }


    private VerticalCoordinateReferenceSystem getVerticalCoordinateReferenceSystem() {
        VerticalDatum vdatum = new VerticalDatum(CrsId.parse("EPSG:1"), "vdat", 1, new Extension("key", "val"));
        return new VerticalCoordinateReferenceSystem(CrsId.parse("EPSG:-1"), "elevation", vdatum, new VerticalStraightLineAxis("h", UP, METER));
    }

    private ProjectedCoordinateReferenceSystem getProjectedCoordinateReferenceSystem() {

        Projection projection = new Projection(CrsId.parse("EPSG:200"), "some projection");

        List<CrsParameter> parameters = Arrays.asList(new CrsParameter("foo", 1.0), new CrsParameter("bar", 0.33333333));
        return new ProjectedCoordinateReferenceSystem(CrsId.parse("EPSG:1000"), "projsys", getGeographic2DCoordinateReferenceSystem(), projection, parameters, getCartesian2DCRS(), new Extension("key", "value"));
    }

    private CartesianCoordinateSystem2D getCartesian2DCRS() {
        return new CartesianCoordinateSystem2D(new StraightLineAxis("X", EAST, METER), new StraightLineAxis("Y", NORTH, METER));
    }


    private CompoundCoordinateReferenceSystem<G2DM> getCompoundGeodeticCoordinateReferenceSystem() {
        return new CompoundCoordinateReferenceSystem<>(CrsId.parse("EPSG:101"),
                "cplx", getGeographic2DCoordinateReferenceSystem(),
                getVerticalCoordinateReferenceSystem(),
                getLinearCoordinateReferenceSystem());
    }

    private CompoundCoordinateReferenceSystem<G2DM> getCompoundGeodeticCoordinateReferenceSystemAlt() {
        return new CompoundCoordinateReferenceSystem<>(CrsId.parse("EPSG:101"),
                "cplx",
                getGeographic2DCoordinateReferenceSystem(),
                getVerticalCoordinateReferenceSystem(),
                getLinearCoordinateReferenceSystem(UNKNOWN_LINEAR));
    }

}
