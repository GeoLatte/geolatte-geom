# Manual

## Positions

A `Position` is a tuple of coordinates that specify a position relative to a coordinate reference system. It corresponds
to the concept of **direct position** in the Simple Feature and ISO-19107 specifications.

The coordinate space can be 2-, 3- or 4-dimensional. The first two dimensions are used to specify a point on the earth's
surface. The third dimension usually represent altitude or elevation, and the fourth a measurement.

There are two major types of 2D coordinate reference systems. `GeographicCoordinateReferenceSystem`s specify points on
the earth's surface using spherical coordinates (i.e. latitude and longitude).
`ProjectedCoordinateReferenceSystem`s use cartesian coordinates (x and y) on a projected plane.

From these 2D base systems we can construct higher-dimensional systems by adding a
`VerticalCoordinateReferenceSystem` for elevation and/or a `LinearCoordinateReferenceSystem` for a (user-defined)
measurement (see [below](#coordinate-reference-systems)).

Consequently, the instantiable (2D) types of `Position` are `G2D` (spherical coordinates) and `C2D` (cartesian
coordinates) in a geographic, resp. projected coordinate reference system. Subtypes of `G2D`for the higher dimensional
coordinate reference system are `G3D` (position + elevation), `G2DM` (position + measure), and `G3DM`. For `C2D` a
parallel hierarchy exists. This leads to the following class hierarchy.

![Position Class Diagram](resources/positions_hierarchy.png)

## Geometry

A `Geometry` is a topologically closed set (in the mathematical sense) of `Position`s. The instantiable `Geometry`
subclasses all specify this set using one or more boundaries. The boundaries in turn are specified by interpolation
between consecutive elements in a list of `Position`s. These `Position`s are called the _vertices_ of the
`Geometry`.

A distinctive feature of this library is that `Geometry` class is parameterized by `Position` type. This means that e.g.
a `Point<C2D>` is a different type than `Point<G2D>`. This ensures that it is always explicit what the coordinates mean
(projected or spherical), and what types of operation make sense. E.g. the Euclidean distance on the plane works for
projected coordinates, but makes no sense for spherical coordinates.

The instantiable subclasses of `Geometry` are:

- `Point`: a single position
- `LineString`: a 1-dimensional curve specified by linear interpolation between its vertices
- `Polygon`: a 2-dimensional space enclosed by an outer `LinearRing` (a closed `LineString`), minus the space enclosed
  by any inner `LinearRing`s.
- `MultiPoint`: a collection of `Point`s
- `MultiLineString`: a collection of `LineString`s
- `MultiPolygon`: a collection of `Polygon`s
- `GeometryCollection`: a collection of `Geometry`s

## Coordinate Reference Systems

For many operations a `CoordinateReferenceSystem` is required. The most important ways to create or get hold of a
`CoordinateReferenceSystem` instance are:

- Using one of the statically declared systems in the `CoordinateReferenceSystems` class (e.g. `WGS84`, `WEB_MERCATOR`)
- Find the system by its [EPSG](https://epsg.org/home.html) ID in the `CrsRegistry`. The `CrsRegistry` has most of the
  coordinate reference systems in use.

As already mentioned, you can create higher-dimensional coordinate reference systems by adding a vertical or linear
system.

```java
CoordinateReferenceSystem<C2D> crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
CoordinateReferenceSystem<C3D> crsZ = crs.addVerticalSystem(LinearUnit.METER, C3D.class);
CoordinateReferenceSystem<C3DM> crsZM = crsZ.addLinearSystem(LinearUnit.METER, C3DM.class);
```

## Geometry operations

The [Simple Feature specification](https://portal.ogc.org/files/?artifact_id=25355) specifies a number of spatial query
and analysis operations as methods on the `Geometry` classes. From a design standpoint, declaring these operations as
methods has several drawbacks:

- Some operations need very different implementations in case of projected vs. geographic coordinate reference systems.
- It precludes the possibility of having alternative implementations for the operations with perhaps very different
  performance or correctness properties

For these reasons the spatial operations are separated into their own classes. Currently, two types are supported:

- `ProjectedGeometryOperations` : operations on projected (planar) geometries (i.e. `C2D` and subtypes)
- `MeasureGeometryOperations` : operations specific to projected (planar) geometries with a Measure (i.e. `C2DM`,
  `C3DM`)

At present there is no support for operations on geometries in a `GeographicCoordinateReferenceSystem`.

You can get hold of an operations class using the `GeometryOperations` factory class. Currently there are only default
implementations available. The `ProjectedGeometryOperations` implementation will delegate most operations to the
corresponding JTS implementation.

```java
// example of a contains test with planar geometries (Web Mercator coordinates are in meters)
ProjectedGeometryOperations pgo = GeometryOperations.projectedGeometryOperations();
Point<C2D> point = point(WEB_MERCATOR, c(482000, 7020000));
Polygon<C2D> polygon = polygon(WEB_MERCATOR, ring(c(481000, 7019000), c(483000, 7019000), c(483000, 7021000),
        c(481000, 7021000), c(481000, 7019000)));
boolean isPntInPoly = pgo.contains(polygon, point);
// true

// example of an operation on measured geometries
CoordinateReferenceSystem<C2DM> crsM = CoordinateReferenceSystems.PROJECTED_2DM_METER;
MeasureGeometryOperations mgo = GeometryOperations.measureGeometryOperations();
LineString<C2DM> linestring = linestring(crsM, cM(0, 0, 0), cM(10, 0, 1), cM(20, 0, 2));
// create a geometry along the linestring that is between measures 1.5 and 1.8
Geometry<C2DM> c2DMGeometry = mgo.locateBetween(linestring, 1.5, 1.8);
// MULTILINESTRINGM((15 0 1.5,18 0 1.8))
```

## JTS interop

Because JTS has been the dominant geometry library in the Java ecosystem, JTS interoperability has been a major concern.
The `JTS` class supports conversion from/to JTS `Geometry` instances.

```java
import org.geolatte.geom.jts.JTS;

// Geolatte to JTS
org.locationtech.jts.geom.Point jtsPoint = JTS.to(point(WGS84, g(4.32, 53.12)));
// ... and back
org.geolatte.geom.Point<?> glPnt = JTS.from(jtsPoint);
// or if you know the CRS in advance
org.geolatte.geom.Point<G2D> glPnt2 = JTS.from(jtsPoint, WGS84);
```
