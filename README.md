[![Build Status](https://github.com/GeoLatte/geolatte-geom/actions/workflows/java-ci.yml/badge.svg)](https://github.com/GeoLatte/geolatte-geom/actions/workflows/java-ci.yml)
[![javadoc](https://javadoc.io/badge2/org.geolatte/geolatte-geom/javadoc.svg)](https://javadoc.io/doc/org.geolatte/geolatte-geom)

# Geolatte-geom

A geometry model for Java with:

* immutable data structures
* support for 2D, 3D, 2DM and 3DM geometries
* A DSL for creating Geometries
* support for several dialects of WKT/WKB (SFA 1.1.0 and 1.2.1, Postgis, MySQL, SAP HANA, and DB2 for WKT only)
* Codecs for translating from/to native database formats for Postgis, MySQL, Oracle (`SDO_GEOMETRY`), Microsoft SQL
  Server (`GEOMETRY` and `GEOGRAPHY`), and DB2 (as used by Hibernate Spatial).
* Pluggable, extendable Geometry operations
* Coordinate reference system aware
* space filling curves

The library's geometry model is largely based on the
[Simple Feature Access (1.2.1) specification](https://portal.ogc.org/files/?artifact_id=25355).

GeoLatte-geom is fully interoperable with [the Java Topology Suite (JTS)](https://github.com/locationtech/jts).

# Using Geolatte-geom

Currently we require Java 17 or later.

The library is published on Maven Central. For Maven, you can include the following dependency.

```xml

<dependency>
    <groupId>org.geolatte</groupId>
    <artifactId>geolatte-geom</artifactId>
    <version>1.13</version>
</dependency>
```

# Documentation

- [Quick start](docs/quick-start.md): creating geometries and encoding them to WKT/WKB
- [Manual](docs/manual.md): positions, geometries, coordinate reference systems and geometry operations
- [Javadoc on javadoc.io](https://javadoc.io/doc/org.geolatte/geolatte-geom)

# GeoJSON

GeoJSON support is provided by separate modules, depending on the Jackson version you use:

- [`geolatte-geojson-jackson2`](json-jackson2/README.md) for Jackson 2.x
- [`geolatte-geojson-jackson3`](json-jackson3/README.md) for Jackson 3.x

Both are thin adapters on top of `geolatte-geojson-core`, which holds the Jackson-independent GeoJSON encoding and
decoding logic. The GeoJSON modules are versioned independently of `geolatte-geom`.

# Releasing

Releases are published automatically to Maven Central via GitHub Actions when a version tag is pushed.

The repository has two independently versioned release lines: `geom` (`geolatte-geom`, versioned by `${revision}` in
the root `pom.xml`) and `geojson` (the three GeoJSON modules). The tag prefix selects what is published:

| Tag           | Publishes                                   |
|---------------|---------------------------------------------|
| `geom-v*`     | `geolatte-geom` only                        |
| `geojson-v*`  | the three GeoJSON modules only              |
| `v*`          | all modules                                 |

Publishing a version that is already on Maven Central fails, so only use `v*` when both lines have a new version.
For example, to release `geolatte-geom` on its own:

```bash
scripts/bump-version.sh geom 1.13
git commit -am "Set revision to 1.13"
git tag -a geom-v1.13 -m "Release geolatte-geom 1.13"
git push origin master geom-v1.13
```

See [docs/release-process.md](docs/release-process.md) for the details of both release lines, SNAPSHOT releases and
manual deploys.

The GitHub Actions workflow will:
1. Build the project with Java 17
2. Run all tests
3. Generate source and JavaDoc JARs
4. Sign all artifacts with GPG
5. Publish to Maven Central
6. Create a GitHub Release

Artifacts typically appear on Maven Central within 15-30 minutes.
