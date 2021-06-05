package org.geolatte.geom;

/**
 * Factory methods for Geometry Operations instances.
 */
public class GeometryOperations {

    /**
     * Create an instance of {@code ProjectedGeometryOperations}
     *
     * @return
     */
    public static ProjectedGeometryOperations projectedGeometryOperations() {
        return ProjectedGeometryOperations.Default;
    }

    /**
     * Create an instance of {@code MeasureGeometryOperations}
     *
     * @return
     */
    public static MeasureGeometryOperations measureGeometryOperations() {
        return MeasureGeometryOperations.Default;
    }

}
