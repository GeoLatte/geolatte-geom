package org.geolatte.geom.jts;


import org.geolatte.geom.Simple;
import org.locationtech.jts.geom.*;

/**
 * Some utility functions for working with JTS geometries
 */
public class JTSUtils {

    private JTSUtils() {
    }

    /**
     * Determines equality between geometries taking into
     * account all coordinates, and the SRID.
     * <p>
     * Note that doubles are compared exactly, i.e. _not_ to without some error-bound.
     * <p>
     * This method is used e.g. for dirty-checking in Hibernate
     *
     * @param g1 first geometry to compare (may be null)
     * @param g2 second geometry to compare (may be null)
     * @return if g1 is exactly equal to g2
     */
    static public boolean equalsExact3D(Geometry g1, Geometry g2) {
        if (g1 == g2) return true;
        if (g1 == null || g2 == null) return false;
        if (!g1.getGeometryType().equals(g2.getGeometryType())) return false;
        if (g1.getSRID() != g2.getSRID()) return false;

        //empty geometries of the same type are the same
        if (g1.isEmpty() && g2.isEmpty()) return true;

        int ng1 = g1.getNumGeometries();
        int ng2 = g2.getNumGeometries();
        if (ng1 != ng2) return false;

        if ((g1 instanceof GeometryCollection)) {
            return equalComponentGeometries(g1, g2, ng1);
        }
        return equals3DPrimitiveGeometries(g1, g2);
    }

    private static boolean equalComponentGeometries(Geometry g1, Geometry g2, int ng1) {
        for (int gIdx = 0; gIdx < ng1; gIdx++) {
            if (!equalsExact3D(g1.getGeometryN(gIdx), g2.getGeometryN(gIdx))) {
                return false;
            }
        }
        return true;
    }

    static public boolean equals3D(Coordinate c1, Coordinate c2) {
        return c1.x == c2.x && c1.y == c2.y &&
                ((Double.isNaN(c1.z) && Double.isNaN(c2.z)) || c1.z == c2.z) &&
                ((Double.isNaN(c1.getM()) && Double.isNaN(c2.getM())) || c1.getM() == c2.getM());
    }

    private static boolean equalLineStringCoordinates(LineString g1, LineString g2) {
        int np1 = g1.getNumPoints();
        int np2 = g2.getNumPoints();
        if (np1 != np2) return false;
        for (int i = 0; i < np1; i++) {
            if (!equalsExact3D(g1.getPointN(i), g2.getPointN(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalPolygonCoordinates(Polygon g1, Polygon g2) {
        int nr1 = g1.getNumInteriorRing();
        int nr2 = g2.getNumInteriorRing();
        if (nr1 != nr2) return false;
        for (int i = 0; i < nr1; i++) {
            if (!equalLineStringCoordinates(g1.getInteriorRingN(i), g2.getInteriorRingN(i))) {
                return false;
            }
        }
        return equalLineStringCoordinates(g1.getExteriorRing(), g2.getExteriorRing());
    }

    private static boolean equals3DPrimitiveGeometries(Geometry g1, Geometry g2) {
        //this method assumes that g1 and g2 are of the same type
        assert (g1.getClass().equals(g2.getClass()));
        if (g1 instanceof Point) {
            return equals3D(g1.getCoordinate(), g2.getCoordinate());
        }

        if (g1 instanceof LineString) {
            return equalLineStringCoordinates((LineString) g1, (LineString) g2);
        }

        if (g1 instanceof Polygon) {
            return equalPolygonCoordinates((Polygon) g1, (Polygon) g2);
        }
        throw new IllegalStateException("Only simple geometries should be used");
    }
}
