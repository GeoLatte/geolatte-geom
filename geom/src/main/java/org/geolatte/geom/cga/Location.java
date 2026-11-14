package org.geolatte.geom.cga;

/**
 * The topological location of a point relative to a geometry: in its interior, on its boundary, or in its exterior.
 *
 * @see IntersectionMatrix
 * @see PointLocator
 */
public enum Location {
    INTERIOR,
    BOUNDARY,
    EXTERIOR
}
