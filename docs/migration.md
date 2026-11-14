# Migrating from version 1

This guide lists the changes in behaviour between version 1 of `geolatte-geom` (the `main` branch, 1.13.x) and
this version, which adds exact computational geometry (see [Exact computational geometry](exact-cga.md)). Most
code keeps working without changes, but some results differ, and one interface has new methods.

## Checklist

1. If you implement `ProjectedGeometryOperations` yourself, add the six new methods
   ([details](#projectedgeometryoperations-has-new-methods)).
2. The default `ProjectedGeometryOperations` is no longer `JTSGeometryOperations`. Predicates, `isSimple`,
   `boundary`, `convexHull` and `centroid` are now exact or correctly rounded, and give different results in
   some cases
   ([details](#the-default-projectedgeometryoperations-changed)). To keep the old results, use
   `new JTSGeometryOperations()` explicitly.
3. `LineString.isRing()` now also requires the line string to be simple
   ([details](#linestringisring-requires-simplicity)). Use `isClosed()` for the old meaning.
4. The predicates of `NumericalMethods` are exact, and reject NaN and infinite coordinates
   ([details](#numericalmethods-is-exact)).

## `ProjectedGeometryOperations` has new methods

The interface has six new abstract methods:

| Method | Meaning |
|---|---|
| `within(a, b)` | `a` lies within `b` |
| `disjoint(a, b)` | `a` and `b` have no point in common (the negation of `intersects`) |
| `equalsTopologically(a, b)` | `a` and `b` are the same point set; two empty geometries are equal |
| `covers(a, b)` | no point of `b` lies in the exterior of `a` |
| `coveredBy(a, b)` | no point of `a` lies in the exterior of `b` |
| `isValid(g)` | `g` is well formed (`ST_IsValid`); an empty geometry is valid |

It also has a new default method, `exactArea(g)`: the area, correctly rounded (the double nearest to the exact
area), about ten times slower than `area`. Implementations inherit it.

The six abstract methods only break classes that implement the interface. Callers are not affected. Both
implementations in the library, `DefaultProjectedGeometryOperations` and `JTSGeometryOperations`, have them. A
custom implementation can extend either one, or implement the methods itself.

## The default `ProjectedGeometryOperations` changed

`ProjectedGeometryOperations.Default`, and so `GeometryOperations.projectedGeometryOperations()`, is now a
`DefaultProjectedGeometryOperations`. In version 1 it was a `JTSGeometryOperations`, which delegates everything to
JTS.

The new default computes these operations exactly, with the classes in `org.geolatte.geom.cga`:

- the predicates `intersects`, `disjoint`, `touches`, `crosses`, `within`, `contains`, `overlaps`, `covers`,
  `coveredBy`, `equalsTopologically` and `relates`, with `Relate`;
- `isSimple`, with `Simplicity`;
- `isValid`, with `Validity`;
- `boundary`, with `Boundary`;
- `convexHull`, with `ConvexHull`;
- `centroid`, correctly rounded, with `Centroid`.

`area` is computed with `Area.fast`, in plain floating point arithmetic, as fast as JTS and with the same kind
of rounding errors. The results can differ from those of JTS in the last bits. The new `exactArea` returns the
correctly rounded area.

`reverse`, `distance`, `buffer`, `intersection`, `union`, `difference`, `symmetricDifference` and `length` are
still delegated to JTS, and behave as before.

`JTSGeometryOperations` itself is unchanged, apart from the new methods. To get the version 1 behaviour for all
operations, use it directly:

```java
ProjectedGeometryOperations ops = new JTSGeometryOperations();
```

The sections below describe where the new default gives different results.

### Predicates are exact

The predicates are decided exactly for the coordinates as given: no intersection point is rounded to decide the
topology. JTS `RelateOp`, used in version 1, rounds intersection points. It agrees with the exact result on
well-behaved data, but can differ:

- when vertices lie very close to (within a few ulps of) other segments or vertices, or segments cross very
  close to a vertex;
- for line strings that intersect themselves.

For example, the interiors of these two multi line strings share a segment. The exact DE-9IM matrix is
`101F00102`; JTS `RelateOp` computes `001F00102`:

```
MULTILINESTRING ((1 4, 2 5, 0 1, 2 3), (3 5, 5 5, 1 4, 3 4))
MULTILINESTRING ((6 2, 2 2, 1 1), (1 4, 5 5))
```

Unchanged from version 1:

- A predicate with an empty operand is false, except `disjoint`, and `equalsTopologically`, which is true for two
  empty geometries.
- Geometries with different coordinate reference systems are rejected with an `IllegalArgumentException`.

New: a NaN or infinite coordinate is rejected with an `IllegalArgumentException`. In version 1, JTS returned an
unspecified result.

### Geometry collections have union semantics

A geometry is now the union of its elements. This applies to `GeometryCollection`s with members of different
dimensions, and to members that overlap or touch:

- adjacent or overlapping polygons merge into one area, so a shared edge lies in the interior;
- lines and points that lie in an area are part of its interior;
- points that lie on a line are part of the line.

This is the semantics of the JTS 1.20 `RelateNG` engine, and coincides with the usual definition for geometries
that are valid by the Simple Features rules. Version 1 used JTS `RelateOp`, which has no union semantics: there,
the shared edge of two adjacent polygons in a collection is part of the boundary.

```java
Geometry<C2D> twoSquares = Wkt.fromWkt("GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), "
        + "POLYGON ((2 0, 4 0, 4 2, 2 2, 2 0)))", crs);
Geometry<C2D> line = Wkt.fromWkt("LINESTRING (1 1, 3 1)", crs);   // crosses the shared edge

ops.contains(twoSquares, line);   // version 1: false; now: true
```

The dimension of a collection, used by `crosses` and `overlaps`, is the highest dimension of its members, as
before.

### `convexHull` returns a counterclockwise polygon

Every turn of the convex hull is now decided with an exact orientation test. As before, the vertices are input
positions, with their Z and M values.

- The polygon is now **counterclockwise**, starting at the vertex with the smallest X (then Y) coordinate. JTS
  returned it clockwise. Code that depends on the order or orientation of the hull's vertices must be adapted;
  topological comparisons are not affected.
- As before, the result is an empty `GeometryCollection` for an empty geometry, a `Point` if all positions
  coincide, and a `LineString` between the two extreme positions if all positions are collinear.

```
convexHull(MULTIPOINT ((0 0), (4 0), (2 1), (4 4), (0 4)))
  version 1: POLYGON ((0 0, 0 4, 4 4, 4 0, 0 0))
  now:       POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0))
```

### `boundary` orders the endpoints of a multi line string differently

The result of `boundary` has the same type and the same points as in version 1, with one difference: the boundary
of a `MultiLineString` lists its points in the order in which they first occur in the line strings. JTS sorted
them by X and then Y.

```
boundary(MULTILINESTRING ((5 5, 1 1), (3 3, 0 0)))
  version 1: MULTIPOINT ((0 0), (1 1), (3 3), (5 5))
  now:       MULTIPOINT ((5 5), (1 1), (3 3), (0 0))
```

The boundary of a heterogeneous `GeometryCollection` is still rejected with an `IllegalArgumentException` (with a
different message).

### `isSimple` detects overlapping elements of a multi line string

`isSimple` is exact. Version 1 used the deprecated `org.locationtech.jts.operation.IsSimpleOp`, which misses
elements of a `MultiLineString` that overlap another element completely (a duplicate, possibly reversed, or with
repeated positions). Such a multi line string is not simple: its elements meet in more than their endpoints.

```
isSimple(MULTILINESTRING ((3 1, 0 1), (3 3, 0 2, 1 3), (3 1, 0 1)))
  version 1: true
  now:       false
```

In a comparison on 20,000 random small line strings, multi line strings and multipoints, this was the only kind of
difference. As before, repeated consecutive positions are
ignored, and polygons are simple if their rings are simple.

### `centroid` is correctly rounded

The centroid of a polygonal geometry is now the double nearest to the exact value. JTS computes it in floating
point, so results can differ in the last bits; for nearly degenerate polygons (slivers) they can differ more, and
the JTS result is then the inaccurate one. The definition is unchanged: holes subtract from the area whatever the
orientation of the rings, and a polygon with zero area has the centroid of its rings.

The point returned by `centroid` has changed type:

- It now has the coordinate reference system of the geometry, with NaN for the coordinates other than X and Y.
  In version 1 it was a 2D point in the coordinate reference system registered for the SRID, even when the
  geometry had Z or M coordinates. Such a point was typed as `Point<P>` but held `C2D` positions, so that, for
  example, reading its Z coordinate as `C3D` failed with a `ClassCastException`.

```
centroid(POLYGON Z ((0 0 1, 4 0 1, 4 4 1, 0 4 1, 0 0 1)))   (SRID 31370 with a vertical axis)
  version 1: SRID=31370;POINT(2 2)       a C2D position
  now:       SRID=31370;POINT(2 2 NaN)   a C3D position
```

### `isValid` is new

`isValid` follows the Simple Features rules, as JTS `IsValidOp` does, and is exact. `Validity.of(geometry)` also
reports the first problem found and its location.

## `LineString.isRing()` requires simplicity

`LineString.isRing()` now follows `ST_IsRing`: a ring is a line string that is closed **and simple**. In version 1
it only checked that the line string was closed and not empty, so a closed line string that crosses itself was a
ring. It now returns false for such a line string, and for one with a NaN or infinite coordinate.

```java
LineString<C2D> bowTie = (LineString<C2D>) Wkt.fromWkt("LINESTRING (0 0, 2 2, 2 0, 0 2, 0 0)", crs);
bowTie.isRing();     // version 1: true; now: false
bowTie.isClosed();   // true, as before
```

Use `isClosed()` where only closure matters. `isRing()` now tests all segment pairs, which takes O(n log n) time
in the number of positions, where it took constant time before.

## `NumericalMethods` is exact

`NumericalMethods.orientation`, `collinear`, `orient2d` and `isCounterClockwise` now use exact predicates (see
`cga.exact.Predicates`). In version 1 they used floating point determinants of the raw coordinates, which often got
the result wrong for nearly collinear positions with large coordinates (about 77% wrong in a test with projected
coordinates near (150000, 200000)). The results only change where the old ones were wrong.

- A NaN or infinite coordinate is now rejected with an `IllegalArgumentException`. In version 1, `orientation`
  then returned `Clockwise`, `collinear` returned false, and `orient2d` returned NaN, which made
  `isCounterClockwise` return false.
- `orient2d` returns -1, 0 or 1, also for very large coordinates, where the old computation could overflow.
- `isCounterClockwise` still throws an `IllegalArgumentException` for a ring with zero area.

These methods are also used by the Oracle SDO encoder (to orient the rings of polygons) and by the linearization of
circular arcs. Both now orient nearly degenerate rings and arcs correctly.

## New API

The following is new and does not change existing behaviour:

- `org.geolatte.geom.cga.exact`:
  - `Predicates`: exact orientation, in-circle, segment intersection, distance comparisons and ring orientation;
  - `Constructions`: correctly rounded segment intersections and circumcenters;
  - `Summation`: exact sums of (products of) doubles, with correctly rounded values and quotients;
  - `PolygonMoments`: the correctly rounded area and centroid of polygon rings;
  - building blocks: `Interval`, `ExactSum`, `Essa`.
- `org.geolatte.geom.cga`:
  - `Relate`, `PreparedRelate`, `IntersectionMatrix`, `Location` and `PointLocator`;
  - `Simplicity`, `Validity` and `Boundary`;
  - `Area` and `Centroid`;
  - `ConvexHull`, `DouglasPeucker` (optionally preserving topology), `DelaunayTriangulation` and
    `VoronoiDiagram`.
- `LineSegment.classifyIntersection`, `intersects` and `intersection`, with the new `SegmentIntersection` and
  `SegmentIntersectionResult`.

The dependencies are unchanged.
