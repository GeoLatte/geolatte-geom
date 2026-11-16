package org.geolatte.geom.cga;

import org.geolatte.geom.*;

/**
 * The reverse of a geometry: the same geometry with its positions in reverse order.
 *
 * <p>A point is returned as it is, and a line string (or linear ring) gets its positions in reverse order. A polygon
 * keeps its exterior ring first and its interior rings in their order, each ring with its positions in reverse
 * order, so that the polygon stays the same (its rings change orientation). A collection gets its elements in
 * reverse order, each element reversed, so that the positions of the collection as a whole are in reverse order. An
 * empty geometry is returned as it is.</p>
 */
public final class Reverse {

    private Reverse() {
    }

    /**
     * @param geometry the geometry to reverse
     * @return a geometry of the same type and coordinate reference system, with the positions in reverse order
     */
    @SuppressWarnings("unchecked")
    public static <P extends Position, G extends Geometry<P>> G of(G geometry) {
        if (geometry.isEmpty() || geometry instanceof Point) return geometry;
        if (geometry instanceof LineString) {
            return (G) Geometries.mkGeometry(geometry.getClass(), geometry.getPositions().reverse(),
                    geometry.getCoordinateReferenceSystem());
        }
        if (geometry instanceof Polygon) {
            Polygon<P> polygon = (Polygon<P>) geometry;
            LinearRing<P>[] rings = polygon.components();
            for (int i = 0; i < rings.length; i++) rings[i] = of(rings[i]);
            return (G) new Polygon<>(rings);
        }
        AbstractGeometryCollection<P, ?> collection = (AbstractGeometryCollection<P, ?>) geometry;
        Geometry<P>[] elements = collection.components();
        int n = elements.length;
        Geometry<P>[] reversed = elements.clone();
        for (int i = 0; i < n; i++) reversed[i] = of(elements[n - 1 - i]);
        return (G) Geometries.mkGeometry(geometry.getClass(), reversed);
    }
}
