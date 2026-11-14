package org.geolatte.geom;

import java.util.Objects;
import java.util.Optional;

/**
 * The intersection of two line segments: its kind, and the common point or the common segment.
 *
 * <p>Instances are immutable.</p>
 *
 * @param <P> the type of the {@code Position}s
 * @see LineSegment#intersection(LineSegment)
 */
public final class SegmentIntersectionResult<P extends Position> {

    private static final SegmentIntersectionResult<?> DISJOINT =
            new SegmentIntersectionResult<>(SegmentIntersection.DISJOINT, null, null);

    private final SegmentIntersection kind;
    private final P point;
    private final LineSegment<P> overlap;

    private SegmentIntersectionResult(SegmentIntersection kind, P point, LineSegment<P> overlap) {
        this.kind = kind;
        this.point = point;
        this.overlap = overlap;
    }

    /**
     * @return the result for segments that have no point in common
     */
    @SuppressWarnings("unchecked")
    public static <P extends Position> SegmentIntersectionResult<P> disjoint() {
        return (SegmentIntersectionResult<P>) DISJOINT;
    }

    /**
     * @param point the (rounded) crossing point
     * @return the result for segments that properly cross
     */
    public static <P extends Position> SegmentIntersectionResult<P> crossing(P point) {
        return new SegmentIntersectionResult<>(SegmentIntersection.CROSSING, Objects.requireNonNull(point), null);
    }

    /**
     * @param point the common point (an endpoint of one of the segments)
     * @return the result for segments that touch
     */
    public static <P extends Position> SegmentIntersectionResult<P> touching(P point) {
        return new SegmentIntersectionResult<>(SegmentIntersection.TOUCHING, Objects.requireNonNull(point), null);
    }

    /**
     * @param overlap the segment that the segments have in common
     * @return the result for collinear segments that overlap
     */
    public static <P extends Position> SegmentIntersectionResult<P> overlapping(LineSegment<P> overlap) {
        return new SegmentIntersectionResult<>(SegmentIntersection.OVERLAPPING, null, Objects.requireNonNull(overlap));
    }

    /**
     * @return the kind of intersection
     */
    public SegmentIntersection kind() {
        return kind;
    }

    /**
     * @return the common point if the kind is {@link SegmentIntersection#CROSSING} or
     * {@link SegmentIntersection#TOUCHING}, otherwise empty
     */
    public Optional<P> point() {
        return Optional.ofNullable(point);
    }

    /**
     * @return the common segment if the kind is {@link SegmentIntersection#OVERLAPPING}, otherwise empty
     */
    public Optional<LineSegment<P>> overlap() {
        return Optional.ofNullable(overlap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SegmentIntersectionResult)) return false;
        SegmentIntersectionResult<?> other = (SegmentIntersectionResult<?>) o;
        return kind == other.kind
                && Objects.equals(point, other.point)
                && (overlap == null ? other.overlap == null : other.overlap != null
                && overlap.getStartPosition().equals(other.overlap.getStartPosition())
                && overlap.getEndPosition().equals(other.overlap.getEndPosition()));
    }

    @Override
    public int hashCode() {
        int h = kind.hashCode();
        h = 31 * h + Objects.hashCode(point);
        if (overlap != null) {
            h = 31 * h + overlap.getStartPosition().hashCode();
            h = 31 * h + overlap.getEndPosition().hashCode();
        }
        return h;
    }

    @Override
    public String toString() {
        switch (kind) {
            case CROSSING:
            case TOUCHING:
                return kind + " " + point;
            case OVERLAPPING:
                return kind + " " + overlap.getStartPosition() + " - " + overlap.getEndPosition();
            default:
                return kind.toString();
        }
    }
}
