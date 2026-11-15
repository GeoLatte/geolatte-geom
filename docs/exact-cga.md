# Exact computational geometry

The `org.geolatte.geom.cga.exact` package and the algorithms in `org.geolatte.geom.cga` built on it compute
geometric results exactly for double precision coordinates. The methods follow H. Ratschek and J. Rokne,
*Geometric Computations with Interval and New Robust Methods* (Horwood, 2003), complemented by Shewchuk's
adaptive expansion arithmetic and double-word arithmetic (from *Tight and rigorous error bounds for 
basic building blocks of double-word arithmetic*, Joldes, Muller & Popescu).

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
| `squaredDistanceToSegment(...)` Joldes, Muller & Popescu)| an `Interval` enclosing the squared distance | interval arithmetic |
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
- **`Summation`**: the exact sum of doubles and of products of two or three doubles, kept as non-overlapping
  partial sums (Shewchuk's GROW-EXPANSION, as in Python's `math.fsum`), with a `BigInteger` part for terms that
  would underflow or overflow. `signum()` is exact; `round()`, `round(scale)` and `roundQuotient(num, den)` are
  correctly rounded. This is primitive 5 of the roadmap.
- **`PolygonMoments`**: the correctly rounded area and centroid of a set of rings (see below).
- **`MinimumDistance`**: the correctly rounded minimum of the distances of candidate point-point and
  point-segment pairs, i.e. the correctly rounded square root of a rational (primitive 6, see below).
- **`LengthSum`**: the sum of the lengths of segments, correctly rounded in practice and faithfully rounded always
  (see below).
- **`RoundingCell`**: exact predicates on the rounding cells of double points, the pixels of snap rounding
  (primitive 7, see `Overlay` below): whether a segment meets a cell, and in which order it enters two cells.
- **`GridCell`**: the same predicates for the cells of a fixed grid with a decimal cell size, and the exact
  rounding of a coordinate or of the crossing point of two segments to that grid (see `Overlay` below).

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

### `Area` and `Centroid`

- `Area.of(geometry)` returns the area: for each polygon, the area enclosed by its shell minus the areas enclosed
  by its holes, whatever the orientation of the rings, summed over the polygons of a multipolygon or collection.
  Points and lines have no area. Overlapping polygons of a collection are counted once per polygon, as in JTS.
  It backs `ProjectedGeometryOperations.exactArea`.
- `Area.fast(geometry)` computes the same area with the shoelace formula in plain floating point. It is as fast
  as JTS, reads the coordinates in place, and has no accuracy guarantee. It backs `area` in
  `DefaultProjectedGeometryOperations`, because the area is often needed in bulk, where speed matters more than
  the last bits.
- `Centroid.of(geometry)` returns the centroid of the parts of the highest dimension with a non-zero measure, as
  JTS does: the centroid of the area; else of the segments, weighted by length; else the average of the points
  (a line of zero length counts as its first point). The result has the coordinate reference system of the
  geometry, with NaN for Z and M; the centroid of an empty geometry is an empty point.

**Guarantee.** The area and the centroid of an area are rational functions of the coordinates:

```
A  = 1/2 sum_r f_r sum_i c_i,                         c_i = x_i y_(i+1) - x_(i+1) y_i
Cx = sum_r f_r sum_i (x_i + x_(i+1)) c_i / (6 A)      (and Cy alike)
```

with `f_r = ±1` the sign with which ring r is counted (from the exact sign of its area). Both are **correctly
rounded**: the result is the double nearest to the exact value (ties to even). It does not depend on the order of
the polygons, rings or positions, nor on the orientation of the rings; scaling by a power of 2 scales the result
exactly. The average of points is correctly rounded too. The centroid of lines is not: segment lengths are square
roots, which are rounded (the sums are then exact, so the result is very accurate).

**Algorithm** (`PolygonMoments`):

1. Each ring is accumulated in double-word arithmetic with a rigorous error bound. Coordinates are taken relative
   to the first position of the ring, when all those differences are exact (as for typical projected data,
   where the coordinates of a ring are within a factor 2 of each other). This removes the cancellation due to
   large coordinates. The difference of the two products of each `c_i` is formed exactly before it is added, so
   the running sums stay small, and so do their rounding errors. The loops have no data-dependent branches.
2. The ring results are combined with the sign of each ring's area (`Predicates.ringOrientation` if the
   first stage cannot decide it). The centroid of a ring relative to its first position `o` is shifted back
   exactly: `Nx = 3 o S + Mx`.
3. The totals are enclosed in intervals about 2⁻¹⁰⁰ times their magnitude wide, and the quotient for the
   centroid is enclosed with the double-word division of `Constructions`. When the interval contains no rounding
   boundary, the result is decided; this is the case for all realistic input.
4. Otherwise (nearly degenerate polygons, a centroid extremely close to a rounding boundary, coordinates outside
   [2⁻²⁰⁰, 2²⁰⁰]), the sums are computed exactly with `Summation`, from the original coordinates, and rounded with
   `BigInteger` arithmetic.

Indicative timings, against JTS 1.19 `getArea()` and `getCentroid()` (including copying the coordinates, about
1.3 ms per million positions):

| Geometry | `Area.of` | `Area.fast` | JTS | `Centroid` | JTS |
|---|---|---|---|---|---|
| polygon of 100,000 vertices | ≈ 0.5 ms | ≈ 0.04 ms | ≈ 0.04 ms | ≈ 1.5 ms | ≈ 0.9 ms |
| polygon of 1,000,000 vertices | ≈ 5 ms | ≈ 0.4 ms | ≈ 0.4 ms | ≈ 15 ms | ≈ 9 ms |
| multipolygon of 10,000 polygons of 20 vertices | ≈ 1.1 ms | ≈ 0.13 ms | ≈ 0.12 ms | ≈ 3.4 ms | ≈ 1.2 ms |

The correctly rounded area costs about ten times the floating point sum of `Area.fast` and JTS, which has no
accuracy guarantee. The naive shoelace sum gives 0 for a square of 1 mm² at Belgian Lambert coordinates. JTS,
which takes the X coordinates relative to the first position, gets such a square right, but not slivers: for
nearly collinear positions at coordinates around 100,000, its area can be off by a factor of 75.

### `Distance` and `Length`

- `Distance.of(geometry, other)` returns the smallest distance between a point of one geometry and a point of the
  other. It is 0 when the geometries intersect (`Relate.intersects`, so a geometry inside a polygon of the other,
  or in a collection with union semantics, is at distance 0), and NaN when one of them is empty. It backs
  `distance` in `DefaultProjectedGeometryOperations`.
- `Length.of(geometry)` returns the length of lines, the perimeter of polygons (all rings), and the sum over the
  elements of a collection; points have no length. It backs `ProjectedGeometryOperations.exactLength` and
  `exactPerimeter`; `Length.fast` backs `length` and `perimeter`.
- `Length.fast(geometry)` sums `sqrt(dx² + dy²)` in plain floating point, reading the coordinates in place. It is
  as fast as JTS, and backs `length` in `DefaultProjectedGeometryOperations`, as `Area.fast` backs `area`.

**Guarantee.** For non-intersecting geometries, the distance is attained between a vertex of one and a vertex or
segment of the other, so its square is rational: `|p - q|²`, or `((b - a) × (p - a))² / |b - a|²` when the
perpendicular from p falls inside segment ab. The distance is **correctly rounded**: the double nearest to the
exact distance (ties to even). It is symmetric, and scaling by a power of 2 scales it exactly.

A length is a sum of square roots. A positive sum of square roots is rational only if every term is, so the
length can be exactly a midpoint between two doubles (a tie) only when every segment length is rational, as for
lines along the axes. Otherwise it is irrational, and can in principle lie arbitrarily close to a midpoint.
`Length.of` is **correctly rounded** (ties to even), except when the exact length is irrational and lies within
about 10⁻⁵⁸ (relatively) of a midpoint; it is then one of the two doubles next to it (faithful rounding).

**Algorithm** (`Distance`, `MinimumDistance`):

1. The elements (points and segments) of each geometry go into a packed R-tree. A branch-and-bound search visits
   the pairs of nodes in order of the distance of their boxes, and keeps an upper bound of the distance. For each
   pair of elements, the distances of the vertices of each to the other element are estimated in floating point,
   with a rigorous error bound (2⁻⁴⁰ relative to the coordinate differences). Pairs whose lower bound exceeds the
   final upper bound are dropped. A single point or segment against a geometry, and two very small geometries,
   skip the trees.
2. Each remaining pair is classified exactly: whether the perpendicular falls inside the segment
   (`Predicates.dotSign`), so that the squared distance is one of the two rational forms above. It is computed
   in double-word arithmetic with a rigorous relative error bound, and the square root is taken with one Newton
   step. When the enclosure of the smallest one contains no rounding boundary, the result is decided.
3. Otherwise (a distance extremely close to a midpoint, an exact tie, extreme magnitudes), the squared distances
   are compared exactly as `BigInteger` rationals, and the square root of the smallest is rounded correctly from
   an integer square root with more than 60 bits and a sticky bit.

**Algorithm** (`Length`, `LengthSum`):

1. The coordinate differences are exact double-words, the sum of their squares is computed with a tiny error, and
   its square root with one Newton step (relative error below 2⁻¹⁰⁰). The lengths are summed with TwoSum, with a
   rigorous bound on the error, and the enclosure is rounded as for the area. This decides the result unless the
   exact length lies within about 2⁻¹⁰⁰ of a midpoint, or is one.
2. Otherwise (and for coordinate differences outside [2⁻⁴⁵⁰, 2⁴⁵⁰]), the lengths are computed again with
   `BigDecimal` square roots: exactly when they are rational, and with at least 60 digits otherwise. Their exact
   sum is then rounded.

Indicative timings, against JTS 1.19 `DistanceOp` and `getLength()`:

| Geometries | `Distance.of` | JTS |
|---|---|---|
| a point and a polygon of 100 vertices | ≈ 1 µs | ≈ 1 µs |
| a point and a polygon of 100,000 vertices | ≈ 1.2 ms | ≈ 0.9 ms |
| two polygons of 100 vertices | ≈ 3 µs | ≈ 3 µs |
| two polygons of 10,000 vertices | ≈ 0.25 ms | ≈ 5 ms |
| two polygons of 100,000 vertices | ≈ 3 ms | ≈ 970 ms |
| two lines of 100,000 vertices | ≈ 3.5 ms | ≈ 980 ms |

About a fifth of the time of `Distance.of` is the `intersects` test. JTS `DistanceOp` compares all pairs of
segments, unless the geometries are indexed beforehand (`IndexedFacetDistance`).

| Geometry | `Length.of` | `Length.fast` | JTS |
|---|---|---|---|
| polygon of 100,000 vertices | ≈ 0.7 ms | ≈ 0.14 ms | ≈ 0.14 ms |
| polygon of 1,000,000 vertices | ≈ 6.7 ms | ≈ 1.4 ms | ≈ 1.4 ms |

### `PointOnSurface`

`PointOnSurface.of(geometry)` returns a point that lies on the geometry (`ST_PointOnSurface`), with the rules of
JTS `InteriorPoint`: for polygons, a point in the interior; otherwise an interior vertex of a line nearest to the
centroid (else the nearest endpoint), or the point nearest to the centroid. It backs `pointOnSurface`.

**Guarantee.** For a valid polygon, the point lies in its **interior**, decided exactly. For a multipolygon or a
collection it lies in the interior of one of the polygons, and so in the interior of their union. A polygon of
zero area has no interior; its rings then count as lines, and the result is one of their vertices. The same
holds when no interior point is found on the scan lines that are tried, which happens for extremely thin
polygons, often because no point with double coordinates lies in their interior. The result is then on the
boundary, but never outside.

**Algorithm.** As in JTS, a horizontal scan line is placed between the vertex y coordinates nearest to the middle
of the polygon's envelope, so that it passes through no vertex. The crossings with the edges are computed in
floating point, and the candidate is the midpoint of the widest interval between crossings 2k and 2k + 1. Its
location is then decided exactly: no vertex lies on the scan line, so the edges that cross it are found exactly
by comparing y coordinates, and whether each passes to the left of the candidate is an exact `orientation` test.
The candidate is accepted if that count is odd and it lies on no edge. Otherwise the next widest intervals, and
then other scan lines (from the middle outwards), are tried. The approximation only chooses the candidate, so
rounding errors can at worst cost a retry.

The results are the same as those of JTS 1.19 `getInteriorPoint()` on ordinary polygons, and so is the speed
(≈ 0.4 ms for a polygon of 100,000 vertices, ≈ 1.1 ms for a multipolygon of 10,000 polygons of 20 vertices).
On nearly degenerate polygons they differ: for 100,000 nearly collinear triangles at coordinates around
(152000, 212000), JTS returned a point outside the triangle in 14 % of the cases and on its boundary in 20 %.
`PointOnSurface` never returned a point outside; in 20 % of the cases no double was found in the interior along
the scan lines tried (often there is none), and the result was a vertex.

### `Overlay`

`Overlay.intersection`, `union`, `difference`, `symDifference` and `unaryUnion` (and `overlay(a, b, operation)`)
compute the overlay of two geometries. They back the set operations of `DefaultProjectedGeometryOperations`, and
the new `unaryUnion`. Each has an overload with a `Grid`: `Grid.DOUBLE` (the default) or a fixed grid such as
`Grid.fixed("0.001")` (see *Fixed grids* below). `Overlay.reducePrecision(geometry, grid)` snap rounds a single
geometry to a grid; it is the unary union on that grid.

**Semantics.** A geometry is the union of its elements, as for `Relate`: the members of a `GeometryCollection` may
overlap, and each polygon must be valid. The result is the set of points for which the operation holds, so it can
mix dimensions: the intersection of two squares that share a side is a `LineString`, and the union of a polygon
and a line that sticks out of it is a `GeometryCollection` of the polygon and the parts of the line outside it.
Lower dimensional parts that lie on higher dimensional parts of the result are absorbed. An empty result is an
empty geometry of the dimension of the result (for an intersection the lower dimension of the operands, for a
difference that of the first operand, otherwise the higher one), as in JTS OverlayNG.

**Guarantee.** All decisions are exact for the input doubles, and the result is snap rounded to the double grid
(for fixed grids see below):

- the result is valid;
- each vertex is a vertex of an operand, or the double nearest to a point where two segments cross;
- each edge passes through the rounding cells (see below) that the exact result passes through, so it lies
  within one unit in the last place of it;
- parts of polygons that are thinner than that can collapse; they are dropped, as in OverlayNG;
- points of an operand that are not on its lines or polygons are located exactly in the other operand.

**Snap rounding on the double grid (primitive 7).** The *rounding cell* of a double point is the set of points of
the plane whose coordinates round to it: in each axis, from the midpoint with the previous double to the midpoint
with the next one. Under round half to even, the interval of an even double is closed and that of an odd one is
open, so the cells tile the plane, and a correctly rounded crossing point lies in its own cell. Doubles are not a
uniform grid (around a power of 2 the cell is asymmetric), but each cell is convex and contains its point, which
is all that the argument of Guibas and Marimont needs. `RoundingCell` decides exactly whether a segment meets a
cell and in which order a segment enters two cells: a floating point filter decides almost all cases, the rest are
clipped exactly with rational arithmetic (Liang–Barsky on `BigDecimal` fractions, respecting the open and closed
sides).

The *hot pixels* are the cells of all vertices of the lines and rings of both operands, and of the correctly
rounded crossing points of their segments (found with the monotone chains of `Relate`). Each segment is replaced
by the polyline through the points of the hot pixels it meets, in the order in which it enters them (Hobby's snap
rounding). The vertices do not move. The snapped segments are then checked exactly: a vertex that lies in the
interior of another snapped segment is inserted into it, and so are the ends of overlapping segments, so that
the chains share vertices and identical segments. Should two snapped segments still cross, their rounded crossing
point becomes a hot pixel and the snapping is repeated. That has not happened in any test; it is a safeguard.
The hot pixels and the monotone chains are packed into their R-trees along a Hilbert curve, so that many small
separate rings (such as the disks of a buffer of points) stay fast.

**Fixed grids.** `Grid.fixed(size)` takes a decimal cell size g (a `BigDecimal`, a string such as `"0.001"`, or
a double read as its shortest decimal, so `0.001` is one thousandth); `Grid.ofScale(scale)` takes the number of
cells per unit, as JTS's `PrecisionModel`, and requires 1 / scale to be a finite decimal. The grid points are
(i g, j g). The cell of a grid point is the half-open square [(i − ½) g, (i + ½) g) × [(j − ½) g, (j + ½) g), so
a coordinate v rounds to floor(v / g + ½) g: halves round up, as in JTS. The cells tile the plane. A grid point
such as 0.001 is in general not a double, so it is represented by the double nearest to it, which lies inside
the cell as long as the cell is wider than the spacing of the doubles there. The operations check this for the
coordinates of the operands, and throw an `IllegalArgumentException` otherwise: a cell size of 0.001 works up to
coordinates of about 10¹², far beyond projected coordinates.

The snap rounding is the same as on the double grid, with the cells of the fixed grid as pixels: the hot pixels
are the cells that contain a vertex, or the exact crossing point of two segments (located exactly, with a
`BigDecimal` fallback near cell boundaries, not via a rounded double). Each segment becomes the polyline through
the grid points of the hot pixels it meets, from the grid point of its start to that of its end, so vertices
move to their grid points. Hobby's argument only needs convex cells that tile the plane and contain their
representative points; the exact check of the snapped segments makes the result crossing free in any case.
`GridCell` decides the cell predicates exactly, with floating point filters and an exact `BigDecimal` fallback.

The guarantee carries over: the result is valid (checked exactly on its doubles), each vertex is a grid point,
each edge passes through the cells that the exact result passes through, and parts thinner than a cell collapse
and are dropped — including lines and polygons that collapse to a point, and polygons that collapse to a line,
as in OverlayNG. Snapping can make operands meet that did not: squares 0.0004 apart share a side on a millimetre
grid. Points of an operand that are not on its lines or polygons are located exactly in the original operands,
then rounded to their grid point, and dropped if that lies on a line or polygon of the result. A vertex takes the
Z and M of the first input vertex (of the first operand first) that rounds to it.

Snap rounding is not idempotent: an edge of a result can pass through the cell of another vertex of it, so
rounding the result again can move that edge to the vertex (in 7 % of random star polygons with grids of a few
percent of their size). This holds for JTS's snap rounding as well. Iterated snap rounding would avoid it, at the
cost of the bound of one cell on the displacement.

JTS rounds with floating point arithmetic: `PrecisionModel.makePrecise(0.0045)` with scale 1000 gives 0.005,
because 0.0045 × 1000 rounds to 4.5. The double 0.0045 is slightly less than 0.0045, so `Grid.round` gives 0.004.

**Algorithm.** The snapped segments become the edges of a planar graph. Equal segments are merged, with per
operand a flag for lines and the sum of the deltas of the rings along it (the change of the number of polygons
from its right to its left). Edges of collapsed polygon parts sum to zero and are dropped. The edges around each
node are sorted with exact direction comparisons, and the depths (number of polygons) of the faces are propagated
around the nodes and along the edges, starting for each connected component from the face to the left of its
smallest node, whose depth is counted exactly along a ray. This gives each face, edge and node a location relative
to each operand, as in `Relate`. The result consists of the faces for which the operation holds; the edges for
which it holds, outside such faces; and the nodes for which it holds, outside both.

The boundary of the result faces is traced with the result to the left, splitting traces at repeated nodes into
simple rings. Counterclockwise rings are shells, clockwise rings holes. Each hole goes to the innermost shell that
contains it, decided with a node of the hole that is not on the shell. Result edges are merged into maximal lines
through nodes where only two of them meet, except at the endpoints of input lines; a line follows the direction of
the first input line along it.

**Z and M.** The result has the coordinate reference system of the first operand. A vertex keeps the Z and M of
the input vertex at the same position (of the first operand if both have one). A new vertex gets the values
interpolated along the segment of the input it was created on (of the first operand, if both have one), at the
projection of the vertex on it.

**Tests.** Unions of random grid cells are computed exactly (no coordinate is rounded): the areas of all results
equal the cell counts, and the identities A = (A − B) ∪ (A ∩ B), A △ B = (A ∪ B) − (A ∩ B) and commutativity hold
exactly. On random star polygons with holes, also at projected coordinates, the areas agree with JTS 1.19
OverlayNG within a relative 10⁻¹², and so does the area of the symmetric difference of the two results. Polygons
overlaid with copies of themselves moved by a few ulps or rotated by 10⁻¹⁴ radians, and polygons whose vertices
all lie within a few ulps, always give valid results. On fixed grids (0.001, 0.03, 0.5, 7), random star polygons
give valid results with all vertices on the grid, with areas within 2 g × the perimeters of those of OverlayNG
with the corresponding `PrecisionModel`; a scratch stress run of 24,000 overlays of star polygons, copies shifted
by a fraction of a cell, and collections of lines and points, on six grids, gave no invalid result and no
exception. `GridCell` is checked against an independent rational oracle, and `SnapRounder` on 60,000 random sets
of chains on fixed grids.

**Performance.** Per operation, after warm up:

| Operands | `Overlay` | OverlayNG (robust) | `OverlayOp` (legacy, snap if needed) |
|---|---|---|---|
| two polygons of 1,000 vertices | ≈ 0.7–2 ms | ≈ 1 ms | ≈ 0.6 ms |
| two polygons of 10,000 vertices | ≈ 8 ms | ≈ 5 ms | ≈ 4 ms |
| two polygons of 100,000 vertices | ≈ 170–220 ms | ≈ 160 ms | ≈ 145 ms |
| a multipolygon of 10,000 squares and a polygon of 1,000 vertices | ≈ 65 ms | ≈ 28 ms | ≈ 145 ms |

The time grows with the number of crossings. Two polygons of 32,000 vertices with long spikes that cross 330,000
times take ≈ 2.5 s, against ≈ 2.9 s for OverlayNG.

On a millimetre grid (`Grid.fixed("0.001")`), against OverlayNG with the corresponding fixed `PrecisionModel`, and
`reducePrecision` against JTS `GeometryPrecisionReducer`:

| Operands | `Overlay`, grid 0.001 | OverlayNG, scale 1000 | `reducePrecision` | `GeometryPrecisionReducer` |
|---|---|---|---|---|
| two polygons of 1,000 vertices | ≈ 1.7 ms | ≈ 1.6 ms | ≈ 1.1 ms | ≈ 0.9 ms |
| two polygons of 10,000 vertices | ≈ 7 ms | ≈ 9 ms | ≈ 4 ms | ≈ 6 ms |
| two polygons of 100,000 vertices | ≈ 160 ms | ≈ 175 ms | ≈ 70 ms | ≈ 125 ms |
| a multipolygon of 10,000 squares and a polygon of 1,000 vertices | ≈ 55 ms | ≈ 55 ms | | |

A fixed grid costs about as much as the double grid.

### `Buffer`

`Buffer.buffer(geometry, distance)` computes the buffer: for a positive distance d the points within d of the
geometry, for a negative distance the points of its polygons farther than |d| from their boundary (lines and points
then give nothing), and for 0 the union of its polygons. It backs `buffer` in `DefaultProjectedGeometryOperations`.
Overloads take `BufferParameters`, the style of JTS and PostGIS (segments per quadrant, round, flat or square end
caps, round, mitre or bevel joins, mitre limit, simplify factor), and a `Grid`. The result is a `Polygon` or `MultiPolygon`, possibly
empty, without Z or M.

**Approximation.** Arcs are approximated as in JTS: the arc around a vertex is divided into round(angle / quantum)
equal parts, with a quantum of π / 2q for q segments per quadrant, and its vertices lie on the circle. With round
joins and caps the result therefore lies inside the exact buffer, by at most d (1 − cos(3π / 8q)) (0.011 d for
q = 8). Mitres follow JTS 1.19: the meeting point of the offset lines if it lies within the mitre limit × d of the
vertex, otherwise a mitre cut off perpendicular to the bisector at that distance, or a bevel if the bevel lies
farther out.

**Simplification.** With round joins the input is first simplified with tolerance h = simplify factor × |d|
(0.01 |d| by default, as in JTS; at most 0.1 |d|). That adds at most 2h to the error above, and the result still lies
inside the exact buffer. Each ring and line is simplified with the exact Douglas-Peucker algorithm: every dropped
vertex lies within h of the chord that replaces it (decided exactly), so the part of the input that a chord replaces
lies within h of it and vice versa. The pieces (see below) are built on the simplified path, with arcs of radius d
around its vertices, which are input vertices. The quadrilateral of a chord keeps depth d if no dropped vertex lies
on the side away from the buffer: each of its points is then within d of the dropped part, which crosses the normal
through the point between the chord and the point, or passes within h of the point. Otherwise the quadrilateral has
depth d − h. So the error of the simplification only appears where vertices were dropped toward the inside, and
there it is at most 2h: a point within d cos(3π / 8q) − 2h of the input is within d cos(3π / 8q) − h of the
simplified path. The simplified polygons must be valid with their rings oriented as before (checked exactly; the
polygon is used as it is otherwise), so the winding number argument holds for them; what simplification cuts off a
polygon lies within 2h of the simplified boundary, which the pieces cover as long as 2h ≤ (d − h) cos(3π / 8q),
hence the limit of 0.1. With mitre or bevel joins the corners would change shape, so the input is not simplified;
with flat or square caps the end segments of a line are kept.

**Method.** Each ring, line and point gives a closed raw offset curve (`OffsetCurve`), whose winding number is
positive exactly in the buffer. The curves go into the overlay graph of `Overlay` as rings of a single operand; it
snap rounds them, labels the faces with their winding numbers (the depths of the overlay), and the faces with a
positive winding number form the result. So no new topology code is involved, the result is valid, and all
decisions (the kind of each turn, whether offset segments cross, the noding) are exact. The only errors are the
arc approximation, the rounding of the constructed offset points, and snap rounding.

Why the winding number is right: offset a closed path by d to its right. The pieces

- the quadrilateral (a, a + d m, b + d m, b) of each segment from a to b, with m its right unit normal, and
- at each left turn (the right side is convex there), the wedge of the join: the vertex plus the arc, mitre or
  bevel between the two offset endpoints; at the end of a line the cap

each have winding number 1 inside and 0 outside, and their union is the one sided buffer. In the sum of their
boundaries the sides shared by a quadrilateral and a wedge cancel, and the segments of the path add up to the
reversed path. What remains, plus the path, is the raw offset curve: the offset segments, joined by the joins at
left turns and through the vertex at right turns (from the end of one offset segment to the vertex and on to the
start of the next). Its winding number is the winding number of the path plus the number of pieces that contain the
point. For a polygon ring oriented with the interior to its left, that is 1 in the polygon plus the pieces: positive
exactly in the buffer. For a negative distance the reversed ring is offset, into the polygon, and the curve counts
negatively: 1 in the polygon minus the pieces, positive exactly where the polygon is not covered by pieces. An open
line is the closed path along the line and back, with caps at both ends; a closed line gives both directions of the
ring. Since the argument holds for any quadrilaterals and wedges, the rounding of the offset points only perturbs
the pieces; it does not break it.

At a right turn whose offset segments cross, JTS joins them at the crossing point instead of through the vertex.
That removes a cycle around the region between the crossing point, the offset endpoints and the vertex. If that
region lies in both quadrilaterals, the number of pieces there drops from 2 to 1, which is harmless; so the
shortcut is taken only if each offset endpoint lies in the quadrilateral of the other segment (decided exactly).
And it is not taken at every vertex of a path: if all vertices took it, a point could lie in as many removed regions
as in quadrilaterals. That happens when a negative buffer removes a convex polygon completely: with the shortcut
everywhere, the curve is an inverted copy of the polygon, which JTS has to detect by a separate test.

At a right turn whose offset segments do not cross, the curve passes through the vertex: a spike of length d. Where
the path curves more tightly than d (dense, noisy or tightly curved input) every vertex has one, and each spike
crosses the spikes and offset segments around it, so noding the curve as a whole costs time quadratic in d over the
vertex spacing. Long spikes are therefore handled locally: spikes longer than 4 times an adjacent segment, or whose
bounding box meets those of more than 8 segments (counted with an R-tree; a short spike is cheaper to leave in the
curve, and the choice only affects speed). The offset curve splits into the path plus closed curves
around chunks of consecutive vertices (from the first vertex of the chunk along the offset curve to its last vertex,
and back along the path); the winding number of a chunk curve is the number of pieces of the chunk that contain the
point (the shortcut argument holds within a chunk, which takes no shortcut at its ends). Long spikes at most 8
vertices apart form a group; the chunks of a group (2 long spikes each) are united separately, those unions merged pairwise, and
the union of the group takes the place of its chunks: the curve follows the path through the group, and the union
counts once, with the sign of the curve. All terms are non-negative, so the sign of the total does not change. JTS
instead closes the gap between the offset segments with short "closing segments" near the vertex. That is fast but
not covered by an argument: put into this curve, they leave out parts of the buffer in about 1 % of random cases (up
to 9 % of its area); in JTS itself, which also has other steps, misses were only found with fewer than 8
segments per quadrant, and small.

**Differences from JTS.** JTS also simplifies the input by 1 % of the distance, but only drops vertices that turn
toward the buffer, checks only a sample of about 10 of the dropped vertices against the tolerance, and then offsets
by the full distance (so its result can reach up to h beyond the buffer). It also skips offset points that lie very
close together, and simplifies with every join style. So the vertices differ slightly. JTS also drops the offset curve of a hole
that the buffer closes, which removes the mitres of the hole (a mitre at a sharp point of a polygon into a hole can
reach far beyond it); it buffers the overlapping polygons of a collection with a negative distance separately,
where `Buffer` unites them first; and it can leave a hole in the buffer of a closed line that crosses itself.

**Tests.** Exact areas for points, squares and segments with every join and cap, erosion (an L shape, a square
until it vanishes), holes that close, merging sides, collections, and fixed grids. On random star polygons with
holes, random self-crossing open and closed lines, dense lines and point sets, also at projected coordinates, with
1 to 10 segments per quadrant and distances from 0.05 to 15 of both signs, every result is valid, and an exact
oracle on sample points confirms that the result contains every point within d cos(3π / 8q) − 2h of the geometry and
no point farther than d (mirrored for negative distances); the same holds on dense wavy polygons, with and without
holes, and dense lines with many spikes, with simplify factors from 0 to 0.1. Scratch stress runs of 20,000 random
and 3,000 dense cases, including unlimited mitres and random simplify factors, found no violation. With all styles, on the cases where JTS follows the definition (see above),
the area of the symmetric difference with JTS `BufferOp` is below 0.3 % of the distance × the length of the
geometry. On fixed grids the result is valid, on the grid, and within a cell of the buffer on the double grid.

**Performance.** After warm up, against JTS `BufferOp` with the default parameters:

| Input | `Buffer` | JTS |
|---|---|---|
| smooth polygon, 1,000 vertices, d = 1 / 50 / −20 | 3.1 / 6.4 / 1.5 ms | 0.9 / 1.2 / 0.3 ms |
| smooth polygon, 10,000 vertices, d = 1 / 50 / −20 | 5.1 / 3.8 / 1.4 ms | 0.7 / 2.3 / 0.7 ms |
| smooth polygon, 100,000 vertices, d = 1 / 50 / −20 | 10.5 / 9.4 / 7.2 ms | 5.8 / 6.6 / 5.0 ms |
| noisy polygon, 10,000 vertices, d = 1 / 50 / −20 | 23 / 65 / 112 ms | 7.4 / 587 / 222 ms |
| noisy polygon, 100,000 vertices, d = 1 / 50 / −20 | 1.99 / 0.40 / 0.94 s | 2.59 s / out of memory / out of memory |
| noisy line, 10,000 vertices 0.6 apart, d = 50 | 141 ms | 2,796 ms |
| random star polygon, 10,000 vertices, d = 100 | 145 ms | 7,169 ms |
| 10,000 points, d = 5 | 554 ms | 693 ms |

The smooth polygons have radius about 1,000 with waves of amplitude 200 and 50; the noisy ones add Gaussian noise of
0.5 to the radius; out of memory is with an 8 GB heap. Timings are indicative (repeated runs vary by up to 20 %).
The input simplification (factor 0.01, the default) makes the difference on smooth, dense input: without it the
smooth polygon of 100,000 vertices took 132 / 952 / 107 ms, since every vertex then gives a short offset segment and,
where the distance is much larger than the curvature radius, a spike. With it, `Buffer` is within a factor of about
1.5 to 5 of JTS on smooth input; what remains is the cost of the exact overlay. Simplification cannot remove noise
that is larger than the tolerance (the noisy polygons at d = 1), and there JTS's own simplification does not help
either. On noisy, dense or jagged input JTS spends its time noding long raw curves, and `Buffer`, which unites the
spikes locally, is faster.

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
| `Area` | **done** | `Area.of`: correctly rounded (`exactArea`); `Area.fast`: floating point (`area`) |
| `Centroid` | **done** | `Centroid`: correctly rounded for areas and points; lines are weighted by rounded lengths |
| `PointOnSurface` | **done** | `PointOnSurface`: in the interior, decided exactly |
| `Distance` | **done** | `Distance`: correctly rounded; NaN for an empty operand |
| `Length`, `Perimeter` | **done** | `Length.of`: correctly rounded (`exactLength`, `exactPerimeter`); `Length.fast`: floating point (`length`, `perimeter`) |
| `Intersection`, `Union`, `Difference`, `SymDifference` (and `unaryUnion`) | **done** | `Overlay`: exact decisions, snap rounded to the double grid or a fixed grid (primitive 7); point set semantics |
| precision reduction (PostGIS `ST_ReducePrecision`) | **done** | `Overlay.reducePrecision`: snap rounded to a fixed grid, valid |
| `Buffer` | **done** | `Buffer`: raw offset curves, united by their winding number in the overlay graph; exact decisions, valid result, arcs approximated as in JTS |
| `LocateAlong`, `LocateBetween` | existing | could use correctly rounded interpolation; low priority |

### Primitives

All primitives are done: 1–4 make up the relate engine, 5 gives the area and the centroid, 6 the distance, and 7
the overlay.

1. **Ordering of implicit intersection points** (done): `orientationOfCrossing` compares a crossing point with
   a line, which orders crossings along a segment and decides whether two crossings coincide. Crossing points
   are never constructed to decide topology.
2. **A public point locator** (done): `PointLocator`, with union semantics and the Mod-2 rule.
3. **Angular ordering of edges around a node** (done): every edge lies on an original segment, so this reduces
   to the half plane, from exact coordinate comparisons, and `crossProductSign`.
4. **Noding of two geometries** (done): the arrangement and depth labelling of `Relate`.
5. **Correct rounding of exact sums and quotients** (done): `Summation`, with a double-word first stage in
   `PolygonMoments`, for `Area` and `Centroid`.
6. **Correctly rounded square root of a rational** (done): `MinimumDistance`, for `Distance`. The distance
   from a point to a segment is √(cross² / |ab|²).
7. **Snap rounding** (done): overlay output vertices are rounded crossing points, which in general do not lie on
   either segment; without snap rounding the output can become invalid. `SnapRounder` uses the rounding cells
   of the doubles as pixels (`RoundingCell`): they are not uniform, but they tile the plane and contain their
   points, which is what Hobby's snap rounding needs. A fixed grid with a decimal cell size (`Grid.fixed`) uses
   the cells of `GridCell` instead; `Grid` hides the difference from `SnapRounder`.

### Order of work

1. **Relate engine** (primitives 1–4), **done**: all spatial relations and `Equals` are exact. `IsSimple`,
   `IsValid` and `Boundary` are done as well, on its monotone chains and exact angular ordering.
2. **Numeric measures**: `Area`, `Centroid`, `Distance` and `Length` are **done**.
3. **`PointOnSurface`**, **done**.
4. **Overlay** with snap rounding, **done**.
5. **`Buffer`**, on top of the overlay graph, **done**.
