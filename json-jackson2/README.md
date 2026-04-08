
[![javadoc](https://javadoc.io/badge2/org.geolatte/geolatte-geojson-jackson2/javadoc.svg)](https://javadoc.io/doc/org.geolatte/geolatte-geojson-jackson2)


# GeoJson module — Jackson 2 adapter

This module provides a Jackson 2 adapter that registers a `GeolatteGeomModule` for
(de)serializing `Geometry`, `Feature` and `FeatureCollection` instances to/from GeoJSON.

The actual GeoJSON encoding/decoding logic lives in the Jackson-free
`geolatte-geojson-core` module; this module is a thin Jackson 2 adapter on top of it.
A separate `geolatte-geojson-jackson3` module provides the equivalent for Jackson 3.x.

> **Use this module** if your project is on Jackson 2.x (e.g. Spring Boot 2.x or 3.x lines that
> still ship Jackson 2). If you are on Jackson 3.x, use `geolatte-geojson-jackson3` instead.

# How to use
Add the following dependency to the POM.

```xml

<dependency>
    <groupId>org.geolatte</groupId>
    <artifactId>geolatte-geojson-jackson2</artifactId>
    <version>${geolatte-geom-version}</version>
</dependency>
```

This module is built and tested against Jackson 2.18.x. The Jackson dependency is declared as
`<optional>true</optional>`: consumers must declare the desired Jackson 2 version explicitly in
their own POM.

The library provides a custom module `GeolatteGeomModule` (in package
`org.geolatte.geom.json.jackson2`) that can be added to a Jackson 2 `ObjectMapper`.

```java
    import org.geolatte.geom.json.jackson2.GeolatteGeomModule;

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new GeolatteGeomModule());
```

# Configuration

Optionally you can customize the `GeolatteGeomModule` behavior by:

- setting the default `CoordinateReferenceSystem` to use
- setting a feature flag

## Configuring a default Coordinate Reference System

You can configure a default Coordinate Reference System by passing it in the constructor of the `GeolatteGeomModule`

```java
    CoordinateReferenceSystem<G2D> crs = ...;
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new GeolatteGeomModule(crs));
```

If no default is specified in the constructor, the default will be set to `WGS84`.
The default Coordinate Reference System will be used during deserialization from GeoJson whenever one of the following
conditions hold:

- the GeoJson `Geometry` doesn't have a `crs` element
- the `IGNORE_CRS` feature flag setting is set (see below)


## Feature flags

You can set a feature flag after creation of the `GeolatteGeomModule`:

```java
    GeolatteGeomModule module = new GeolatteGeomModule();
    module.set(Setting.IGNORE_CRS, true);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(module);
```

The following settings are currently supported:
- `IGNORE_CRS`: ignore the `crs` element in the GeoJson `Geometry` (if any) and always use the default coordinate (default: `false`)
- `SUPPRESS_CRS_SERIALIZATION`: do not serialize a `crs` object in `Geometry` GeoJsons (default: `false`)
- `SERIALIZE_CRS_AS_URN`: serialize `crs` as a URN (default: `false`)
