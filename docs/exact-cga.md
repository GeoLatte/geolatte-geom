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

## Performance

Indicative timings for 100,000 points:

| Operation | Time |
|---|---|
| Delaunay, uniform random points | ≈ 50 ms |
| Delaunay, grid with or without noise | ≈ 60–70 ms |
| Delaunay, points on a circle (all cocircular) | ≈ 100 ms |
| Voronoi diagram | ≈ 115–130 ms |

## Not yet available

`ST_Equals`, `ST_IsValid` and the other relate-based predicates have been postponed until the exact
foundation is complete.
