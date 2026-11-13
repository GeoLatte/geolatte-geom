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

### `Relate`, `PreparedRelate`, `IntersectionMatrix` and `PointLocator`

- `Relate.relate(a, b)` returns the DE-9IM `IntersectionMatrix`; `relate(a, b, pattern)` matches it against a
  pattern (`T`, `F`, `*`, `0`, `1`, `2`).
- The named predicates `equalsTopologically`, `disjoint`, `intersects`, `touches`, `crosses`, `within`,
  `contains`, `overlaps`, `covers` and `coveredBy` are static methods of `Relate`, and are also available on an
  `IntersectionMatrix` (`isTouches`, `isCrosses`, ...). The static methods stop as soon as the answer is known
  (see below), so they are usually faster than computing the matrix.
- `PreparedRelate.of(a)` prepares a geometry for testing it against many others: `relate(b)` and the same named
  predicates, with the prepared geometry as the first argument. It is immutable and thread-safe.
- `PointLocator.of(geometry).locate(x, y)` returns the `Location` (`INTERIOR`, `BOUNDARY` or `EXTERIOR`) of a
  point.
- `DefaultProjectedGeometryOperations`, the default `ProjectedGeometryOperations`, computes its spatial
  predicates with `Relate`, its convex hull with `ConvexHull`, and `isSimple`, `isValid` and `boundary` with
  `Simplicity`, `Validity` and `Boundary` (see below).

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

- All segments of both geometries are put into one arrangement. The lines and rings are split into monotone
  chains (runs of segments whose directions lie in the same quadrant), which are packed in a static R-tree per
  geometry, in their order along the lines and rings. A join of the two trees gives the pairs of chains with
  intersecting envelopes, and a recursive bisection of each pair gives the candidate pairs of segments, which
  are classified with `segmentIntersection`. Segments of the same monotone chain only meet in a common vertex and
  are never tested; consecutive segments of a line or ring are only tested if the line turns back.
- A geometry is only intersected with itself (a self-join of its tree) if it has lines, more than one polygon,
  or points and a polygon. The rings of one polygon are never tested against each other, because the polygon is
  valid: they can only touch in vertices. Such a vertex is found afterwards: every node is looked up in the ring
  chains, and a ring that passes through it is split there.
- The nodes are the points where elements meet (vertices, points and crossing points), the endpoints of lines,
  and the lexicographically smallest vertex of each line and ring. Other vertices touch nothing and are not
  nodes. A crossing point is represented implicitly by its two segments; its correctly rounded value is only
  used as a hash key to find nodes that may coincide, and coincidence is confirmed exactly with `orientation` and
  `orientationOfCrossing`.
- The nodes on a segment are ordered exactly. A crossing point is correctly rounded, so if the coordinates of
  two nodes (along the major axis of the segment) differ by more than their rounding errors, they decide the
  order. Otherwise: a vertex and a crossing are ordered with `orientation`, two crossings with
  `orientationOfCrossing`. The parts of the lines and rings between consecutive nodes are the edges: polylines.
  The endpoints of overlapping segments are nodes, and overlapping parts are merged into one edge.
- The edges around each node are sorted by the direction of their first segment, with `crossProductSign`.
- Each face gets a depth per geometry: the number of polygons that cover it. Per connected component, the depth
  of the face to the left of its lexicographically smallest vertex comes from an exact point-in-polygon test;
  the other depths follow by walking around the nodes and along the edges, adding +1 or −1 when crossing a ring.
- The locations of the nodes (dimension 0), the edges (dimension 1) and the faces beside each edge (dimension 2)
  in both geometries give the matrix.

**Predicates.** A named predicate is decided early where possible:

- an envelope test, and the dimensions (two points never touch, areas never cross, geometries of different
  dimensions never overlap);
- the exact location of one position of each geometry in the other, by a linear scan: a position in the
  interior of a polygon of the other geometry proves that the interiors intersect (so `intersects` is true and
  `touches` false), and a position outside the other geometry rules out `within`, `covers` and the like;
- during the join: `intersects` is true at the first contact. A proper crossing of a segment of A and one of B
  that no other segment comes near proves that the interiors intersect and that each geometry has points outside
  the other, if both geometries are locally simple (no polygons, or a single polygon and no lines). So
  `touches`, `within`, `contains`, `covers`, `coveredBy` and `equalsTopologically` are false; `crosses` is true
  for a line against an area, and `overlaps` is true for two areas.

Otherwise the predicate is evaluated on the full matrix.

**Prepared geometries.** `PreparedRelate` computes once everything that depends only on the prepared geometry A:
its monotone chains and their R-tree, the pairs of its own segments that meet, a `PointLocator`, and its connected
components (elements that meet each other), each with its contribution to the matrix when it lies in the exterior
or the interior of another geometry. A computation against B then only uses the components whose bounding box
meets that of B; the others lie in the exterior of B. If no element of A meets one of B, every component lies
entirely in the interior or in the exterior of B, and one point location decides which; A then contributes no
segments at all. The arrangement only stores the vertices and segments of a chain where something happens, so a
long ring that B touches costs no more than a short one. The reference depths in A come from the prepared
locator.

`PointLocator` uses exact crossing parity per polygon, on an interval index over the ring segments, and for a
point on polygon boundaries the same angular ordering to decide whether the polygons cover all of its
neighbourhood.

### `Simplicity`, `Validity` and `Boundary`

- `Simplicity.isSimple(geometry)` tests simplicity (`ST_IsSimple`):
  - A multipoint is simple if no two of its points are equal.
  - A line string is simple if it passes through no point twice, except that it may be closed.
  - A multi line string is simple if its elements are simple and only meet in endpoints of both, which are not
    closed.
  - Polygons are simple if each of their rings is simple.
  - A collection is simple if each element is simple.
  - Repeated consecutive positions are ignored. JTS (1.19 and 1.20) instead reports a repeated position inside
    a line as a self-intersection.
- `Validity.of(geometry)` tests validity (`ST_IsValid`) and reports the first problem with its location;
  `Validity.isValid(geometry)` returns only the verdict. The rules are those of JTS `IsValidOp` (the Simple
  Features rules), with the same verdict on every tested input:
  - coordinates are finite;
  - a line string has two distinct positions;
  - a ring is closed, has three distinct positions and is simple;
  - the rings of a polygon meet only in single points, where they do not cross;
  - holes lie inside the shell and not inside each other;
  - the interior of a polygon is connected;
  - the polygons of a multipolygon have disjoint interiors and meet only in single points.
- `Boundary.of(geometry)` returns the boundary (`ST_Boundary`):
  - nothing for points;
  - the endpoints, by the Mod-2 rule, for lines, in the order in which they first occur;
  - the rings for polygons.
  - It is purely combinatorial: the positions of the geometry are compared exactly by X and Y and returned with
    their Z and M values.
  - The boundary of a heterogeneous `GeometryCollection` is not defined element by element, and is refused, as
    in JTS.

**Exactness.** All three are exact for the input doubles. No intersection point is computed to decide the
result: rings that meet in a valid polygon always meet in a vertex of one of them.

- The segments that meet are found with the monotone chains and R-tree of the relate engine, and each pair is
  classified exactly with `segmentIntersection`.
- In a polygon, a crossing or an overlap of two rings makes it invalid; other pairs touch, in a point that is a
  vertex of at least one of them.
- At each touch point, the edges of the rings that pass through it are sorted by angle, exactly with
  `crossProductSign`. The rings must not interleave around the point: if they did, they would cross there.
- After that, the rings form a nested family, so each ring lies entirely inside or outside each other ring. For
  each polygon, the number of its shells minus the number of its holes that contain a point must be 0 or 1, and
  the sum over the polygons too. These counts only change across rings, and they are constant on each side of a
  ring.
  - So one test per ring is enough: an exact point-in-ring test at a vertex that no other ring passes through.
    The candidate rings come from an R-tree on the ring boxes.
  - If every vertex of the ring touches other rings, the side of each such ring is decided by the direction of
    the ring's first edge, exactly.
  - A count of −1 is a hole outside its shell or in another hole; a count of 2 is two overlapping polygons.
- The interior of a polygon is connected if the graph of its rings and the points where they touch has no cycle
  (union–find).

Indicative timings, against JTS 1.20 `IsValidOp` and `IsSimpleOp`:

| Geometry | Exact | JTS 1.20 |
|---|---|---|
| polygon of 100,000 vertices, `isValid` | ≈ 2 ms | ≈ 5 ms |
| polygon of 1,000,000 vertices, `isValid` | ≈ 13 ms | ≈ 47 ms |
| polygon with 1,000 holes of 50 vertices, `isValid` | ≈ 3 ms | ≈ 12 ms |
| multipolygon of 10,000 polygons of 20 vertices, `isValid` | ≈ 7 ms | ≈ 24 ms |
| polygon with 800 holes that touch at their corners (invalid), `isValid` | ≈ 3.7 ms | ≈ 2.4 ms |
| simple spiral of 100,000 vertices, `isSimple` | ≈ 6 ms | ≈ 7 ms |
| 10,000 lines chained at their endpoints, `isSimple` | ≈ 1.5 ms | ≈ 4.4 ms |

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
| two overlapping polygons, 100,000 vertices each | ≈ 3 ms |
| two overlapping polygons, 1,000,000 vertices each | ≈ 28 ms |
| two overlapping polygons, 100,000 vertices each, with spikes | ≈ 28 ms |
| 10,000 line segments against a polygon of 2,000 vertices | ≈ 8 ms |
| 10,000 line segments against 10,000 line segments (random walks) | ≈ 20 ms |

This is comparable to JTS 1.20 `RelateNG` (and 4 to 15 times faster than JTS `RelateOp` on large inputs). The
named predicates take ≈ 1 ms for two polygons of 100,000 vertices when they are decided early, most of which is
spent copying the coordinates; RelateNG is faster there (≈ 0.1–0.3 ms), because JTS geometries cache their
envelope and RelateNG reads the coordinates in place.

With `PreparedRelate`, per test against 2,000 small geometries (points, or polygons of 12 vertices):

| Prepared geometry | Predicate | Full matrix |
|---|---|---|
| a polygon of 100,000 vertices, against points | ≈ 1 µs | ≈ 1 µs |
| a polygon of 100,000 vertices, against small polygons | ≈ 1.5 µs | ≈ 4 µs |
| 10,000 small polygons | ≈ 8–35 µs | ≈ 57 µs |
| 10 lines of 1,000 segments that cross each other often | ≈ 0.3–2.5 µs | ≈ 63 µs |

This is comparable to prepared `RelateNG`, except for the full matrix of the line set: when B touches a component
of A, the whole component is part of the arrangement. That costs ≈ 1 ms for each of the 7 % of tests where it
happens; RelateNG, which evaluates the topology only locally, averages ≈ 5 µs per test.

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
| `Boundary` | **done** | `Boundary`: combinatorial (Mod-2 rule); not for heterogeneous collections |
| `IsSimple`, `IsRing` | **done** | `Simplicity`; `LineString.isRing()` is closed and simple |
| `Equals`, `Disjoint`, `Intersects`, `Touches`, `Crosses`, `Within`, `Contains`, `Overlaps`, `Relate` (and `Covers`, `CoveredBy`) | **done** | `Relate`, with union semantics |
| `IsValid` (SQL/MM) | **done** | `Validity`, with the rules of JTS `IsValidOp` |
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

1. **Relate engine** (primitives 1–4), **done**: all spatial relations and `Equals` are exact. `IsSimple`,
   `IsValid` and `Boundary` are done as well, on its monotone chains and exact angular ordering.
2. **Numeric measures**: `Area`, `Centroid`, `Distance` and `Length`; small and independent, each needing one
   rounding routine.
3. **`PointOnSurface`**.
4. **Overlay** with snap rounding, followed by **`Buffer`** on top of it.
