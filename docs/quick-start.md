# Quick start

This guide assumes you have added `geolatte-geom` as a dependency (see the [README](../README.md#using-geolatte-geom)).

## Creating Geometries

To create a Geometry we first need to specify the Coordinate Reference System we will be working in. Let's say we use
WGS84 (for other options, see [Coordinate Reference Systems](manual.md#coordinate-reference-systems)).

```java
import org.geolatte.geom.*;
import org.geolatte.geom.crs.*;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
```

The easiest way to create `Geometry` instances is by using the built-in builder DSL. This allows you to specify
2D `Position`s (coordinates) using `c(x,y)` for Cartesian or projected coordinates, and `g(long,lat)` for geodetic or
spherical coordinates. (There are also variants for the higher dimensions).

```java
import static org.geolatte.geom.builder.DSL.*;
```

Now we can create geometries like so.

```java
Point<G2D> pnt = point(WGS84, g(4.33, 53.21));

LineString<G2D> lstr = linestring(WGS84, g(4.43, 53.21), g(4.44, 53.20), g(4.45, 53.19));

Polygon<G2D> pgn = polygon(WGS84, ring(g(4.43, 53.21), g(4.44, 53.22), g(4.45, 53.21), g(4.43, 53.21)));
```

We can also create Geometries in a higher-dimensional space. Let's do it in 3D.

First we again need to specify the coordinate reference system we will be working in. In this case, we derive the system
from WGS84 by adding a Vertical system for the elevation.

```java
CoordinateReferenceSystem<G3D> wgs84E = WGS84.addVerticalSystem(LinearUnit.METER, G3D.class);

Point<G3D> pointWithElevation = point(wgs84E, g(4.33, 53.21, 350));
```

## Encoding and Decoding Geometries to WKT/WKB

Now let's write these out as WKT string.

```java
import org.geolatte.geom.codec.Wkt;

String wkt = Wkt.toWkt(pnt);
// "SRID=4326;POINT(4.33 53.21)"

// or maybe using a specific dialect such as SFA 1.2.1
String wktZ = Wkt.toWkt(pointWithElevation, Wkt.Dialect.SFA_1_2_1);
// "POINT Z (4.33 53.21 350)"
```

There is a very similar API for WKB encoding/decoding (see the `Wkb` codec class).

For historical and practical reasons, the default dialects for WKB/WKT are those used by [Postgis](https://postgis.net).

## Next steps

- The [manual](manual.md) explains positions, geometries, coordinate reference systems and geometry operations.
- For GeoJSON, see the [`geolatte-geojson-jackson2`](../json-jackson2/README.md) or
  [`geolatte-geojson-jackson3`](../json-jackson3/README.md) module.
