# Exact computational geometry

The `org.geolatte.geom.cga.exact` package and the algorithms in `org.geolatte.geom.cga` built on it compute
geometric results exactly for double precision coordinates. The methods follow H. Ratschek and J. Rokne,
*Geometric Computations with Interval and New Robust Methods* (Horwood, 2003), complemented by Shewchuk's
adaptive expansion arithmetic and double-word arithmetic (Joldes, Muller & Popescu).

Two kinds of guarantee apply:

- **Predicates are exact.** The result is the correct sign for the input doubles taken as exact real numbers.
- **Constructions are correctly rounded.** A constructed point is generally not representable as a double, so
  each coordinate is the nearest double to the exact value (round half to even). Because that result is unique,
  it does not depend on argument order or orientation.

Every computation is staged: a cheap floating point filter decides almost all cases, and progressively more
expensive exact stages are only used for the inputs the filter cannot decide.

## `cga.exact.Predicates` — exact signs

All predicates take either raw `double` coordinates or `Position`s.

| Method | Returns | Stages |
|---|---|---|
| `orientation(a, b, c)` | sign of the orientation determinant (-1, 0, 1) | error-bound filter → interval → ESSA → BigDecimal |
| `inCircle(a, b, c, d)` | whether d is inside, on or outside the circle through a, b, c | Shewchuk error bound → exact expansions → interval → ESSA (384 terms) → BigDecimal |
| `ringOrientation(PositionSequence)`, `ringOrientation(xs, ys, n)` | orientation of a ring | exact signed area |
| `segmentIntersection(a, b, c, d)` | `DISJOINT`, `CROSSING`, `TOUCHING` or `OVERLAPPING` | the four orientations plus the collinear ordering |
| `compareDistanceToSegment(a, b, p, q)` | whether p or q is nearer to segment ab | exact |
| `compareDistanceToSegment(a, b, p, double d)` | how the distance from p to ab compares with d | exact |
| `squaredDistanceToSegment(...)` | an `Interval` enclosing the squared distance | interval arithmetic |
| `squaredDistancesToSegment(...)` | the same enclosure for many points at once | fast bulk filter |
| `dotSign(...)` | exact sign of a dot product | exact |
| `crossProductSign(a, b, c, d)` | sign of (b − a) × (d − c): compares two directions | error-bound filter → interval → ESSA → BigDecimal |
| `orientationOfCrossing(a, b, p, q, r, w)` | the side of line ab on which the crossing point of lines pq and rw lies, without constructing it | sign shortcut → interval → ESSA (≤ 576 terms) → BigDecimal |

## `cga.exact.Constructions` — correctly rounded points

- **`intersection(LineSegment, LineSegment)`** returns a `SegmentIntersectionResult` with the kind of
  intersection and the intersection point or overlap.
  - A crossing point is correctly rounded. A double-word enclosure decides almost every case; the rest go to an
    ESSA midpoint test and, rarely, an exact BigInteger quotient.
  - Touching points and overlap endpoints are existing endpoints and are returned exactly.
  - Z and M are interpolated along the receiving segment.
  - The XY result is bit-identical if the segments are swapped or reversed, so all segments crossing at the
    same exact point get the same rounded point. The rounded point lies in both segments' envelopes, but is in
    general not exactly on either segment (it is off by at most ½ ulp per axis).
- **`crossingPoint(ax, ay, bx, by, cx, cy, dx, dy)`** is the raw form of the crossing computation.
- **`circumcenter(ax, ay, bx, by, cx, cy)`** returns the circumcenter of a triangle as `double[2]`, correctly
  rounded. A double-word stage decides practically every case (about 80 ns per triangle); the remainder is
  computed exactly with BigInteger arithmetic.

The same segment operations are available on `LineSegment` itself: `classifyIntersection`, `intersects` and
`intersection`.

## Building blocks in `cga.exact`

- **`Interval`**: interval arithmetic with directed rounding (`of`, `difference`, `product`, the arithmetic
  operations, `signum`, and directed `addUp`/`addDown`-style operations).
- **`Essa.sign(double...)`**: the exact sign of a sum of doubles (ESSA, Ratschek & Rokne Ch. 4).
- **`ExactSum`**: represents sums of products of doubles exactly as lists of doubles, so that ESSA can be
  applied.
- **`Expansion`** (package-private): Shewchuk's expansion arithmetic, used in the `inCircle` stage.

## Algorithms in `cga`

### `ConvexHull`

- `vertices(Iterable)` returns the hull vertices in counterclockwise order, starting at the lowest (x, y)
  position.
- `of(Geometry)` returns the hull as a geometry: an empty `GeometryCollection` for an empty geometry, a `Point`
  if all positions coincide, a `LineString` between the two extreme positions if all are collinear, and a
  counterclockwise `Polygon` otherwise.
- Andrew's monotone chain with the Akl–Toussaint prefilter; every turn is decided exactly.

### `DouglasPeucker`

- `keptPositions(PositionSequence, tolerance)` and `simplify(...)` for `PositionSequence`, `LineString`,
  `MultiLineString`, `Polygon` and `MultiPolygon`. All distance comparisons are exact (Ratschek & Rokne,
  Algorithm 24). Rings that collapse are dropped.
- `simplify(geometry, tolerance, preserveTopology)` optionally preserves topology: a run of positions is only
  replaced by a segment if no intersections are created or removed and no other part of the geometry lies
  between the removed part and the new segment. A valid geometry remains valid, and rings are never removed.

### `DelaunayTriangulation`

- `of(Geometry)` or `of(Iterable, crs)`.
- Results as `getVertices()`, `getNumTriangles()`, `getTriangleIndices()`, `getEdgeIndices()`,
  `getTriangles()` (a `GeometryCollection` of polygons) and `getEdges()` (a `MultiLineString`).
- Incremental insertion with Lawson flips, in Hilbert order with a stochastic walk. The triangulation is exact,
  and symbolic perturbation makes the result for cocircular points canonical (independent of insertion order).

### `VoronoiDiagram`

- `of(Geometry)`, `of(Geometry, Envelope)` or `of(DelaunayTriangulation, Envelope)`.
- `getSites()`, `getCells()` (one cell per site, an empty polygon if the cell lies outside the clip envelope)
  and `getDiagram()`.
- The default clip envelope is the envelope of the sites, expanded by its diameter.
- Cell vertices are correctly rounded circumcenters, and adjacent cells share bit-identical vertices.

### `Relate`, `IntersectionMatrix` and `PointLocator`

- `Relate.relate(a, b)` returns the DE-9IM `IntersectionMatrix`; `relate(a, b, pattern)` matches it against a
  pattern (`T`, `F`, `*`, `0`, `1`, `2`).
- The named predicates `equalsTopologically`, `disjoint`, `intersects`, `touches`, `crosses`, `within`,
  `contains`, `overlaps`, `covers` and `coveredBy` are static methods of `Relate`, with envelope prefilters, and
  are also available on an `IntersectionMatrix` (`isTouches`, `isCrosses`, ...).
- `PointLocator.of(geometry).locate(x, y)` returns the `Location` (`INTERIOR`, `BOUNDARY` or `EXTERIOR`) of a
  point.
- `DefaultProjectedGeometryOperations`, the default `ProjectedGeometryOperations`, computes its spatial
  predicates with `Relate` and its convex hull with `ConvexHull`.

**Union semantics.** A geometry is the union of its elements: points, lines and polygons, also as members of a
`GeometryCollection`, and also when members overlap or touch. The location of a point is decided by the elements
of the highest dimension that contain it:

1. polygons: a point is in the interior if it lies in the interior of the union of the polygons, also when it
   lies on the boundaries of polygons that together cover a neighbourhood of it (an edge shared by adjacent
   polygons, a hole filled by another polygon); otherwise it is on the boundary if it lies on a polygon;
2. lines: a point on a line is on the boundary if it is an endpoint of an odd number of lines (the Mod-2 rule,
   applied literally: an endpoint of one line that lies in the interior of another line is still on the
   boundary), and in the interior otherwise;
3. points: a point of the geometry is in the interior.

For valid Simple Features geometries this is the usual definition. Only the individual polygons need to be valid.

**Algorithm.** The result is exact for the input doubles: no coordinate is ever rounded to decide the topology.

- All segments of both geometries are put into one arrangement. Candidate pairs come from a sweep over the
  segment envelopes, and are classified with `segmentIntersection`.
- The nodes are the vertices, the points, and the crossing points. A crossing point is represented implicitly by
  its two segments; its correctly rounded value is only used as a hash key to find nodes that may coincide, and
  coincidence is confirmed exactly with `orientation` and `orientationOfCrossing`.
- The nodes on a segment are ordered exactly: vertices by coordinate, a vertex and a crossing with
  `orientation`, two crossings with `orientationOfCrossing`. The pieces between consecutive nodes are the
  edges; overlapping pieces are merged.
- The edges around each node are sorted by angle with `crossProductSign`.
- Each face gets a depth per geometry: the number of polygons that cover it. Per connected component, the depth
  of the face to the left of its lexicographically smallest vertex comes from an exact point-in-polygon test;
  the other depths follow by walking around the nodes and along the edges, adding +1 or −1 when crossing a ring.
- The locations of the nodes (dimension 0), the edges (dimension 1) and the faces beside each edge (dimension 2)
  in both geometries give the matrix.

`PointLocator` uses exact crossing parity per polygon, on an interval index over the ring segments, and for a
point on polygon boundaries the same angular ordering to decide whether the polygons cover all of its
neighbourhood.

## Performance

Indicative timings for 100,000 points:

| Operation | Time |
|---|---|
| Delaunay, uniform random points | ≈ 50 ms |
| Delaunay, grid with or without noise | ≈ 60–70 ms |
| Delaunay, points on a circle (all cocircular) | ≈ 100 ms |
| Voronoi diagram | ≈ 115–130 ms |

Indicative timings for `Relate.relate`:

| Operation | Time |
|---|---|
| two overlapping polygons, 10,000 vertices each | ≈ 15 ms |
| two overlapping polygons, 100,000 vertices each | ≈ 0.6 s |
| 10,000 line segments against a polygon of 2,000 vertices | ≈ 20 ms |

## Roadmap: the Simple Features operations

The goal is to implement the 2D geometry operations and predicates of the Simple Features Access specification
(and `ST_IsValid` from SQL/MM) on the exact foundation above.

There are two kinds of operation, with different guarantees:

- **Predicates** (the spatial relations, `IsSimple`, `IsValid`) depend only on the input coordinates. They can
  be made fully exact: no intersection point ever needs to be constructed.
- **Constructions** (overlay, buffer, area, distance, ...) return new coordinates or numbers that must be
  rounded. For each one the guarantee has to be defined: correct rounding where the result is rational,
  faithful rounding otherwise, and a valid result for geometry-valued operations.

### Status per operation

| Operation | Exact implementation | What is needed |
|---|---|---|
| `Dimension`, `Envelope`, `IsEmpty`, `StartPoint`, `EndPoint`, `IsClosed` | combinatorial or exact equality | — |
| `Boundary` | to do | combinatorial (Mod-2 rule); no arithmetic |
| `IsSimple`, `IsRing` | to do | all segment intersections within one geometry; the arrangement of the relate engine provides them |
| `Equals`, `Disjoint`, `Intersects`, `Touches`, `Crosses`, `Within`, `Contains`, `Overlaps`, `Relate` (and `Covers`, `CoveredBy`) | **done** | `Relate`, with union semantics |
| `IsValid` (SQL/MM) | to do | the arrangement of the relate engine, plus a check that the polygon interior is connected |
| `ConvexHull` | **done** | — |
| `Area` | to do | correctly rounded value of an exact sum (primitive 5); the area is rational |
| `Centroid` | to do | correctly rounded quotient of two exact sums (primitive 5); the centroid is rational |
| `PointOnSurface` | to do | scan line, ordering of implicit x-coordinates, exact check that the result is inside |
| `Distance` | to do | exact selection of the minimum over segment pairs, then a correctly rounded √(N/D) (primitive 6) |
| `Length` | to do | a sum of square roots: faithful rounding (≤ 1 ulp) rather than correct rounding |
| `Intersection`, `Union`, `Difference`, `SymDifference` | to do | noding, graph labelling, polygon assembly, and snap rounding of the output (primitive 7) |
| `Buffer` | to do | offset curves followed by a union; the result is an approximation by definition |
| `LocateAlong`, `LocateBetween` | existing | could use correctly rounded interpolation; low priority |

### Primitives

Primitives 1–4 are done: they make up the relate engine.

1. **Ordering of implicit intersection points** (done): `orientationOfCrossing` compares a crossing point with
   a line, which orders crossings along a segment and decides whether two crossings coincide. Crossing points
   are never constructed to decide topology.
2. **A public point locator** (done): `PointLocator`, with union semantics and the Mod-2 rule.
3. **Angular ordering of edges around a node** (done): every edge lies on an original segment, so this reduces
   to the half plane, from exact coordinate comparisons, and `crossProductSign`.
4. **Noding of two geometries** (done): the arrangement and depth labelling of `Relate`.
5. **Correct rounding of exact sums and quotients**, for `Area` and `Centroid`: a generalization of the staged
   rounding (enclosure → midpoint test with ESSA → BigInteger) behind `crossingPoint` and `circumcenter`.
6. **Correctly rounded square root of a rational**, for `Distance`: the distance from a point to a segment
   is √(cross² / |ab|²).
7. **Snap rounding.** Overlay output vertices are rounded crossing points, which in general do not lie on
   either segment. Without snap rounding the output can become invalid (slivers, new self-intersections).
   Doubles do not form a uniform grid, which complicates the classic hot-pixel algorithm. This is the main
   design question for the constructive operations.

### Order of work

1. **Relate engine** (primitives 1–4), **done**: all spatial relations and `Equals` are exact. `IsSimple` and
   `IsValid` can be built on its arrangement.
2. **Numeric measures**: `Area`, `Centroid`, `Distance` and `Length`; small and independent, each needing one
   rounding routine.
3. **`PointOnSurface`**.
4. **Overlay** with snap rounding, followed by **`Buffer`** on top of it.
