![Build Status](https://github.com/GeoLatte/geolatte-geom/workflows/Java%20CI/badge.svg)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/GeoLatte/geolatte-geom.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/GeoLatte/geolatte-geom/context:java)

# Geolatte-geom

A geometry model for Java with:
* immutable data structures
* support for 2D, 3D, 2DM and 3DM geometries
* A DSL for creating Geometries 
* support for several dialects of WKT/WKB (Postgis, Sql Server, SFA 1.1.0 and 1.2.1)
* Codecs for translating from/to native database formats for Postgis, Mysql, Oracle, and Microsoft SQL Server. 
* Pluggable, extendable Geometry operations
* Coordinate reference system aware
* space filling curves

The library's geometry model is largely based on the 
[Simple Feature Access (1.2.1) specification](https://portal.ogc.org/files/?artifact_id=25355). 

GeoLatte-geom is fully interoperable with [the Java Topology Suite (JTS)](https://github.com/locationtech/jts).  

# Using Geolatte-geom 

Currently we require Java 1.8 or later.

The library is published on Maven Central. For Maven, you can include the following dependency. 

```xml
<dependency>
    <groupId>org.geolatte</groupId>
    <artifactId>geolatte-geom</artifactId>
    <version>1.8.0</version>
</dependency>
```

# Quick start

## Creating Geometries
To create a Geometry we first need to specify the Coordinate Reference System we will be working in. Let's say we use 
WGS84 (for other options, see [below](#coordinate-reference-systems)).

```java
import org.geolatte.geom.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

```

The easiest way to create `Geometry` instances is by using the built-in builder DSL. This allows you to specify 2D `Position`s 
(coordinates) using `c(x,y)`for Cartesian or projected coordinates, and 
`g(long,lat)` for geodetic or spherical coordinates. (There are also variants for the higher dimensions).

```java
import static org.geolatte.geom.builder.DSL.*;
```
Now we can create geometries like so.
```java
...
Point<G2D> pnt = point(WGS84, g(4.33,53.21));

LineString<G2D> lstr = linestring(WGS84, g(4.43, 53.21), g(4.44, 53.20), g(4.45, 53.19));

Polygon<G2D> pgn = polygon(WGS84, ring(g(4.43, 53.21), g(4.44, 53.22), g(4.43, 53.21)));
```
We can also create Geometries in a higher-dimensional space. Let's do it in 3D.

First we again need to specify the coordinate reference system we will be working in. In this case, we derive 
the system from WGS84 by adding a Vertical system for the elevation. 

```java

CoordinateReferenceSystem<G3D>  wgs84E =  WGS84.addVerticalSystem(LinearUnit.METER, G3D.class);
...
        
Point<G3D> pntWithElevation = point(wgs84E, g(4.33, 53.21, 350));

```

## Encoding and Decoding Geometries to WKT/WKB

Now let's write these out as WKT string.
```java
import org.geolatte.geom.codec.Wkt;

String wkt = Wkt.toWkt(pnt);
// "SRID=4326;POINT(4.33 53.21)"

// or maybe using a specific dialect such as SFA 1.2.1
String wktZ = Wkt.toWkt(pntWithElevation, Wkt.Dialect.SFA_1_2_1);
// "POINT Z (4.33 53.21 350)"
```

There is a very similar API for WKB encoding/decoding (see the `Wkb` codec class).

For historical and practical reasons. The default dialects for WKB/WKT are those
used in [Postgis](http://postgis.org).

## Geometry operations 

[TODO]

# The Geometry model

## Positions

A `Position` is a tuple of coordinates that specify a position relative to a coordinate reference system. 
It corresponds with to the concept of **direct position** in the Simple Feature 
and ISO-19107 specifications. 

The coordinate space can be 2-, 3- or 4-dimensional. The first two dimensions are  used to specify a 
point on the earth's surface. The third dimension usually represent altitude or elevation, 
and the fourth a measurement. 

There are two major types of 2D coordinate reference systems. `GeographicCoordinateReferenceSystem`s specify
points on the earth's surface using spherical coordinates (i.e. latitude` and longitude). 
`ProjectedCoordinateReferenceSystem`s use cartesian coordinates (x and y) on a projected plane. 

From these 2D-spaces the higher-dimensional spaces can be constructed by adding a 
`VerticalCoordinateReferenceSystem` and/or a `LinearCoordinateReferenceSystem`.   

Consequently, the instantiable (2D) types of `Position` are `G2D` (spherical coordinates) and `C2D` (cartesian coordinates) in a 
geographic, resp. projected coordinate reference system. From these the higher-dimensional subtypes 
can be derived. E.g. from `C2D`, we can build `C3D`, `C2DM` and `C3DM` positions. 


## Geometry 

A `Geometry` is a topologically closed set (in the mathematical sense) of `Position`s. The instantiable `Geometry`
subclasses all specify this set using one or more boundaries. The boundaries in turn are specified by
interpolation between consecutive elements in a list of `Position`s. These `Position`s are called the _vertices_ of the 
`Geometry`.

A distinctive feature of this library is that `Geometry` class is parameterized by `Position` type. This means that e.g.
a `Point<C2D>` is a different type than `Point<G2D>`. This ensures that it is always explicit what the coordinates mean 
(projected or spherical), and what types of operation make sense. E.g. the euclydian distance on the plane works for 
projected coordinates, but makes no sense for spherical coordinates.


The instantiable subclasses of `Geometry` are:

- `Point` a single position
- `LineString` a 1-dimensional curve specified by linear interpolation between its vertices
- `Polygon` a 2-dimensional space enclosed by an outer `LinearRing` (a closed `LineString`), minus the space enclosed
by any inner `LinearRing`s.
- `MultiPoint` a collection of `Point`s
- `MultiLineString` a collection of `LineString`s
- `MultiPolygon` a collection of `Polygon`s
- `GeometryCollection` a collection of `Geometry`s

## Coordinate Reference Systems 
[TODO]

# JTS interop

[TODO]







