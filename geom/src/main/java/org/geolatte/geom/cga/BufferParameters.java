package org.geolatte.geom.cga;

import java.util.Objects;

/**
 * The style of a {@link Buffer}: how arcs are approximated, the shape of the ends of lines and of the outer corners,
 * and how much the input may be simplified. These are the parameters of JTS {@code BufferParameters} and of the style
 * parameters of PostGIS {@code ST_Buffer}.
 *
 * <p>Instances are immutable; the {@code with...} methods return a modified copy:</p>
 * <pre>{@code
 * BufferParameters p = BufferParameters.DEFAULT.withEndCap(EndCap.FLAT).withJoin(Join.MITRE);
 * }</pre>
 */
public final class BufferParameters {

    /**
     * The shape of the buffer at the ends of a line
     */
    public enum EndCap {
        /**
         * a half circle around the end point
         */
        ROUND,
        /**
         * cut off square at the end point
         */
        FLAT,
        /**
         * a half square around the end point: the line is extended by the buffer distance
         */
        SQUARE
    }

    /**
     * The shape of the buffer at the outer side of a corner
     */
    public enum Join {
        /**
         * an arc around the corner
         */
        ROUND,
        /**
         * the offset lines are extended until they meet, but not further than the mitre limit from the corner
         */
        MITRE,
        /**
         * the ends of the offset lines are connected by a straight segment
         */
        BEVEL
    }

    /**
     * 8 segments per quadrant, round end caps and joins, a mitre limit of 5 and a simplify factor of 0.01: the
     * defaults of JTS and PostGIS
     */
    public static final BufferParameters DEFAULT = new BufferParameters(8, EndCap.ROUND, Join.ROUND, 5.0, 0.01);

    /**
     * The largest simplify factor. Up to it, the parts that simplification cuts off a polygon stay within the
     * buffer of the simplified boundary, whatever the number of segments per quadrant.
     */
    public static final double MAX_SIMPLIFY_FACTOR = 0.1;

    private final int quadrantSegments;
    private final EndCap endCap;
    private final Join join;
    private final double mitreLimit;
    private final double simplifyFactor;

    private BufferParameters(int quadrantSegments, EndCap endCap, Join join, double mitreLimit,
                             double simplifyFactor) {
        if (quadrantSegments < 1) {
            throw new IllegalArgumentException("The number of segments per quadrant must be positive: "
                    + quadrantSegments);
        }
        if (!(mitreLimit > 0) || mitreLimit == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("The mitre limit must be positive and finite: " + mitreLimit);
        }
        if (!(simplifyFactor >= 0 && simplifyFactor <= MAX_SIMPLIFY_FACTOR)) {
            throw new IllegalArgumentException("The simplify factor must be from 0 to " + MAX_SIMPLIFY_FACTOR + ": "
                    + simplifyFactor);
        }
        this.quadrantSegments = quadrantSegments;
        this.endCap = Objects.requireNonNull(endCap);
        this.join = Objects.requireNonNull(join);
        this.mitreLimit = mitreLimit;
        this.simplifyFactor = simplifyFactor;
    }

    /**
     * @return the number of segments that approximate a quarter circle
     */
    public int getQuadrantSegments() {
        return quadrantSegments;
    }

    public EndCap getEndCap() {
        return endCap;
    }

    public Join getJoin() {
        return join;
    }

    /**
     * @return the largest distance of a mitre point from its corner, as a multiple of the buffer distance
     */
    public double getMitreLimit() {
        return mitreLimit;
    }

    /**
     * The tolerance of the simplification of the input, as a fraction of the absolute buffer distance. With round
     * joins, {@link Buffer} drops the vertices that lie within that tolerance h of a segment between the vertices
     * it keeps (exactly decided), which can make it much faster on input with many vertices, at the cost of an
     * error of at most 2h. The result still lies within the exact buffer. With mitre or bevel joins the input is
     * not simplified.
     *
     * @return the tolerance of the simplification, as a fraction of the buffer distance
     */
    public double getSimplifyFactor() {
        return simplifyFactor;
    }

    /**
     * @param quadrantSegments the number of segments that approximate a quarter circle, at least 1
     * @return these parameters, with the given number of segments per quadrant
     * @throws IllegalArgumentException if the number is less than 1
     */
    public BufferParameters withQuadrantSegments(int quadrantSegments) {
        return new BufferParameters(quadrantSegments, endCap, join, mitreLimit, simplifyFactor);
    }

    /**
     * @return these parameters, with the given end cap
     */
    public BufferParameters withEndCap(EndCap endCap) {
        return new BufferParameters(quadrantSegments, endCap, join, mitreLimit, simplifyFactor);
    }

    /**
     * @return these parameters, with the given join
     */
    public BufferParameters withJoin(Join join) {
        return new BufferParameters(quadrantSegments, endCap, join, mitreLimit, simplifyFactor);
    }

    /**
     * @param mitreLimit the largest distance of a mitre point from its corner, as a multiple of the buffer
     *                   distance; positive and finite
     * @return these parameters, with the given mitre limit
     * @throws IllegalArgumentException if the limit is not positive, or not finite
     */
    public BufferParameters withMitreLimit(double mitreLimit) {
        return new BufferParameters(quadrantSegments, endCap, join, mitreLimit, simplifyFactor);
    }

    /**
     * @param simplifyFactor the tolerance of the simplification of the input, as a fraction of the absolute buffer
     *                       distance; from 0 (no simplification) to {@link #MAX_SIMPLIFY_FACTOR}
     * @return these parameters, with the given simplify factor
     * @throws IllegalArgumentException if the factor is negative, NaN or larger than the maximum
     * @see #getSimplifyFactor()
     */
    public BufferParameters withSimplifyFactor(double simplifyFactor) {
        return new BufferParameters(quadrantSegments, endCap, join, mitreLimit, simplifyFactor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BufferParameters)) return false;
        BufferParameters that = (BufferParameters) o;
        return quadrantSegments == that.quadrantSegments && endCap == that.endCap && join == that.join
                && Double.compare(mitreLimit, that.mitreLimit) == 0
                && Double.compare(simplifyFactor, that.simplifyFactor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quadrantSegments, endCap, join, mitreLimit, simplifyFactor);
    }

    @Override
    public String toString() {
        return "BufferParameters[quadrantSegments=" + quadrantSegments + ", endCap=" + endCap + ", join=" + join
                + ", mitreLimit=" + mitreLimit + ", simplifyFactor=" + simplifyFactor + "]";
    }
}
